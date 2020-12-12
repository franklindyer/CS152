/*****************************************
 * Assignment 10
 * Name:    Franklin Pezzuti Dyer
 * E-mail:  fpezzutidyer@unm.edu
 * Course:      CS 152 - Section 005
 * Submitted:    11/29/2020
 *
 * Move your character with the arrow keys.
 * Collect food to avoid starving (measured by the red bar at the bottom).
 * Some food items have special effects on the player.
 * Food tastes better when you're hungry - so the hungrier you are, the more points you gain from eating.
 * See how long you can survive, and how high you can get your score!
 ***********************************************/

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.*;

public class TargetGame extends Canvas implements KeyListener {

    static KeysDown keysDown;
    static int screenSize;
    static Color backgroundColor;

    static Player player;
    static int totalTargets;
    static int activeTargets;
    static int targetsCaptured;
    static int level;
    static int score;
    static Target[] targets;

    static float[][] targetDistribution;

    public static void main(String[] args) {
        TargetGame targetGame = new TargetGame();
        targetGame.setupScreen(targetGame);
        while (player.health > 0) {
            try {
                Thread.sleep(20);
            } catch (Exception e) {}
            targetGame.updateGame();
        }
    }

    // initialize TargetGame object
    TargetGame() {
        screenSize = 500;
        backgroundColor = new Color(166, 219, 142);
        player = new Player(screenSize);
        keysDown = new KeysDown();
        level = 0;
        targetDistribution = getLevelData();
        loadLevel();
    }

    // sets up the canvas and JFrame
    // @param canvas - the canvas on which to set up the game
    void setupScreen(TargetGame canvas) {
        JFrame frame = new JFrame("Target Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas.setSize(screenSize, screenSize);
        canvas.setBackground(backgroundColor);
        canvas.addKeyListener(canvas);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        canvas.requestFocusInWindow();
    }

    // loads the next level by resetting the play to normal (full health and no status effects) and generating a new queue of targets
    public static void loadLevel() {
        // increment the level number and set appropriate parameters for the level length/difficulty
        level += 1;
        player.health = 1;
        player.patience = 700 - (int)(80 * Math.sqrt(level));
        player.effectDuration = 0;
        targetsCaptured = 0;
        activeTargets = 1 + 2 * (int)(Math.sqrt(3 * level));
        totalTargets = 10 + 10 * level;
        targets = new Target[totalTargets];
        // create a bunch of random targets for the level
        for (int i = 0; i < totalTargets; i++) {
            targets[i] = new Target((int)(screenSize * Math.random()),
                                    (int)(screenSize * Math.random()),
                                    randomTargetType(level));
            if (i < activeTargets) targets[i].activate();
        }
    }

    // draws everything on the canvas (player, targets, level number and score)
    // @param g - the graphics object
    public void paint(Graphics g) {
        // draw active targets
        for (int i = 0; i < totalTargets; i++) {
            if (targets[i].activated) targets[i].drawMe(g, player);
        }
        // draw player
        player.drawMe(g);
        // keep track of the level and score in the upper left corner
        Font f = new Font("Serif", Font.PLAIN, 30);
        g.setFont(f);
        g.drawString(Integer.toString(level), 10, 30);
        f = new Font("Serif", Font.PLAIN, 20);
        g.setFont(f);
        g.drawString("Score: " + Integer.toString(score), 10, 60);
        // draw health bar
        g.setColor(Color.RED);
        g.fillRect(10, screenSize - 20, (int)(player.health * (screenSize - 20)), 10);
    }

    // update the game during each tick
    public void updateGame() {
        // change player velocity based on which keys are pressed
        // but reverse the velocity if the player has eaten a mushroom recently
        int vel_coeff = 1;
        if (player.statusEffect == 3) vel_coeff = -1;
        if (keysDown.up) player.yvel += vel_coeff * player.speed;
        if (keysDown.down) player.yvel += -vel_coeff * player.speed;
        if (keysDown.right) player.xvel += vel_coeff * player.speed;
        if (keysDown.left) player.xvel += -vel_coeff * player.speed;
        // update the player
        player.updateMe(screenSize);
        // update the active targets
        for (int i = 0; i < totalTargets; i++) {
            if (targets[i].activated) targets[i].updateMe(screenSize, player);
        }
        // check for collisions between player and targets
        collisionCheck();
        deployTargets();
        // repaint everything
        repaint();
    }

    // check if player has collided with any targets, and if so, increment the score and trigger target effects
    public void collisionCheck() {
        for (int i = 0; i < totalTargets; i++) {
            // only check activated targets
            if (targets[i].activated) {
                // check if player is touching target
                if (player.detectCollision(targets[i])) {
                    // trigger the "capture function" for the target, and increment the target count
                    score += 100 * (1 - player.health);
                    targets[i].capture(player);
                    targetsCaptured += 1;
                }
            }
        }
    }

    // if some targets have been captured, deploy more targets to keep the total number of active targets constant
    public void deployTargets() {
        // count the number of activated targets
        int activeTargetCount = 0;
        for (int i = 0; i < totalTargets; i++) {
            if (targets[i].activated) activeTargetCount += 1;
        }
        // if too few are activated, activate the next inactive one in the list
        boolean targetsRemaining = false;
        if (activeTargetCount < activeTargets) {
            for (int i = 0; i < totalTargets; i++) {
                if (!targets[i].activated && !targets[i].destroyed) {
                    targets[i].activate();
                    targetsRemaining = true;
                    break;
                }
            }
        }
        // if there aren't any targets left, move to the next level
        if (activeTargetCount == 0 && !targetsRemaining) loadLevel();
    }

    // given the number of the level, this function randomly generates
    // each type of target with an appropriate probability, so that
    // the levels increase in difficulty.
    // @param levelNum - the number of the level from which a target should be generated
    public static int randomTargetType(int levelNum) {
        float randomizer = (float)Math.random();
        float cumSum = 0;
        for (int i = 0; i < targetDistribution[levelNum - 1].length; i++) {
            cumSum += targetDistribution[levelNum - 1][i];
            if (randomizer < cumSum) return i;
        }
        return 0;
    }

    // gets data about the target types of each level from an external file
    public float[][] getLevelData() {
        Scanner scanner;
        String filepath = "src/levelData.txt";
        float[][] levelData;
        try {
            // count the number of lines in the file
            scanner = new Scanner(new File(filepath));
            int lines = 0;
            while (scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
                lines++;
            }
            // create a 2D array with that many rows
            levelData = new float[lines][];
            // loop through the lines of the file and store the probabilities in the 2D array
            int lineNum = 0;
            scanner = new Scanner(new File(filepath));
            while (scanner.hasNextLine()) {
                String[] floatStrings = scanner.nextLine().split(" ");
                levelData[lineNum] = new float[floatStrings.length];
                for (int i = 0; i < floatStrings.length; i++) levelData[lineNum][i] = Float.parseFloat(floatStrings[i]);
                lineNum++;
            }
            return levelData;
        } catch (IOException e) {
            System.out.println("No file containing level data was found.");
            float[][] defaultData = {{0f, 0f, 1f}};
            return defaultData;
        }
    }

    // determine which keys have been released and update the keysUp object accordingly
    // supports both arrow keys and WASD
    // @param e - the key event that occurred
    public void keyReleased(KeyEvent e) {
        int keycode = e.getKeyCode();
        if (keycode == 39 || keycode == 68) {
            keysDown.right = false;
        } else if (keycode == 37 || keycode == 65) {
            keysDown.left = false;
        } else if (keycode == 40 || keycode == 83) {
            keysDown.down = false;
        } else if (keycode == 38 || keycode == 87) {
            keysDown.up = false;
        }
    }

    // determine which keys have been pressed and update the keysDown object accordingly
    // supports both arrow keys and WASD
    // @param e - the key event that occurred
    public void keyPressed(KeyEvent e) {
        int keycode = e.getKeyCode();
        if (keycode == 39 || keycode == 68) {
            keysDown.right = true;
        } else if (keycode == 37 || keycode == 65) {
            keysDown.left = true;
        } else if (keycode == 40 || keycode == 83) {
            keysDown.down = true;
        } else if (keycode == 38 || keycode == 87) {
            keysDown.up = true;
        }
    }

    // unused
    public void keyTyped(KeyEvent e) {}

}

class Player {

    float xpos, ypos;
    float xvel, yvel;
    float friction;
    float speed;
    float radius;
    float xRadius;
    float yRadius;
    Color bodyColor;
    Color eyeColor;
    float health;           // health will slowly decrease over time, but can be replenished by targets
    int patience;
    int statusEffect;       // the player can have different status effects, with 0 representing no effects
        // 1 = on fire, decreases friction
        // 2 = brain freeze, decreases speed
        // 3 = high on mushrooms, reverses directions
    int effectDuration;     // counts down the time left for the current status effect

    // constructor for the player object
    // @param screenSize - the size of the screen, used to place the player at the center
    Player(int screenSize) {
        xpos = screenSize / 2;
        ypos = screenSize / 2;
        xvel = 0;
        yvel = 0;
        friction = 0.1f;
        speed = 1f;
        radius = 25;
        xRadius = 25;
        yRadius = 25;
        bodyColor = Color.YELLOW;
        eyeColor = Color.BLACK;
        health = 1;
        statusEffect = 0;
        effectDuration = 0;
        patience = 1000;
    }

    // draws the player
    // @param g - the graphics object
    public void drawMe(Graphics g) {
        // body
        g.setColor(bodyColor);
        g.fillOval((int)(xpos - xRadius),
                   (int)(ypos - yRadius),
                   (int)(2 * xRadius),
                   (int)(2 * yRadius));
        // eyes
        float eyeRadius = Math.min(xRadius, yRadius) / 5;
        g.setColor(eyeColor);
        g.fillOval((int)(xpos - xRadius / 3 - eyeRadius + xvel),
                   (int)(ypos - yRadius / 3 - eyeRadius - yvel),
                   (int)(2 * eyeRadius),
                   (int)(2 * eyeRadius));
        g.fillOval((int)(xpos + xRadius / 3 - eyeRadius + xvel),
                   (int)(ypos - yRadius / 3 - eyeRadius - yvel),
                   (int)(2 * eyeRadius),
                   (int)(2 * eyeRadius));
        // mouth
        g.setColor(Color.BLACK);
        g.fillArc((int)(xpos - xRadius / 3),
                (int)(ypos),
                (int)(2 * xRadius / 3 + 20 * Math.abs(xvel) / (Math.abs(xvel) + 20)),
                (int)(yRadius / 2 + 20 * Math.abs(yvel) / (Math.abs(yvel) + 20)),
                0, -180);
        // nose
        g.setColor(Color.BLACK);
        g.fillRect((int)(xpos - xRadius/12 + xvel),
                (int)(ypos - yRadius/3 - yvel),
                (int)(xRadius/6),
                (int)(yRadius/2));
    }

    // given a target, determine whether the player is colliding with it
    // @param target - the target with which to detect collision
    public boolean detectCollision(Target target) {
        double squishyNorm = Math.pow(xpos - target.xpos, 2) / Math.pow(xRadius + target.radius, 2)
                             + Math.pow(ypos - target.ypos, 2) / Math.pow(yRadius + target.radius, 2);
        return (squishyNorm < 1);
    }

    // update the player during each tick
    // @param screenSize - the size of the screen, used to make the player loop around the edges
    public void updateMe(int screenSize) {
        // increment xy-position based on velocity
        xpos += xvel;
        ypos += -yvel;
        // decrease velocity based on friction
        xvel = (1 - friction) * xvel;
        yvel = (1 - friction) * yvel;
        // wrap around the screen when the player crosses the edges
        if (xpos < 0) xpos = screenSize;
        if (xpos > screenSize) xpos = 0;
        if (ypos < 0) ypos = screenSize;
        if (ypos > screenSize) ypos = 0;
        // update "squishiness" for the cute animation
        xRadius = radius - 20 * Math.abs(yvel) / (Math.abs(yvel) + 20);
        yRadius = radius - 20 * Math.abs(xvel) / (Math.abs(xvel) + 20);
        // decrement health due to hunger
        health += - 1 / (float)(patience);
        // apply status effects
        applyStatusEffects();
        // allow status effects to expire
        if (effectDuration == 0) {
            statusEffect = 0;
        } else {
            effectDuration += -1;
        }
    }

    // give the player a certain status effect for a certain duration
    // @param type - the ID number of the desired status effect
    // @param duration - the duration of the status effect, in ticks
    public void setStatusEffect(int type, int duration) {
        effectDuration = duration;
        statusEffect = type;
    }

    // implement the mechanics of different status effects
    public void applyStatusEffects() {
        // normal, no status effect
        if (statusEffect == 0) {
            bodyColor = Color.YELLOW;
            eyeColor = Color.BLACK;
            friction = 0.1f;
            speed = 1f;
        // spicy, increased speed/decreased friction
        } else if (statusEffect == 1) {
            xRadius += 10 * Math.random();
            yRadius += 10 * Math.random();
            bodyColor = new Color(229, 107, 40);
            eyeColor = Color.WHITE;
            friction = 0.02f;
        // brain freeze, decreased speed
        } else if (statusEffect == 2) {
            bodyColor = new Color(40, 154, 163);
            speed = 0.2f;
        // trippin', crazy color change
        } else if (statusEffect == 3) {
            bodyColor = new Color((int)(255 * Math.random()), (int)(255 * Math.random()), (int)(255 * Math.random()));
            eyeColor = new Color((int)(255 * Math.random()), (int)(255 * Math.random()), (int)(255 * Math.random()));
        }
    }

}

// targets for the player to capture
class Target {

    float xpos, ypos;
    float xvel, yvel;
    float radius;
    int type;               // there are various types of targets, with different special effects
    boolean activated;
    boolean destroyed;

    // target constructor
    // @param x - the x position of the new target
    // @param y - the y position of the new target
    // @param typeNum - the ID number of the type of the new target
    Target(int x, int y, int typeNum) {
        // initialize default values
        xpos = x;
        ypos = y;
        xvel = 0;
        yvel = 0;
        radius = 15;
        type = typeNum;
        activated = false;
        destroyed = false;
        // type-specific values
        if (type == 0 || type == 3) {
            xvel = 3 * (float)Math.random();
            yvel = 3 * (float)Math.random();
        }
    }

    public void capture(Player player) {
        destroyed = true;
        activated = false;
        // normal apple
        if (type == 0) {
            player.health = Math.min(1, player.health + 0.07f);
        // living apple
        } else if (type == 1) {
            player.health = Math.min(1, player.health + 0.1f);
        // hot pepper
        } else if (type == 2) {
            player.health = Math.min(1, player.health + 0.05f);
            player.setStatusEffect(1, 150);
        // sneaky apple
        } else if (type == 3) {
            player.health = Math.min(1, player.health + 0.1f);
        // magic mushroom
        } else if (type == 4) {
            player.health = Math.min(1, player.health + 0.05f);
            player.setStatusEffect(3, 300);
        // bomb
        } else if (type == 5) {
            player.health = player.health / 2;
            float xDelta = player.xpos - xpos;
            float yDelta = - player.ypos + ypos;
            float distance = (float)Math.sqrt(Math.pow(xDelta, 2f) + Math.pow(yDelta, 2f));
            float newXvel = 100 * xDelta / distance;
            float newYvel = 100 * yDelta / distance;
            player.xvel = newXvel;
            player.yvel = newYvel;
        }

    }

    // activates the target
    // (recall that a level's targets are generated in advance, and kept inactive in a queue until they are needed)
    public void activate() {
        activated = true;
    }

    // draws the target
    // @param g - the graphics object
    // @param player - the player
    public void drawMe(Graphics g, Player player) {
        Color c = Color.BLACK;
        float xDelta = player.xpos - xpos;
        float yDelta = - player.ypos + ypos;
        float distance = (float)Math.sqrt(Math.pow(xDelta, 2f) + Math.pow(yDelta, 2f));
        // plain vanilla target, stationary
        if (type == 0) {
            c = new Color(220, 62, 62);
            g.setColor(c);
            g.fillOval((int)(xpos - radius),
                       (int)(ypos - radius),
                       (int)(2 * radius),
                       (int)(2 * radius));
            // c = new Color(125, 77, 29);
            c = new Color(52, 127, 26);
            g.setColor(c);
            g.fillRect((int)(xpos - 2),
                       (int)(ypos - 3 * radius / 2),
                      4,
                       (int)(radius));
        // moving target
        } else if (type == 1) {
            c = new Color(212, 106, 73);
            g.setColor(c);
            g.fillOval((int) (xpos - radius),
                    (int) (ypos - radius),
                    (int) (2 * radius),
                    (int) (2 * radius));
            g.setColor(Color.BLACK);
            g.fillOval((int) (xpos - radius / 3 - radius / 8 + (radius / 4) * (xDelta / distance)),
                    (int) (ypos - radius / 4 + (radius / 4) * (-yDelta / distance)),
                    (int) (radius / 4),
                    (int) (radius / 4));
            g.fillOval((int) (xpos + radius / 3 - radius / 8 + (radius / 4) * (xDelta / distance)),
                    (int) (ypos - radius / 4 + (radius / 4) * (-yDelta / distance)),
                    (int) (radius / 4),
                    (int) (radius / 4));
            g.fillArc((int) (xpos - radius / 2),
                    (int) (ypos + radius / 4),
                    (int) (radius),
                    (int) (radius / 2),
                    180, 180);
            c = new Color(52, 127, 26);
            g.setColor(c);
            g.fillRect((int) (xpos - 2),
                    (int) (ypos - 3 * radius / 2),
                    4,
                    (int) (radius));
        // spicy red pepper
        } else if (type == 2) {
            c = new Color(116, 154, 33);
            g.setColor(c);
            g.fillRect((int) (xpos - radius / 2),
                    (int) (ypos - 3 * radius / 2),
                    4,
                    (int) (radius));
            c = new Color(246, 78, 11);
            g.setColor(c);
            g.fillPolygon(new int[]{(int) (xpos - 3 * radius / 4), (int) (xpos), (int) (xpos + radius)},
                    new int[]{(int) (ypos - 2 * radius / 3), (int) (ypos - radius), (int) (ypos + radius)},
                    3);
        // runner in disguise
        } else if (type == 3) {
            c = new Color(220, 62, 62);
            g.setColor(c);
            g.fillOval((int) (xpos - radius),
                    (int) (ypos - radius),
                    (int) (2 * radius),
                    (int) (2 * radius));
            c = new Color(52, 127, 26);
            g.setColor(c);
            g.fillRect((int) (xpos - 2),
                    (int) (ypos - 3 * radius / 2),
                    4,
                    (int) (radius));
            if (distance < 110) {
                g.setColor(Color.BLACK);
                g.fillOval((int) (xpos - radius / 3 - radius / 8 + (radius / 4) * (xDelta / distance)),
                        (int) (ypos - radius / 4 + (radius / 4) * (-yDelta / distance)),
                        (int) (radius / 4),
                        (int) (radius / 4));
                g.fillOval((int) (xpos + radius / 3 - radius / 8 + (radius / 4) * (xDelta / distance)),
                        (int) (ypos - radius / 4 + (radius / 4) * (-yDelta / distance)),
                        (int) (radius / 4),
                        (int) (radius / 4));
                g.fillArc((int) (xpos - radius / 2),
                        (int) (ypos + radius / 4),
                        (int) (radius),
                        (int) (radius / 2),
                        180, 180);
            }
        // magic mushroom
        } else if (type == 4) {
            c = new Color(156, 111, 75);
            g.setColor(c);
            g.fillRect((int)(xpos - radius/4),
                    (int)(ypos - radius/3),
                    (int)(radius/2),
                    (int)(4 * radius / 3));
            g.fillArc((int)(xpos - radius/2),
                    (int)(ypos - radius),
                    (int)(radius),
                    (int)(3 * radius / 2),
                    0, 180);
        // bomb that blasts the player away
        } else if (type == 5) {
            c = Color.BLACK;
            g.setColor(c);
            g.fillOval((int)(xpos - radius),
                    (int)(ypos - radius),
                    (int)(2 * radius),
                    (int)(2 * radius));
        }
    }

    // updates the target during each tick
    // @param screenSize - the size of the screen, used to make targets loop around the edges
    // @param player - the player object
    public void updateMe(int screenSize, Player player) {
        // increment position by velocity
        xpos += xvel;
        ypos += yvel;
        // wrap around the screen
        if (xpos < 0) xpos = screenSize;
        if (xpos > screenSize) xpos = 0;
        if (ypos < 0) ypos = screenSize;
        if (ypos > screenSize) ypos = 0;

        float xDelta = player.xpos - xpos;
        float yDelta = - player.ypos + ypos;
        float distance = (float)Math.sqrt(Math.pow(xDelta, 2f) + Math.pow(yDelta, 2f));

        // living apple: run away from the player if it gets too close
        if (type == 1) {
            if (distance < 110) {
                xvel += (-xDelta / distance) * 0.8f;
                yvel += (yDelta / distance) * 0.8f;
            } else {
                xvel += (xDelta / distance) * 0.1f;
                yvel += (-yDelta / distance) * 0.1f;
            }
            xvel = xvel * 0.95f;
            yvel = yvel * 0.95f;
        // spicy pepper: jitter around crazily
        } else if (type == 2) {
            xvel = 4f * (float)(Math.random() - 0.5);
            yvel = 4f * (float)(Math.random() - 0.5);
        // sneaky apple: run away from the player if it gets too close
        } else if (type == 3) {
            if (distance < 110) {
                xvel += (-xDelta / distance) * 0.5f;
                yvel += (yDelta / distance) * 0.5f;
            }
            if (xvel > 0.3) xvel = xvel * 0.95f;
            if (yvel > 0.3) yvel = yvel * 0.95f;
        }
    }

}

class KeysDown {

    boolean up = false;
    boolean down = false;
    boolean left = false;
    boolean right = false;

}
