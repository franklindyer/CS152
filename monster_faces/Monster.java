class Monster {
  float xPosition, yPosition;
  float size;
  color monsterColor;
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
 
 // generate facial features
    eyes = new Eyes(size, xPosition, yPosition, special);
    mouth = new Mouth(size, xPosition, yPosition, special);
    name = new Name(size, xPosition, yPosition, special);
  }

// draw the face, plus each of its parts  
  void drawMe() {
    fill(monsterColor);
    ellipse(xPosition, yPosition, size, size);
    eyes.drawMe();
    mouth.drawMe();
    name.drawMe();
  }
  
// update the face dynamically, and each of its parts
  void updateMe() {
    eyes.updateMe();
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
  boolean special;

// randomize anchor and control points for bezier curve mouth  
  Mouth(float monsterSize, float monsterX, float monsterY, boolean isSpecial) {
    anchor1X = monsterX - monsterSize / 3 + random(monsterSize / 4);
    anchor2X = monsterX + monsterSize / 3 - random(monsterSize / 4);
    control1X = monsterX - monsterSize / 3 + random(monsterSize / 4);
    control2X = monsterX + monsterSize / 3 - random(monsterSize / 4)
    anchor1Y = monsterY + monsterSize / 3 - random(monsterSize / 6);
    anchor2Y = monsterY + monsterSize / 3 - random(monsterSize / 6);
    control1Y = monsterY + monsterSize / 3 - random(monsterSize / 6);
    control2Y = monsterY + monsterSize / 3 - random(monsterSize / 6);
    
    special = isSpecial;
    
    // they say a true sage is never unhappy...
    if (special) {
      anchor1X = monsterX - monsterSize / 3;
      control2X = monsterX + monsterSize / 3;
      control1X = monsterX - monsterSize / 4;
      anchor2X = monsterX + monsterSize / 5;
      anchor1Y = monsterY + monsterSize / 5;
      control2Y = monsterY + monsterSize / 5;
      control1Y = monsterY + monsterSize / 3;
      anchor2Y = monsterY + monsterSize / 3;
    }
  }
  
// draw mouth
  void drawMe() {
    noFill();
    bezier(anchor1X, anchor1Y, control1X, control1Y, anchor2X, anchor2Y, control2X, control2Y);
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
