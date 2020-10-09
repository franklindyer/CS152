class Monster {
  float xPosition, yPosition;
  float size;
  color monsterColor;
  float roundness;
  Eyes eyes;
  Mouth mouth;
  Name name;
  
  Monster(int row, int column, boolean special) {
// generate random color  
    monsterColor = color((int)random(255), (int)random(255), (int)random(255));
 
// calculate coordinates and a size for the face, given which row and column it is in and the size of a grid unit
    xPosition = width / 2 + grid_unit * (2 * column - COLUMNS + 1) / 2;
    yPosition = height / 2 + grid_unit * (2 * row - ROWS + 1) / 2;
    size = grid_unit * 4 / 5;
    
// randomize face roundness
    roundness = (size / 2) * (1 - random(1)**3);
 
 // generate facial features
    eyes = new Eyes(size, xPosition, yPosition, special);
    mouth = new Mouth(size, xPosition, yPosition, special);
    name = new Name(size, xPosition, yPosition, special);
  }

// draw the face, plus each of its parts  
  void drawMe() {
    fill(monsterColor);
//    ellipse(xPosition, yPosition, size, size);
    rect(xPosition - size / 2, yPosition - size / 2, size, size, roundness);
    eyes.drawMe();
    mouth.drawMe();
    name.drawMe();
  }
  
// update the face dynamically, and each of its parts
  void updateMe() {
    eyes.updateMe();
    mouth.updateMe(size, xPosition, yPosition);
  }
}

class Eyes {
  float xPosition, yPosition;
  float diameter;
  float gap;
  float height;
  float blinkprob;
  boolean blinking;
  float squish;
  float glanceprob;
  float glance;
  float browlength;
  float browstart;
  boolean special;
  
  Eyes(float monsterSize, float monsterX, float monsterY, boolean isSpecial) {

// calculate positions for the eyes using the coordinates of the face and some randomized parameters
    height = random(monsterSize / 3);
    diameter = monsterSize / 6 + random(monsterSize / 6);
    gap = max(monsterSize / 10 + random(2 * monsterSize / 5), diameter / 2);
    xPosition = monsterX;
    yPosition = monsterY - height;

// constants to randomize blinking
    blinkprob = max(0, 0.05 * (random(1) - 0.5));
    blinking = false;
 
// constant to randomize eye shape
    squish = random(0.5, 2);
    
// constants to randomize glancing
    glanceprob = max(0, random(1) - 0.3)
    glance = 0;
    
// constants to randomize eyebrow length and position    
    browlength = random(PI);
    browstart = random(PI - browlength);
    
    special = isSpecial;
    
    // they say a true sage has an unwavering gaze...
    if (special) {
      blinkprob = 0;  
    }
  }
  
  void drawMe() {
    noFill();
    
// draw eyebrows
    arc(xPosition - gap,
        yPosition,
        3 * diameter / 2,
        3 * diameter / (2 * squish),
        2 * PI - browstart - browlength,
        2 * PI - browstart);
    arc(xPosition + gap,
        yPosition,
        3 * diameter / 2,
        3 * diameter / (2 * squish),
        PI + browstart,
        PI + browstart + browlength);

// account for blinking
    fill(255);
    if (blinking) fill(0);

// draw eyes with pupils
    ellipse(xPosition - gap, yPosition, diameter, diameter / squish);
    ellipse(xPosition + gap, yPosition, diameter, diameter / squish);
    fill(0);
    // they say you can tell a true sage by the twinkle in their eye...
    if (special) { fill(150); }
    ellipse(xPosition - gap + glance, yPosition, diameter / 2, diameter / 2);
    ellipse(xPosition + gap + glance, yPosition, diameter / 2, diameter / 2);
  }
  
  void updateMe() {
    
// blink randomly with different frequencies determined by the constants assigned during construction
    if (blinking) {
      if (random(1) < blinkprob * 15) {
        blinking = 0;
      }
    } else {
      if (random(1) < blinkprob) {
        blinking = 1;
        glance = 0;
        if (random(1) < glanceprob) {
          glance = random(-diameter / 4, diameter / 4);
        }
      }
    }
  }
  
}

class Mouth {
  float anchor1X, anchor1Y;
  float anchor2X, anchor2Y;
  float control1X, control1Y;
  float control2X, control2Y;
  float[] bezierMouth = new float[8];
  float[] newBezierMouth = new float[8];
  float transition = 0;
  float changeProb = 0.05;
  float changeSpeed = 1/20;
  boolean special;

// randomize anchor and control points for bezier curve mouth  
  Mouth(float monsterSize, float monsterX, float monsterY, boolean isSpecial) {
    
    bezierMouth = randomBezier(monsterSize, monsterX, monsterY);
    changeProb = max(0, 0.1 * (random(1) - 0.5));
    changeSpeed = random(1)**2 / 20;
    
    special = isSpecial;
    
    // they say a true sage is never unhappy...
    if (special) {
      bezierMouth[0] = monsterX - monsterSize / 3;
      bezierMouth[6] = monsterX + monsterSize / 3;
      bezierMouth[2] = monsterX - monsterSize / 4;
      bezierMouth[4] = monsterX + monsterSize / 5;
      bezierMouth[1] = monsterY + monsterSize / 5;
      bezierMouth[7] = monsterY + monsterSize / 5;
      bezierMouth[3] = monsterY + monsterSize / 3;
      bezierMouth[5] = monsterY + monsterSize / 3;
    }
  
    
  }
  
  float[8] randomBezier(float monsterSize, float monsterX, float monsterY) {
      float[8] newBezier = new float[8];
      newBezier[0] = monsterX - monsterSize / 3 + random(monsterSize / 4);
      newBezier[4] = monsterX + monsterSize / 3 - random(monsterSize / 4);
      newBezier[2] = monsterX - monsterSize / 3 + random(monsterSize / 4);
      newBezier[6] = monsterX + monsterSize / 3 - random(monsterSize / 4)
      newBezier[1] = monsterY + monsterSize / 3 - random(monsterSize / 6);
      newBezier[5] = monsterY + monsterSize / 3 - random(monsterSize / 6);
      newBezier[3] = monsterY + monsterSize / 3 - random(monsterSize / 6);
      newBezier[7] = monsterY + monsterSize / 3 - random(monsterSize / 6);
      return newBezier;
  }
  
// draw mouth
  void drawMe() {
    noFill();
    bezier(bezierMouth[0], bezierMouth[1], bezierMouth[2], bezierMouth[3], bezierMouth[4], bezierMouth[5], bezierMouth[6], bezierMouth[7]);
  }
  
  void updateMe(float monsterSize, float monsterX, float monsterY) {
    if (transition <= 0 && random(1) < changeProb && special == 0) {
      transition = 1;
      newBezierMouth = randomBezier(monsterSize, monsterX, monsterY);
    } else if (transition > 0) {
      transition += - 1/20;
      for (int i = 0; i < 8; i++) {
        bezierMouth[i] = transition * bezierMouth[i] + (1 - transition) * newBezierMouth[i];
      }
    }
  }
}

class Name {
  float xPosition, yPosition;
  float size;
  String name;
  final char[] vowels = { 'A', 'E', 'I', 'O', 'U' };
  final char[] consonants = { 'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z' };
  final char[] special_vowels = { 'Ä', 'Ö', 'Ü', 'Ø', 'Å', 'É', 'Î', 'Œ', '◊', 'Æ' };
  final char[] special_consonants = { '$', 'ß', 'Ç', 'Ñ', '∏'};
  
  Name(float monsterSize, float monsterX, 
    float monsterY, boolean special) {
    size = monsterSize / 6;
    xPosition = monsterX;
    yPosition = monsterY;

// array of random characters for the monster's name    
    char[3] nameChars = { consonants[(int)random(consonants.length)], vowels[(int)random(vowels.length)], consonants[(int)random(consonants.length)] };

// use special characters, but only on occasion
    if (random(1) < 0.1) {
      int whichChar = (int)random(3);
      if (whichChar == 1) {
        nameChars[1] = special_vowels[(int)random(special_vowels.length)];
      } else {
        nameChars[whichChar] = special_consonants[(int)random(special_consonants.length)];
      }
    }
    
    name = "" + nameChars[0].toString() + nameChars[1].toString() + nameChars[2].toString();
    
    // they say a true sage needs no name...
    if (special) { name = "";  }
}
  
  void drawMe() {
    stroke(0);
    
// draw the monster's name under its face
    textSize(size);
    text(name, xPosition - size, yPosition + size * 4);
  }
}
