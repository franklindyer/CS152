class Vehicle {
	float xpos, ypos;
	float xvel, yvel;
	float mod_xpos, mod_ypos;
	float x_orientation, y_orientation;
	 
  // constructor
	Vehicle() {
		xpos = width / 2;
		ypos = height / 2;
		xvel = 0;
		yvel = 0;
		mod_xpos = xpos;
		mod_ypos = ypos;
		x_orientation = 0;
		y_orientation = 0;
   }
   
  // reset the vehicle's position
  void reset() {
    xpos = width / 2;
    ypos = height / 2;
    xvel = 0;
    yvel = 0;
    mod_xpos = xpos;
    mod_ypos = ypos;
    x_orientation = 0;
    y_orientation = 0;
  }

  // update the vehicle's position and velocity
	void updatePos(float friction) {
		xpos += xvel;
		ypos += -yvel;
		xvel = (1 - friction) * xvel;
		yvel = (1 - friction) * yvel;
	}
	
	// modify the displayed position of the vehicle depending on what kind of surface it is travelling on
	// here's what the different codes stand for:
	// 'r' = rectangle
	// 'c' = cylinder
	// 't' = torus
	// 'm' = mobius strip
	// 'p' = projective plane
	// 'k' = klein bottle
	void modifyPos(char surfaceCode) {
	  // rectangle
	  if (surfaceCode == 'r') {
	    if (xpos > width) {
	      mod_xpos = width - xvel;
	      xvel = -2 * xvel;
	    } else if (xpos < 0) {
	      mod_xpos = xvel;
	      xvel = -2 * xvel;
	    } else { mod_xpos = xpos; }
	    if (ypos > height) {
	      mod_ypos = height - yvel;
	      yvel = -2 * yvel;
	    } else if (ypos < 0) {
	      mod_ypos = yvel;
	      yvel = -2 * yvel;
	    } else { mod_ypos = ypos; }
	  }
	  // cylinder
	  else if (surfaceCode == 'c') {
	    mod_xpos = positive_mod(xpos, width);
	    if (ypos > height) {
	      mod_ypos = height - yvel;
	      yvel = -2 * yvel;
	    } else if (ypos < 0) {
	      mod_ypos = yvel;
	      yvel = -2 * yvel;
	    } else { mod_ypos = ypos; }
	  }
	  // torus
	  else if (surfaceCode == 't') {
	    mod_xpos = positive_mod(xpos, width);
	    mod_ypos = positive_mod(ypos, height);
	  }
	  // mobius strip
	  else if (surfaceCode == 'm') {
	    mod_xpos = positive_mod(xpos, width);
	    if (ypos > height) {
	      mod_ypos = height - yvel;
	      yvel = -2 * yvel;
	    } else if (ypos < 0) {
	      mod_ypos = yvel;
	      yvel = -2 * yvel;
	    } else { mod_ypos = ypos; }
	    y_orientation = positive_mod(floor(xpos / width), 2);
	    if (y_orientation == 1) {
	      mod_ypos = height - mod_ypos;
	    }
	  }
	  // projective plane
	  else if (surfaceCode == 'p') {
	    mod_xpos = positive_mod(xpos, width);
	    mod_ypos = positive_mod(ypos, height);
	    y_orientation = positive_mod(floor(xpos / width), 2);
	    if (y_orientation == 1) {
	      mod_ypos = height - mod_ypos;
	    }
	  }
	  // klein bottle
	  else if (surfaceCode == 'k') {
	    mod_xpos = positive_mod(xpos, width);
	    mod_ypos = positive_mod(ypos, height);
	    y_orientation = positive_mod(floor(xpos / width), 2);
	    x_orientation = positive_mod(floor(ypos / height), 2);
	    if (y_orientation == 1) {
	      mod_ypos = height - mod_ypos;
	    }
	    if (x_orientation == 1) {
	      mod_xpos = width - mod_xpos;
	    }
	  }
	}
	
	// draw the vehicle in its updated and modified position
	void drawPos(boolean walkCycle) {
	  // draw the head
	  fill(220, 220, 0);
	  noStroke();
	  ellipse(mod_xpos, mod_ypos, 40, 40);
	  // draw the eyes
	  fill(0, 0, 0);
	  ellipse(mod_xpos + 15 - 30 * x_orientation,
	          mod_ypos - 5 + 10 * y_orientation,
	          5, 5);
	  ellipse(mod_xpos,
	          mod_ypos - 5 + 10 * y_orientation,
	          5, 5);
	  noFill();
	  // draw the legs, and generate the "running" animation if the dude is moving
	  fill(220, 220, 0);
	  noStroke();
	  if (walkCycle == 0) {
	    rect(mod_xpos - 15 + 25 * x_orientation,
	        mod_ypos + 10 - 40 * y_orientation,
	        5, 20);
	    rect(mod_xpos + 5 - 15 * x_orientation,
	         mod_ypos + 10 - 40 * y_orientation,
	         5, 20);
	  } else if (y_orientation == 0) {
	    rect(mod_xpos - 15 + 25 * x_orientation,
	        mod_ypos + 10 - 40 * y_orientation,
	        5, 13 + 7 * sin(walkCycle)**2);
	    rect(mod_xpos + 5 - 15 * x_orientation,
	         mod_ypos + 10 - 40 * y_orientation,
	         5, 13 + 7 * cos(walkCycle)**2);
	  } else if (y_orientation == 1) {
	    rect(mod_xpos - 15 + 25 * x_orientation,
	        mod_ypos + 10 - 40 * y_orientation + 7 * sin(walkCycle)**2,
	        5, 13 + 7 * sin(walkCycle)**2);
	    rect(mod_xpos + 5 - 15 * x_orientation,
	         mod_ypos + 10 - 40 * y_orientation + 7 * cos(walkCycle)**2,
	         5, 13 + 7 * cos(walkCycle)**2);
	  }
	  // draw the smile
	  stroke(0, 0, 0);
	  arc(mod_xpos + 5 - 10 * x_orientation,
	      mod_ypos - 3 + 6 * y_orientation,
	      20, 20,
	      PI / 4 + PI * y_orientation, 
	      3 * PI / 4 + PI * y_orientation);
	}
}
