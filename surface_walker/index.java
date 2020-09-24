/*****************************************
 * Assignment 5
 * Name:    Franklin Pezzuti Dyer
 * E-mail:  fpezzutidyer@unm.edu
 * Course:      CS 152 - Section 005
 * Submitted:    09/24/2020
 * 
 * Move the little dude around the screen using the arrow keys.
 * By default, you are walking on a rectangle, such that when you reach the edge of the screen, you cannot go any farther.
 * However, there are 6 different topological surfaces to choose from.
 * Press the number keys 1-6 to switch between topological surfaces. Here is a key for changing between different surfaces:
 * 1 - Rectangle (all edges are boundaries)
 * 2 - Cylinder (top/bottom edges are boundaries, left/right edges wrap around)
 * 3 - Torus (top/bottom and left/right edges wrap around)
 * 4 - Mobius Strip (top/bottom edges are boundaries, left/right edges wrap around with inversion)
 * 5 - Projective Plane (both pairs of edges wrap around, but the left/right edges include an inversion)
 * 6 - Klein Bottle (both pairs of edges wrap around with inversion)
 * Extra credit #2 asks for the player to "wrap around" the edges of the screen, which corresponds to the Torus (3).
***********************************************/

Vehicle vehicle;
float friction = 0.1;
float acceleration = 0.5;
boolean[4] keysPressed; // [up, left, down, right]
float walkCycle = 0;
char surfaceType = 'r';
int num_grasses;
float[] grass_x, grass_y;

void setup() {
	frameRate(60);
  size(screenWidth, screenHeight);
	vehicle = new Vehicle();
	keysPressed = [0, 0, 0, 0];
	
	// generate coordinates of grass patches such that they are somewhat evenly distributed
	num_grasses = ceil(width / 100) * ceil(height / 100);
	grass_x = new float[num_grasses];
	grass_y = new float[num_grasses];
	for (int i = 0; i < ceil(width / 100); i++) {
	  for (int j = 0; j < ceil(height / 100); j++) {
	    grass_x[i * ceil(height / 100) + j] = 100 * i + (int)random(100);
	    grass_y[i * ceil(height / 100) + j] = 100 * j + (int)random(100);
	  }
	}
}

void draw() {
	
	// draw background with grass
	background(170, 255, 170);
	for (int i = 0; i < num_grasses; i++) {
	  grass(grass_x[i], grass_y[i]);
	}
	
	// update the vehicle's position and adjust it to account for looping around the screen
	vehicle.updatePos(friction);
	vehicle.modifyPos(surfaceType);
	
	// update the walkCycle variable used to generate the moving-legs animation
	if (keysPressed[0] || keysPressed[1] || keysPressed[2] || keysPressed[3]) {
	  walkCycle += PI/20;
	} else {
	  walkCycle = 0;
	}
	
	// draw the vehicle
	vehicle.drawPos(walkCycle);

  // update vehicle velocity based on which keys are pressed
	if (keysPressed[0]) vehicle.yvel += acceleration;
	if (keysPressed[1]) vehicle.xvel += -acceleration;
	if (keysPressed[2]) vehicle.yvel += -acceleration;
	if (keysPressed[3]) vehicle.xvel += acceleration;
}

// keep track of when keys are pressed
void keyPressed() {
	if (key == CODED) {
	  if (keyCode == UP) {
	  	keysPressed[0] = 1;
	  } else if (keyCode == LEFT) {
	  	keysPressed[1] = 1;
	  } else if (keyCode == DOWN) {
	  	keysPressed[2] = 1;
	  } else if (keyCode == RIGHT) {
	  	keysPressed[3] = 1;
	  }
	}
}

// keep track of when keys are released
void keyReleased() {
	if (key == CODED) {
	  if (keyCode == UP) {
	  	keysPressed[0] = 0;
	  } else if (keyCode == LEFT) {
	  	keysPressed[1] = 0;
	  } else if (keyCode == DOWN) {
	  	keysPressed[2] = 0;
	  } else if (keyCode == RIGHT) {
	  	keysPressed[3] = 0;
	  }
	} else if (key == '1') {
	  vehicle.reset();
	  surfaceType = 'r';
	} else if (key == '2') {
	  vehicle.reset();
	  surfaceType = 'c';
	} else if (key == '3') {
	  vehicle.reset();
	  surfaceType = 't';
	} else if (key == '4') {
	  vehicle.reset();
	  surfaceType = 'm';
	} else if (key == '5') {
	  vehicle.reset();
	  surfaceType = 'p';
	} else if (key == '6') {
	  vehicle.reset();
	  surfaceType = 'k';
	}
}

// draw a little patch of grass
void grass(int x, int y) {
  stroke(100, 170, 100);
  line(x, y, x + 10, y - 20);
  line(x - 2, y, x + 2, y - 15);
  line(x - 4, y, x - 8, y - 25);
}

// personalized modulo function used to make the little man loop around the screen
int positive_mod(float x, float m) {
  float result = x % m;
  if (x < 0) {
    result += m;
    result = result % m;
  }
  return result;
}
