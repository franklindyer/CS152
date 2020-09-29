/*****************************************
 * Assignment 6
 * Name:    Franklin Pezzuti Dyer
 * E-mail:  fpezzutidyer@unm.edu
 * Course:      CS 152 - Section 005
 * Submitted:    10/28/2020
 * 
 * Draws an array of randomly generated "faces".
 * By default, the screen is 500 by 500 pixels, and the grid of faces is 5 by 5.
 * However, changing the screen size in setup or the number of rows/columns of faces does not mess up the spacing, as they are drawn dynamically.
 * And yes, before you ask, the mouths are Bezier curves. Yay math!
 * They say you can tell a sage by the twinkle in their eye... but such people are truly one-in-a-thousand.
***********************************************/

int ROWS = 5;
int COLUMNS = 5;
Monster[][] monsters = new Monster[ROWS][COLUMNS];
float grid_unit = min((width - 50) / COLUMNS, (height - 50) / ROWS);

void setup() {
	size(500, 500);

// calculate the amount of space to allocate for a single face
	grid_unit = min((width - 50) / COLUMNS, (height - 50) / ROWS);

// generate an array of monsters
	for (int i = 0; i < ROWS; i++) {
	  for (int j = 0; j < COLUMNS; j++) {
	    // they say a true sage is one-in-a-thousand...
	    boolean special = (random(1) < 0.001)
      monsters[i][j] = new Monster(i, j, special);
    }
	}
}

void draw() {
	background(255);
	stroke(0);

// draw all faces by looping through the array	
	for (int i = 0; i < ROWS; i++) {
	  for (int j = 0; j < COLUMNS; j++) {
      monsters[i][j].updateMe();
      monsters[i][j].drawMe();
    }
	}
}
