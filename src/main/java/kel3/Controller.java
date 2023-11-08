package kel3;

import java.awt.event.*;

public class Controller implements KeyListener {

    private Bicycle bicycle;

    public Controller(Bicycle rBicycle) {
        bicycle = rBicycle;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        int gaugeW = bicycle.getWidth() - 16;
        // System.out.println(key);
        if(key == 37 && bicycle.getCurrPedal() != Bicycle.STANDBY) { //left
            if(bicycle.gVelocity == 0) {
                bicycle.setCurrPedal(Bicycle.LEFT);
                bicycle.gVelocity = 2;
                bicycle.setStatusPedal(Bicycle.DEFAULT);
            }

            if(bicycle.gX <= 8) {
                bicycle.setCurrPedal(Bicycle.LEFT);
                bicycle.gVelocity = Math.abs(bicycle.gVelocity);
                if(bicycle.getPrevPedal() == Bicycle.RIGHT) {
                    bicycle.gVelocity++;
                    bicycle.setStatusPedal(Bicycle.PERFECT);
                }
            } else if(bicycle.gX <= 12) {
                bicycle.gVelocity = Math.abs(bicycle.gVelocity);
                bicycle.setCurrPedal(Bicycle.NETRAL);
                bicycle.setStatusPedal(Bicycle.GOOD);
            } else if(bicycle.gX <= 32) {
                bicycle.gVelocity = Math.abs(bicycle.gVelocity);
                bicycle.gVelocity -= 2;
                // if(bicycle.gVelocity <= 2) bicycle.gVelocity = 2;
                bicycle.setCurrPedal(Bicycle.NETRAL);
                bicycle.setStatusPedal(Bicycle.NOT_GOOD);
            } else {
                bicycle.gVelocity /= 2;
                // if(bicycle.gVelocity <= 2) bicycle.gVelocity = 2;
                bicycle.setStatusPedal(Bicycle.WORST);
                bicycle.setCurrPedal(Bicycle.NETRAL);
            }
        }

        if(key == 39 && bicycle.getCurrPedal() != Bicycle.STANDBY) { //right
            if(bicycle.gVelocity == 0) {
                bicycle.setCurrPedal(Bicycle.RIGHT);
                bicycle.gVelocity = -2;
                bicycle.setStatusPedal(Bicycle.DEFAULT);
            }

            if(bicycle.gX >= gaugeW - 8 - 4) {
                bicycle.setCurrPedal(Bicycle.RIGHT);
                bicycle.gVelocity = Math.abs(bicycle.gVelocity) * -1;
                if(bicycle.getPrevPedal() == Bicycle.LEFT) {
                    bicycle.gVelocity--;
                    bicycle.setStatusPedal(Bicycle.PERFECT);
                }
            } else if(bicycle.gX >= gaugeW - 4 - 12) {
                bicycle.gVelocity = Math.abs(bicycle.gVelocity) * -1;
                bicycle.setCurrPedal(Bicycle.NETRAL);
                bicycle.setStatusPedal(Bicycle.GOOD);
            } else if(bicycle.gX >= gaugeW - 4 - 32) {
                bicycle.gVelocity = Math.abs(bicycle.gVelocity) * -1;
                bicycle.gVelocity += 2;
                // if(bicycle.gVelocity >= -2) bicycle.gVelocity = -2;
                bicycle.setCurrPedal(Bicycle.NETRAL);
                bicycle.setStatusPedal(Bicycle.NOT_GOOD);
            } else {
                bicycle.gVelocity /= 2;
                // if(bicycle.gVelocity >= -2) bicycle.gVelocity = -2;
                bicycle.setCurrPedal(Bicycle.NETRAL);
                bicycle.setStatusPedal(Bicycle.WORST);
            }
        }

        if(key == 38 && Math.abs(bicycle.gVelocity) >= 10) { //top
            if(!bicycle.hasStandby && bicycle.standbyChance >= 1) {
                bicycle.setCurrPedal(Bicycle.STANDBY);
                bicycle.hasStandby = true;
                bicycle.standbyChance--;
                bicycle.setStatusPedal(Bicycle.DEFAULT);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // throw new UnsupportedOperationException("Unimplemented method 'keyReleased'");
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // throw new UnsupportedOperationException("Unimplemented method 'keyTyped'");
    }
    
}
