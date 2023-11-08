package kel3;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.*;

public class Bicycle {

    private int width, height, currPedal, prevPedal;
    private int statusPedal;
    public static final int LEFT = -1, RIGHT = 1, NETRAL = 0, STOP = 2, STANDBY = 3;
    public static final int NOT_YET = -1, DEFAULT = 0, PERFECT = 1, GOOD = 2, NOT_GOOD = 3, WORST = 4;
    private BufferedImage img;
    private Image sprite;
    public int gX, gVelocity;
    public int standbyDuration, standbyChance;
    public boolean hasStandby;

    public Bicycle(String url, int width, int height) {
        this.width = width;
        this.height = height;
        currPedal = prevPedal = STOP;
        gVelocity = 2;
        hasStandby = false;
        statusPedal = DEFAULT;
        standbyDuration = 5;
        standbyChance = 5;
        gX = 0;
        try {
            img = ImageIO.read(new File("resources/"+url));
            sprite = img.getSubimage(0, 0, width, height);
            sprite = sprite.getScaledInstance(100, 82, Image.SCALE_DEFAULT);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Image getSprite(int x, int y) {
        sprite = img.getSubimage(x, y, width, height);
        sprite = sprite.getScaledInstance(100, 82, Image.SCALE_DEFAULT);
        return sprite;
    }

    public int getWidth() {
        return sprite.getWidth(null);
    }

    public int getHeight() {
        return sprite.getHeight(null);
    }

    public int getCurrPedal() {
        return currPedal;
    }

    public void setCurrPedal(int curr) {
        setPrevPedal(currPedal);
        currPedal = curr;
    }

    public int getPrevPedal() {
        return prevPedal;
    }
    
    public void setPrevPedal(int prev) {
        prevPedal = prev;
    }

    public int getStatusPedal() {
        return statusPedal;
    }

    public void setStatusPedal(int status) {
        statusPedal = status;
    }

}    
