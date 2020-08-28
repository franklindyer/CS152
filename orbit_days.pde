/*****************************************
 * Assignment 2
 * Name:    Franklin Pezzuti Dyer
 * E-mail:  fpezzutidyer@unm.edu
 * Course:      CS 152 - Section 005
 * Submitted:    8/29/2020
 * 
 * A simulation of day/night cycles on a planet revolving about a star.
 * Press X to select from among the parameters of the planet's orbit.
 * Use the UP and DOWN arrow keys to increase or decrease the selected value.
***********************************************/

float sun_rad = 1/16;				// sun radius
float planet_rad = 1/24; 			// planet radius
float orbit_rad = 1/6;				// orbit radius
float axis_len = 1/20;				// length of protruding axis segments
												// all lengths above are given as a proportion of the screen width

float view_tilt = PI/5;				// angular tilt of our view of the planet + sun system
float axis_tilt = PI/8;				// angular deviation of the planet's axis from the normal line

float orbit_speed = PI/8000;		// angular velocity of the planet's rotation about the sun
float rotate_speed = PI/200;		// angular velocity of the planet's rotation about its axis

float my_latitude = PI/6; 		// latitude of the observer on the planet

float vision_angle = PI/2;		// the highest angle of inclination that the observer can see

int selected_param = 0;

void setup() {

	size(screenWidth, screenHeight);
	frameRate(50);

	// set the starting position of the planet relative to the sun
	planet_theta = 0.0;
	my_theta = 0.0;

}

void draw() {
	frameRate(60);

	// aliases to avoid typing out long variable names
	float sW = screenWidth;
	float sH = screenHeight;
	
	// lots of trig calculations!
	float planetX = sW / 4 + sW * orbit_rad * sin(planet_theta);
	float planetY = sH / 2 + sW * orbit_rad * cos(planet_theta) * sin(view_tilt);
	
	float real_planetX = orbit_rad * sin(planet_theta);
	float real_planetY = 0;
	float real_planetZ = orbit_rad * cos(planet_theta);
	
	float my_xpos = planetX - sW * planet_rad * (-sin(my_latitude) * sin(axis_tilt) + sin(my_theta) * cos(my_latitude) * cos(axis_tilt));
	float my_ypos = planetY - sW * planet_rad * (-cos(my_latitude) * cos(my_theta) * sin(view_tilt) + (sin(my_latitude) * cos(axis_tilt) + sin(my_theta) * cos(my_latitude) * sin(axis_tilt)) * cos(view_tilt));
	float my_zpos = real_planetZ - planet_rad * (cos(my_latitude) * cos(my_theta) * cos(view_tilt) + (sin(my_latitude) * cos(axis_tilt) + sin(my_theta) * cos(my_latitude) * sin(axis_tilt)) * sin(view_tilt));
	
	float my_real_xpos = real_planetX - planet_rad * (sin(my_theta) * cos(my_latitude) * cos(axis_tilt) - sin(axis_tilt) * sin(my_latitude));
	float my_real_ypos = real_planetY + planet_rad * (-sin(axis_tilt) * sin(my_theta) * cos(my_latitude) + cos(axis_tilt) * sin(my_latitude));
	float my_real_zpos = real_planetZ + planet_rad * cos(my_theta) * cos(my_latitude);

	float unit_xvel = - cos(my_theta) * cos(my_latitude) * cos(axis_tilt);
	float unit_yvel = cos(my_theta) * cos(my_latitude) * sin(axis_tilt);
	float unit_zvel = - sin(my_theta) * cos(my_latitude);

	float solar_normal_deviation = acos(-((my_real_xpos - real_planetX) * real_planetX + (my_real_ypos - real_planetY) * real_planetY + (my_real_zpos - real_planetZ) * real_planetZ) / (sqrt(sq(my_real_xpos - real_planetX) + sq(my_real_ypos - real_planetY) + sq(my_real_zpos - real_planetZ)) * sqrt(sq(real_planetX) + sq(real_planetY) + sq(real_planetZ))));

	float real_solar_dist = sqrt(sq(my_real_xpos) + sq(my_real_ypos) + sq(my_real_zpos));
	float critical_dist = sqrt(sq(orbit_rad) + sq(planet_rad));

	// dark background
   background(0, 0, 0);

	// write my name
	fill(255, 255, 255);
	textSize(15);
	text("Franklin Pezzuti Dyer", sW / 100, 79 * sH / 80);

	// draw the sun	
	noStroke();
	fill(255, 255, 0);
	ellipse(sW / 4, sH / 2, 2 * sW * sun_rad, 2 * sW * sun_rad);

	// draw the planet's lower axis
	stroke(255, 0, 0);
	strokeWeight(5);
	line(planetX - sW * planet_rad * sin(axis_tilt), 
			planetY + sW * planet_rad * cos(axis_tilt) * cos(view_tilt), 
			planetX - sW * (planet_rad + axis_len) * sin(axis_tilt), 
			planetY + sW * (planet_rad + axis_len) * cos(axis_tilt) * cos(view_tilt));

	// draw the planet
	noStroke();
	fill(0, 99, 199);
	ellipse(planetX,
				planetY, 
				2 * sW * planet_rad,
				2 * sW * planet_rad);

	// draw the observer at a point on the planet's surface
	if (my_zpos <= real_planetZ) {
		fill(255, 255, 255);
		stroke(0, 0, 0);
		strokeWeight(1);
		quad(my_xpos, my_ypos + 5,
				my_xpos + 5, my_ypos,
				my_xpos, my_ypos - 5,
				my_xpos - 5, my_ypos);
	}

	// draw the planet's upper axis
	stroke(255, 0, 0);
	strokeWeight(5);
	line(planetX + sW * planet_rad * sin(axis_tilt), 
			planetY - sW * planet_rad * cos(axis_tilt) * cos(view_tilt), 
			planetX + sW * (planet_rad + axis_len) * sin(axis_tilt), 
			planetY - sW * (planet_rad + axis_len) * cos(axis_tilt) * cos(view_tilt));

	// draw the sun again over top of the planet, if it is "in front" of the planet
	if (cos(planet_theta) < 0) {
		noStroke();
		fill(255, 255, 0);
		ellipse(sW / 4, sH / 2, 2 * sW * sun_rad, 2 * sW * sun_rad);
	}

	// check daytime/nighttime and color the sky accordingly, including sunrise/sunset colors
	noStroke();
	if (real_solar_dist > critical_dist * 1.05) {
		fill(0, 0, 0);
	} else if (real_solar_dist < critical_dist * 0.95) {
		fill(80, 175, 255);
	} else if (real_solar_dist < critical_dist) {
		float bc = (real_solar_dist - 0.95 * critical_dist)/(critical_dist * 0.05);
		fill(floor(80 * (1 - bc) + 255 * bc / 2), floor(175 * (1 - bc) + 102 * bc / 2), floor(255 * (1 - bc) + 255 * bc / 2));
	} else {
		float bc = (1.05 * critical_dist - real_solar_dist)/(critical_dist * 0.05);
		fill (floor(255 * bc / 2), floor(102 * bc / 2), floor(255 * sq(bc) / 2 + 102 * (1 - bc) * bc / 2));
	}
	rect(sW / 2, 0, sW / 2, 2 * sH / 3);

// draw the sun in the sky
	if (solar_normal_deviation < PI/2 + PI/10 && solar_normal_deviation > PI/2 - vision_angle - PI/10) {
		fill(255, 255, 255);
		ellipse(3 * sW / 4, 
					2 * sH * (1 - cos(solar_normal_deviation) / sin(vision_angle)) / 3,
					100, 100);
	}

// draw the parameter selection menu
	fill(0, 0, 0);
	rect(sW / 2, 2 * sH / 3, sW / 2, sH / 3);
	fill(255, 255, 255);
	textSize(15);
	text("Press X to toggle and use arrow keys to adjust parameters.", sW / 2, 33 * sH / 48);
	textSize(20);
	text("Viewing Angle", 2 * sW / 3, 3 * sH / 4);
	text("Observer Latitude", 2 * sW / 3, 16 * sH / 20);
	text("Axial Tilt", 2 * sW / 3, 17 * sH / 20);
	text("Speed of Revolution", 2 * sW / 3, 18 * sH / 20);
	text("Speed of Rotation", 2 * sW / 3, 19 * sH / 20);
	fill(255, 0, 0);
	stroke(255, 100, 100);
	strokeWeight(3)
	ellipse((2 / 3 - 1 / 40) * sW, (3 / 4 - 1 / 160 + selected_param / 20) * sH, 10, 10);

// change the selected parameter using the arrow keys, within given bounds
	if (keyPressed == true) {
		if (keyCode == UP) {
			if (selected_param == 0 && view_tilt < PI/2) {
				view_tilt += PI/200;
			} else if (selected_param == 1 && my_latitude < PI/2) {
				my_latitude += PI/200;
			} else if (selected_param == 2 && axis_tilt < PI/2) {
				axis_tilt += PI/200;
			} else if (selected_param == 3 && orbit_speed < PI/200) {
				orbit_speed += PI/20000;
			} else if (selected_param == 4 && rotate_speed < PI/10) {
				rotate_speed += PI/400;
			}
		} else if (keyCode == DOWN) {
			if (selected_param == 0 && view_tilt > 0) {
				view_tilt += -PI/200;
			} else if (selected_param == 1 && my_latitude > 0) {
				my_latitude += -PI/200;
			} else if (selected_param == 2 && axis_tilt > 0) {
				axis_tilt += -PI/200;
			} else if (selected_param == 3 && orbit_speed > -PI/200) {
				orbit_speed += -PI/20000;
			} else if (selected_param == 4 && rotate_speed > -PI/10) {
				rotate_speed += -PI/400;
			}
		}
	}

// move the planet incrementally farther along in its orbit
	planet_theta += orbit_speed;
	my_theta += rotate_speed;


}

// toggle between different parameters
void keyPressed() {
	if (key == 'x') {
		selected_param = (selected_param + 1) % 5;
	}
}
