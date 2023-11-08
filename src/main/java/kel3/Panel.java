package kel3;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
// import java.util.TimerTask;
// import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

// import javafx.scene.media.*;

public class Panel extends JPanel implements ActionListener {

    final int WIDTH = 500;
    final int HEIGHT = 500;
    final int FPS = 1000/20;
    Bicycle bicyclePlayer;
    // Bicycle bicycleEnemy;
    Image background, standbyAuto;
    int bgX, bgY;

    Timer timer;
    int elapsedTime;
    Controller controller;

    int currFrame;
    int gaugeW, gaugeH;
    int posNotifX;
 
    static String filePath;
    Clip ups, perfect, standby;
    
    public Panel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.WHITE);
        standbyAuto = new ImageIcon("resources/auto.png").getImage();
        standbyAuto = standbyAuto.getScaledInstance(16, 16, Image.SCALE_DEFAULT);
        background = new ImageIcon("resources/bg-new.png").getImage();
        bgX = WIDTH - background.getWidth(null);
        bgY = HEIGHT - background.getHeight(null);
        bicyclePlayer = new Bicycle("bicycle.png", 200, 164);
        currFrame = 0;
        posNotifX = -1;
        gaugeH = 8;
        timer = new Timer(FPS, this);
        timer.start();
        elapsedTime = 0;
        controller = new Controller(bicyclePlayer);

        try {
            GraphicsEnvironment ge = 
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/ka1.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/digital-7.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/01 DigiGraphics.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/leaguespartan-bold.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/Cocogoose Pro Regular Trial.ttf")));

            filePath = "resources/cm.wav";
            Clip clip = addClip(filePath);
            clip.loop(-1);
            clip.start(); 
        } catch (IOException | FontFormatException | LineUnavailableException | UnsupportedAudioFileException e) {
            System.out.println("Failed load media and font!");
            System.out.println(e.getMessage());
        }
    }

    private Clip addClip(String filePath) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        Clip clip = AudioSystem.getClip();
        AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(filePath));
        clip.open(inputStream);
        return clip;
    }

    private Clip startSfx(String filePath, Clip clip) {
        try {
            if(clip == null) {
                clip = addClip("resources/"+filePath+".wav");
                clip.loop(0);
                clip.start();

                if(clip.getMicrosecondLength() == clip.getMicrosecondPosition()) {
                    clip.stop(); 
                    clip.close();
                    clip = null;
                }
            }
        } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
        return clip;
    }

    private void startUpsAudio() {
        ups = startSfx("ups", ups);
    }

    private void startPerfectAudio() {
        perfect = startSfx("ping", perfect);
    }

    private void startStandbyAudio() {
        standby = startSfx("standby", standby);
    }

    private int getPosNotifX(int textW) {
        if(posNotifX == -1) {
            posNotifX = WIDTH / 2 - textW / 2;
            if(bicyclePlayer.gX <= gaugeW / 2) {
                posNotifX -= bicyclePlayer.getWidth() + 8;
            } else {
                posNotifX += bicyclePlayer.getWidth() + 8;
            }
        }
        return posNotifX;
    }

    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        Image sprite = bicyclePlayer.getSprite(currFrame, 0);

        g2d.drawImage(background, bgX - (background.getWidth(null)), bgY, null);
        g2d.drawImage(background, bgX, bgY, null);

        g2d.drawImage(sprite, WIDTH / 2 - bicyclePlayer.getWidth() / 2, HEIGHT - bicyclePlayer.getHeight() - 4, null);

        if(!bicyclePlayer.hasStandby) {
            gaugeW = bicyclePlayer.getWidth() - 16;
            int gaugeX = WIDTH / 2 - gaugeW / 2;
            int gaugeY = HEIGHT - bicyclePlayer.getHeight() - gaugeH - 10;
            GradientPaint gp1 = new GradientPaint(0, 0, Color.white, gaugeW * 2 / 5, gaugeH, new Color(0x26c485), true);
            g2d.setPaint(gp1);
            g2d.fillRect(gaugeX, gaugeY, gaugeW, gaugeH);
            g2d.setColor((Color.lightGray));
            g2d.drawRect(gaugeX, gaugeY, gaugeW, gaugeH);
            if(bicyclePlayer.gX <= 8 || bicyclePlayer.gX >= gaugeW - 4 - 8) {
                g2d.setColor(new Color(0x000000));
            } else if(bicyclePlayer.gX <= 12 || bicyclePlayer.gX >= gaugeW - 4 - 12) {
                g2d.setColor(new Color(0x7ddcb6));
            } else if(bicyclePlayer.gX <= 32 || bicyclePlayer.gX >= gaugeW - 4 - 32) {
                g2d.setColor(new Color(0xbeedda));
            } else {
                g2d.setColor(Color.white);
            }
            g2d.fillRect(gaugeX + bicyclePlayer.gX, gaugeY - 4, 4, gaugeH + 8);
            g2d.setColor(new Color(0x222222));
            g2d.drawRect(gaugeX + bicyclePlayer.gX, gaugeY - 4, 4, gaugeH + 8);
        }

        Font kaFont = new Font("Karmatic Arcade", Font.PLAIN, 18);
        FontMetrics fm = g2d.getFontMetrics(kaFont);
        String textToWrite = "PRESS KEY_UP TO STANDBY!";
        int textW = fm.stringWidth(textToWrite);
        int textH = fm.getHeight();
        if(Math.abs(bicyclePlayer.gVelocity) >= 10 && !bicyclePlayer.hasStandby && bicyclePlayer.standbyChance != 0) {
            g2d.setFont(kaFont);
            g2d.setColor(new Color(0x061A40));
            // g2d.setColor(new Color(0x7743DB));
            g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, HEIGHT / 2 - 180 - textH / 2);
        }

        if(bicyclePlayer.hasStandby) {
            startStandbyAudio();
            textToWrite = String.valueOf(bicyclePlayer.standbyDuration);
            textW = fm.stringWidth(textToWrite);
            g2d.setFont(kaFont);
            g2d.setColor(new Color(0x061A40));
            // g2d.setColor(new Color(0x7743DB));
            g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, HEIGHT / 2 - 180 - textH / 2);

            kaFont = new Font("Karmatic Arcade", Font.PLAIN, 12);
            fm = g2d.getFontMetrics(kaFont);
            textToWrite = "NETRAL IN..";
            textW = fm.stringWidth(textToWrite);
            int textH2 = fm.getHeight();
            g2d.setFont(kaFont);
            g2d.setColor(new Color(0x061A40));
            // g2d.setColor(new Color(0x7743DB));
            g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, HEIGHT / 2 - 180 - textH - textH2 / 2 - 10);
        }

        Font d7Font = new Font("Digital-7", Font.PLAIN, 24);
        fm = g2d.getFontMetrics(d7Font);
        textToWrite = String.valueOf(Math.abs(bicyclePlayer.gVelocity));
        if(bicyclePlayer.getCurrPedal() == Bicycle.STOP) {
            textToWrite = "0";
        }
        textW = fm.stringWidth(textToWrite);
        textH = fm.getHeight();
        g2d.setFont(d7Font);
        g2d.setColor(new Color(0x000000));
        g2d.drawString(textToWrite, 8, HEIGHT - textH);
        d7Font = new Font("Digital-7", Font.PLAIN, 11);
        fm = g2d.getFontMetrics(d7Font);
        textToWrite = "px/s";
        g2d.setFont(d7Font);
        g2d.setColor(new Color(0x000000));
        g2d.drawString(textToWrite, (textW) + 8, HEIGHT - textH);

        d7Font = new Font("Digital-7", Font.PLAIN, 21);
        fm = g2d.getFontMetrics(d7Font);
        textToWrite = String.valueOf(Math.abs(bicyclePlayer.standbyChance));
        textW = fm.stringWidth(textToWrite);
        g2d.setFont(d7Font);
        g2d.setColor(new Color(0x000000));
        g2d.drawString(textToWrite, WIDTH - textW - 8, HEIGHT - 12);
        g2d.drawImage(standbyAuto, WIDTH - standbyAuto.getWidth(null) - textW - 8 - 4, HEIGHT - standbyAuto.getHeight(null) - 10, null);

        kaFont = new Font("Karmatic Arcade", Font.PLAIN, 12);
        fm = g2d.getFontMetrics(kaFont);
        textH = fm.getHeight();
        g2d.setFont(kaFont);
        int posNotifY = HEIGHT - 38;
        switch (bicyclePlayer.getStatusPedal()) {
            case Bicycle.PERFECT:
                textToWrite = "PERFECT!!";
                textW = fm.stringWidth(textToWrite);
                // g2d.setColor(new Color(0x008170));
                g2d.setColor(new Color(0x1A535C));
                g2d.drawString(textToWrite, getPosNotifX(textW), posNotifY);
                startPerfectAudio();
                if(elapsedTime % 750 == 0) {
                    bicyclePlayer.setStatusPedal(Bicycle.DEFAULT);
                    perfect = null;
                    posNotifX = -1;
                }
                break;

            case Bicycle.GOOD:
                textToWrite = "GOOD!!";
                textW = fm.stringWidth(textToWrite);
                g2d.setColor(new Color(0x3772FF));
                g2d.drawString(textToWrite, getPosNotifX(textW), posNotifY);
                if(elapsedTime % 750 == 0) {
                    bicyclePlayer.setStatusPedal(Bicycle.DEFAULT);
                    posNotifX = -1;
                }
                break;

            case Bicycle.NOT_GOOD:
                textToWrite = "NOT GOOD!!";
                textW = fm.stringWidth(textToWrite);
                g2d.setColor(new Color(0x6F1D1B));
                g2d.drawString(textToWrite, getPosNotifX(textW), posNotifY);
                if(elapsedTime % 750 == 0) {
                    bicyclePlayer.setStatusPedal(Bicycle.DEFAULT);
                    posNotifX = -1;
                }
                break;

            case Bicycle.WORST:
                textToWrite = "UPSS!!";
                textW = fm.stringWidth(textToWrite);
                g2d.setColor(new Color(0xA4031F));
                g2d.drawString(textToWrite, getPosNotifX(textW), posNotifY);
                startUpsAudio();
                if(elapsedTime % 750 == 0) {
                    bicyclePlayer.setStatusPedal(Bicycle.DEFAULT);
                    ups = null;
                    posNotifX = -1;
                }
                break;

            default:
                break;
        }
    }

    public void playFrameBicycle() {
        if(currFrame >= 2000 - 200) {
            currFrame = 0;
        } else {
            currFrame += 200;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        elapsedTime += FPS;

        if(bicyclePlayer.getCurrPedal() != Bicycle.STOP) {
            if(bicyclePlayer.getCurrPedal() != Bicycle.NETRAL) {
                int timeToPlay = 1;
                if(Math.abs(bicyclePlayer.gVelocity) <= 2) {
                    timeToPlay = 400;
                } else if(Math.abs(bicyclePlayer.gVelocity) <= 5) {
                    timeToPlay = 250;
                } else if(Math.abs(bicyclePlayer.gVelocity) <= 8) {
                    timeToPlay = 100;
                }
                if(elapsedTime % timeToPlay == 0) {
                    playFrameBicycle();
                }
            }

            bgX += Math.abs(bicyclePlayer.gVelocity);
            if(bicyclePlayer.gX >= gaugeW) {
                if(bicyclePlayer.getCurrPedal() == Bicycle.RIGHT) {
                    bicyclePlayer.gX += bicyclePlayer.gVelocity;
                } else {
                    bicyclePlayer.gX = gaugeW;
                    if((!bicyclePlayer.hasStandby)) {
                        int timeToSlow = 1000;
                        int speedToSlow = 1;
                        if(bicyclePlayer.gVelocity <= 5) {
                            timeToSlow = 500;
                        } else if(bicyclePlayer.gVelocity <= 10) {
                            timeToSlow = 250;
                        } else if(bicyclePlayer.gVelocity <= 15) {
                            timeToSlow = 100;
                            // speedToSlow = 2;
                        } else if(bicyclePlayer.gVelocity <= 20) {
                            timeToSlow = 50;
                            // speedToSlow = 3;
                        } else if(bicyclePlayer.gVelocity <= 30) {
                            timeToSlow = 25;
                            // speedToSlow = 3;
                        } else {
                            timeToSlow = 1;
                            // speedToSlow = 4;
                        }
                        if(elapsedTime % timeToSlow == 0) {
                            bicyclePlayer.gVelocity -= speedToSlow;
                            bicyclePlayer.setCurrPedal(Bicycle.NETRAL);
                        }
                    }
                }
            } else if(bicyclePlayer.gX <= -4) {
                if(bicyclePlayer.getCurrPedal() == Bicycle.LEFT) {
                    bicyclePlayer.gX += bicyclePlayer.gVelocity;
                } else {
                    bicyclePlayer.gX = -4;
                    if((!bicyclePlayer.hasStandby)) {
                        int timeToSlow = 1000;
                        int speedToSlow = 1;
                        if(bicyclePlayer.gVelocity >= -5) {
                            timeToSlow = 500;
                        } else if(bicyclePlayer.gVelocity >= -10) {
                            timeToSlow = 250;
                        } else if(bicyclePlayer.gVelocity >= -15) {
                            timeToSlow = 100;
                            // speedToSlow = 2;
                        } else if(bicyclePlayer.gVelocity >= -20) {
                            timeToSlow = 50;
                            // speedToSlow = 3;
                        } else if(bicyclePlayer.gVelocity >= -30) {
                            timeToSlow = 25;
                            // speedToSlow = 3;
                        } else {
                            timeToSlow = 1;
                            // speedToSlow = 4;
                        }
                        if(elapsedTime % timeToSlow == 0) {
                            bicyclePlayer.gVelocity += speedToSlow;
                            bicyclePlayer.setCurrPedal(Bicycle.NETRAL);
                        }
                    }
                }
            } else {
                bicyclePlayer.gX += bicyclePlayer.gVelocity;
            }

            if(bgX >= WIDTH) {
                bgX = WIDTH - background.getWidth(null);
            }
        }

        if(Math.abs(bicyclePlayer.gVelocity) <= 0) {
            bicyclePlayer.setCurrPedal(Bicycle.STOP);
            bicyclePlayer.gVelocity = 0;
        }

        if((bicyclePlayer.getCurrPedal()) == Bicycle.STANDBY) {
            if(bicyclePlayer.hasStandby) {
                if (elapsedTime % 1000 == 0) {
                    bicyclePlayer.standbyDuration--;

                    if(bicyclePlayer.standbyDuration < 0) {
                        bicyclePlayer.hasStandby = false;
                        standby = null;
                        bicyclePlayer.standbyDuration = 5;
                        bicyclePlayer.gVelocity *= 3;
                        bicyclePlayer.gVelocity /= 4;
                        bicyclePlayer.setCurrPedal(Bicycle.NETRAL);
                    }
                }
            }
        }

        if (elapsedTime % 1000 == 0) {
            elapsedTime = 0;
        }
        repaint();
    }
}
