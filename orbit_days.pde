float sun_rad = 1/16;				// sun radius
float planet_rad = 1/24; 			// planet radius
float orbit_rad = 1/6;				// orbit radius
float axis_len = 1/20;				// length of protruding axis segments
												// all lengths above are given as a proportion of the screen width

float view_tilt = PI/5;				// angular tilt of our view of the planet + sun system
float axis_tilt = PI/4;				// angular deviation of the planet's axis from the normal line

float orbit_speed = PI/2000;		// angular velocity of the planet's rotation about the sun
float rotate_speed = PI/20;		// angular velocity of the planet's rotation about its axis

float my_latitude = PI/2.5; 		// latitude of the observer on the planet

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

	float real_solar_dist = sqrt(sq(my_real_xpos) + sq(my_real_ypos) + sq(my_real_zpos));
	float critical_dist = sqrt(sq(orbit_rad) + sq(planet_rad));

	// dark background
   background(0, 0, 0);

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

	// check daytime/nighttime
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

// move the planet incrementally farther along in its orbit
	planet_theta += orbit_speed;
	my_theta += rotate_speed;

// draw the parameter selection menu
	fill(255, 255, 255);
	textSize(20);
	text("Viewing Angle", 2 * sW / 3, 3 * sH / 4);
	text("Observer Latitude", 2 * sW / 3, 16 * sH / 20);
	text("Axial Tilt", 2 * sW / 3, 17 * sH / 20);
	text("Speed of Revolution", 2 * sW / 3, 18 * sH / 20);
	text("Speed of Rotation", 2 * sW / 3, 19 * sH / 20);
	fill(255, 0, 0);
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
			} else if (selected_param == 3 && orbit_speed < PI/100) {
				orbit_speed += PI/4000;
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
			} else if (selected_param == 3 && orbit_speed > -PI/100) {
				orbit_speed += -PI/4000;
			} else if (selected_param == 4 && rotate_speed > -PI/10) {
				rotate_speed += -PI/400;
			}
		}
	}

}

// toggle between different parameters
void keyPressed() {
	if (key == 't') {
		selected_param = (selected_param + 1) % 5;
	}
}
