package kel3;

import java.awt.event.*;

public class Controller implements KeyListener {

    private Bicycle bicycle;
    private Panel parent;

    public Controller(Bicycle rBicycle, Panel rPanel) {
        bicycle = rBicycle;
        parent = rPanel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        // System.out.println(key);

        if(parent.state == Panel.PLAYING) {
            int gaugeW = bicycle.getWidth() - 16;
            // System.out.println(key);
            // System.out.println(bicycle.getCurrPedal());
            if((key == 37 || key == 65) && bicycle.getCurrPedal() != Bicycle.STANDBY) { //left
                // System.out.println("gass");
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
    
            if((key == 39 || key == 68) && bicycle.getCurrPedal() != Bicycle.STANDBY) { //right
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
    
            if((key == 38 || key == 87) && Math.abs(bicycle.gVelocity) >= 10) { //top
                if(!bicycle.hasStandby && bicycle.standbyChance >= 1) {
                    bicycle.setCurrPedal(Bicycle.STANDBY);
                    bicycle.hasStandby = true;
                    bicycle.standbyChance--;
                    bicycle.setStatusPedal(Bicycle.DEFAULT);
                }
            }
        } else if(parent.state == Panel.MENU) {
            if((key == 38 || key == 87)) {
                if(parent.menuState > 0) {
                    parent.menuState--;
                } else {
                    parent.menuState = parent.menuText.length - 1;
                }
            }
            if((key == 40 || key == 83)) {
                if(parent.menuState < parent.menuText.length - 1) {
                    parent.menuState++;
                } else {
                    parent.menuState = 0;
                }
            }
            if((key == 10 || key == 32)) {
                parent.state = parent.menuState + 2;
            }
        } else if(parent.state > Panel.PLAYING) {
            if((key == 38 || key == 87)) {
                if(parent.gameOverState > 0) {
                    parent.gameOverState--;
                } else {
                    parent.gameOverState = parent.gameOverText.length - 1;
                }
            }
            if((key == 40 || key == 83)) {
                if(parent.gameOverState < parent.gameOverText.length - 1) {
                    parent.gameOverState++;
                } else {
                    parent.gameOverState = 0;
                }
            }
            if((key == 10 || key == 32)) {
                if(parent.gameOverState == 0) {
                    parent.state = Panel.CHOOSE_DIFF;
                } else if(parent.gameOverState == 1) {
                    parent.state = Panel.MENU;
                } else if(parent.gameOverState == 2) {
                    parent.state = Panel.EXIT;
                } 
            }
        } else if(parent.state == Panel.EXIT) {
            if((key == 38 || key == 87)) {
                if(parent.exitState > 0) {
                    parent.exitState--;
                } else {
                    parent.exitState = parent.exitText.length - 1;
                }
            }
            if((key == 40 || key == 83)) {
                if(parent.exitState < parent.exitText.length - 1) {
                    parent.exitState++;
                } else {
                    parent.exitState = 0;
                }
            }
            if((key == 10 || key == 32)) {
                if(parent.exitState == 0) {
                    System.gc();
                    System.exit(0);
                } else if(parent.exitState == 1) {
                    parent.state = Panel.MENU;
                } 
            }
        } else if(parent.state == Panel.CHOOSE_DIFF) {
            if((key == 38 || key == 87)) {
                if(parent.diffState > 0) {
                    parent.diffState--;
                } else {
                    parent.diffState = parent.diffText.length - 1;
                }
            }
            if((key == 40 || key == 83)) {
                if(parent.diffState < parent.diffText.length - 1) {
                    parent.diffState++;
                } else {
                    parent.diffState = 0;
                }
            }
            if((key == 10 || key == 32)) {
                parent.state = Panel.PLAY;
            }
        } else if(parent.state == Panel.HELP || parent.state == Panel.ABOUT) {
            if((key == 10 || key == 32)) {
                parent.state = Panel.MENU;
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
