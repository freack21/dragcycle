package kel3;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class Panel extends JPanel implements ActionListener {

    public static final int WIDTH = 500;
    public static final int HEIGHT = 500;
    static final int FPS = 1000/20;
    public static final int SPLASH = 0, MENU = 1, CHOOSE_DIFF = 2, HELP = 3, ABOUT = 4, EXIT = 5, PLAY = 6, PLAYING = 7, PLAYER_WIN = 8, ENEMY_WIN = 9;
    int state = SPLASH;
    int menuState = 0, gameOverState = 0, exitState = 0, diffState = 0;
    int countdownPlay = 4;

    String[] menuText = {"play","help","about","exit"};
    String[] gameOverText = {"play again","go to menu","exit"};
    String[] exitText = {"yes","cancel"};
    String[] diffText = {"easy","medium","hard","impossible"};
    String[] helpText = {
        "KEY_LEFT or 'A' - Move pedal left",
        "KEY_RIGHT or 'D' - Move pedal right",
        "KEY_UP or 'W' - Standby",
    };
    String[] aboutText = {
        "Game Designer :",
        "    Fikri Rivandi, Yona Nadya, Adel Vanryo, Viera Adella",
        "Sound Designer :",
        "    Fikri Rivandi, Yona Nadya, Shadiq Widi, Zain M. Haqqo",
        "Programmer :",
        "    Fikri Rivandi",
        "Vendor :",
        "    Kelompok 3",
        "Publisher :",
        "    Kelompok 3",
        "Special Thanks :",
        "    Allah SWT., Ibu Noveri Lysbetti M S.T., M.Sc., TI-B 22",
    };

    Bicycle bicyclePlayer;
    Bicycle[] bicycleEnemy;
    int enemyCount = 3;
    int enemyBikeX[], playerBikeX, enemyBikeY[];

    Image background, standbyAuto;
    int bgX, bgY, finishLine;
    double distanceEnemy[], distancePlayer;

    Timer timer;
    long elapsedTime;
    Controller controller;

    int currFramePlayer, currFrameEnemy[], currFrameSplash, maxFrameSplash, startSplash, endSplash;
    int gaugeW, gaugeH;
    int posNotifX;
    Image splashImage;
    Image hand, finish;

    static String filePath;
    Clip ups, perfect, standby, bgSound, winClip, loseClip;

    Random random;

    public Panel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.WHITE);
        random = new Random();
        currFrameSplash = 0;
        maxFrameSplash = 50;
        startSplash = 0;
        endSplash = 1;

        standbyAuto = new ImageIcon("resources/auto.png").getImage();
        standbyAuto = standbyAuto.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        hand = new ImageIcon("resources/hand2.png").getImage();
        hand = hand.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
        finish = new ImageIcon("resources/finish.png").getImage();
        finish = finish.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
        background = new ImageIcon("resources/bg-new.png").getImage();
        posNotifX = -1;
        gaugeH = 8;

        bicyclePlayer = new Bicycle("bicycle.png", 200, 164);
        bicycleEnemy = new Bicycle[enemyCount];
        enemyBikeX = new int[enemyCount];
        enemyBikeY = new int[enemyCount];
        currFrameEnemy = new int[enemyCount];
        distanceEnemy = new double[enemyCount];
        for(int i=0; i<enemyCount;i++) {
            bicycleEnemy[i] = new Bicycle("bicycle1.png", 200, 164);
        }

        controller = new Controller(bicyclePlayer, this);

        initGame();

        try {
            GraphicsEnvironment ge = 
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/ka1.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/digital-7.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/01 DigiGraphics.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/leaguespartan-bold.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/Cocogoose Pro Regular Trial.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File("resources/Rubik.ttf")));

            BufferedImage img = ImageIO.read(new File("resources/unri.png"));
            splashImage = img.getSubimage(0, 0, img.getWidth(null), img.getHeight(null));
            splashImage = splashImage.getScaledInstance(180, 180, Image.SCALE_SMOOTH);

            filePath = "resources/cm.wav";
            bgSound = addClip(filePath);
        } catch (IOException | FontFormatException | LineUnavailableException | UnsupportedAudioFileException e) {
            System.out.println("Failed load media and font!");
            System.out.println(e.getMessage());
        }

        timer = new Timer(FPS, this);
        timer.start();
        elapsedTime = 0;
    }

    public void initGame() {
        finishLine = background.getWidth(null) * 2;
        bgX = WIDTH - finishLine;
        bgY = HEIGHT - background.getHeight(null);
        
        bicyclePlayer.setCurrPedal(Bicycle.STOP);
        bicyclePlayer.gVelocity = 2;
        bicyclePlayer.hasStandby = false;
        bicyclePlayer.setStatusPedal(Bicycle.DEFAULT);
        bicyclePlayer.standbyDuration = 5;
        bicyclePlayer.standbyChance = 5;
        bicyclePlayer.gX = 0;
        playerBikeX = WIDTH / 2 - bicyclePlayer.getWidth() / 2;
        distancePlayer = 0;
        currFramePlayer = 0;
        int gapEnemyX = (bicycleEnemy[0].getWidth() / 2);
        int gapEnemyY = 0;

        for(int i=0; i<enemyCount;i++) {
            bicycleEnemy[i].setCurrPedal(Bicycle.STOP);
            bicycleEnemy[i].gVelocity = 2;
            bicycleEnemy[i].hasStandby = false;
            bicycleEnemy[i].setStatusPedal(Bicycle.DEFAULT);
            bicycleEnemy[i].standbyDuration = 5;
            bicycleEnemy[i].standbyChance = 5;
            bicycleEnemy[i].gX = 0;

            if((i+1)%2 == 0) {
                gapEnemyX += (bicycleEnemy[i].getWidth() + 10);
                gapEnemyY = 0;
            } else {
                gapEnemyY = 24;
            }
            enemyBikeX[i] = WIDTH / 2 - bicycleEnemy[i].getWidth() + gapEnemyX;
            enemyBikeY[i] = HEIGHT - bicycleEnemy[i].getHeight() - 4 - gapEnemyY;
            distanceEnemy[i] = 0;
            currFrameEnemy[i] = 0;
        }

        // diffState = 0;

        winClip = null;
        loseClip = null;
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

    private void startWinAudio() {
        winClip = startSfx("win", winClip);
    }

    private void startLoseAudio() {
        loseClip = startSfx("lose", loseClip);
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

        if(state == SPLASH) {
            // g2d.setColor(new Color(0xFFFFFF));
            g2d.setColor(new Color(0x000000));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
            float alpha = ((float)currFrameSplash / (float)maxFrameSplash);
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
            g2d.setComposite(ac);
            // int maxW = splashImage.getWidth(null);
            // int maxH = splashImage.getHeight(null);
            // Image img = splashImage.getScaledInstance(maxW * currFrameSplash / maxFrameSplash, maxH * currFrameSplash / maxFrameSplash, Image.SCALE_DEFAULT);
            // System.out.println(maxW * currFrameSplash / maxFrameSplash);
            // System.out.println(maxH * currFrameSplash / maxFrameSplash);
            // g2d.drawImage(img, WIDTH / 2 - img.getWidth(null) / 2, HEIGHT / 2 - img.getHeight(null) / 2, null);
            g2d.drawImage(splashImage, WIDTH / 2 - splashImage.getWidth(null) / 2, HEIGHT / 2 - splashImage.getHeight(null) / 2, null);

            if(currFrameSplash >= maxFrameSplash) {
                if(elapsedTime % 1000 == 0) {
                    if(startSplash >= endSplash) {
                        state = MENU;
                        bgSound.loop(-1);
                        bgSound.start(); 
                    }
                    startSplash++;
                }
            } else {
                currFrameSplash++;
            }
        } else if(state == MENU || state > PLAYING || state == EXIT || state == HELP || state == ABOUT || state == CHOOSE_DIFF) {
            bicyclePlayer.setCurrPedal(Bicycle.STANDBY);
            for(int i=0; i<enemyCount;i++) {
                bicycleEnemy[i].setCurrPedal(Bicycle.STANDBY);
            }
            if(elapsedTime % 3000 == 0) {
                for(int i=0; i<enemyCount;i++) {
                    bicycleEnemy[i].gVelocity = 5 + random.nextInt(7);
                }
                bicyclePlayer.gVelocity = 5 + random.nextInt(7);
            }
        } else if(state == PLAY) {
            initGame();
        } else if(state == PLAYING) {
            if(elapsedTime % 2000 == 0) {
                int startVelocity = 7;
                int maxVelocity = 20;
                if(diffState == 0) {
                    startVelocity = 1;
                    maxVelocity = 11;
                } else if(diffState == 1) {
                    startVelocity = 2;
                } else if(diffState == 2) {
                    startVelocity = 5;
                    maxVelocity = 15;
                }
                for(int i=0; i<enemyCount;i++) {
                    bicycleEnemy[i].gVelocity = startVelocity + random.nextInt(maxVelocity);
                }
            }
        }

        if(state != 0) {
            Image sprite = bicyclePlayer.getSprite(currFramePlayer, 0);
            Image enemyBike[] = new Image[enemyCount];
            for(int i=0; i<enemyCount;i++) {
                enemyBike[i] = bicycleEnemy[i].getSprite(currFrameEnemy[i], 0);
            }

            for(int i = -1; i<=finishLine/background.getWidth(null); i++) {
                g2d.drawImage(background, bgX + background.getWidth(null) * i, bgY, null);
            }

            g2d.setColor(new Color(0x777777));
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f);
            g2d.setComposite(ac);
            for(int i=0; i<enemyCount;i++) {
                g2d.fillRoundRect(enemyBikeX[i] + 2, enemyBikeY[i] + bicycleEnemy[i].getHeight() - 4, bicycleEnemy[i].getWidth() - 4, 5, 4, 4);
            }
            g2d.fillRoundRect(playerBikeX + 2, HEIGHT - 8, bicyclePlayer.getWidth() - 4, 5, 4, 4);

            ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
            g2d.setComposite(ac);
            for(int i=0; i<enemyCount;i+=2) {
                g2d.drawImage(enemyBike[i], enemyBikeX[i], enemyBikeY[i], null);
            }

            for(int i=1; i<enemyCount;i+=2) {
                g2d.drawImage(enemyBike[i], enemyBikeX[i], enemyBikeY[i], null);
            }

            g2d.drawImage(sprite, playerBikeX, HEIGHT - bicyclePlayer.getHeight() - 4, null);

            if(state == MENU) {
                Font kaFont = new Font("Karmatic Arcade", Font.PLAIN, 24);
                FontMetrics fm = g2d.getFontMetrics(kaFont);
                String textToWrite = "DRAG-CYCLE";
                int textW = fm.stringWidth(textToWrite);
                int textH = fm.getHeight();
                g2d.setFont(kaFont);
                g2d.setColor(new Color(0xAA0022));
                g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, 16 + textH);

                kaFont = new Font("Karmatic Arcade", Font.PLAIN, 18);
                fm = g2d.getFontMetrics(kaFont);
                textH = fm.getHeight() + 10;
                int posAbsMenu = HEIGHT / 2 - (textH * menuText.length / 2);
                g2d.setFont(kaFont);
                
                for(int i = 0; i < menuText.length; i++) {
                    g2d.setColor(new Color(0x000000));
                    textToWrite = menuText[i];
                    textW = fm.stringWidth(textToWrite);

                    if(i == menuState) {
                        g2d.setColor(new Color(0xAA0033));
                        g2d.drawImage(hand, WIDTH / 2 + textW / 2 + 16, posAbsMenu + textH * i - textH / 2, null);
                    }
                    g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, posAbsMenu + textH * i);
                }
            }


            if(state == HELP) {
                Font kaFont = new Font("Karmatic Arcade", Font.PLAIN, 24);
                FontMetrics fm = g2d.getFontMetrics(kaFont);
                String textToWrite = "HELP";
                int textW = fm.stringWidth(textToWrite);
                int textH = fm.getHeight();
                g2d.setFont(kaFont);
                g2d.setColor(new Color(0xAA0022));
                g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, 16 + textH);

                kaFont = new Font("Rubik", Font.BOLD, 18);
                fm = g2d.getFontMetrics(kaFont);
                textH = fm.getHeight() + 10;
                int posAbsMenu = HEIGHT / 2 - (textH * helpText.length / 2);
                g2d.setColor(new Color(0x000000));
                g2d.setFont(kaFont);

                for(int i = 0; i < helpText.length; i++) {
                    textToWrite = helpText[i];
                    textW = fm.stringWidth(textToWrite);

                    g2d.drawString(textToWrite, 32, posAbsMenu + textH * i);
                }

                textToWrite = "Press ENTER to back to MENU";
                textW = fm.stringWidth(textToWrite);
                g2d.setColor(new Color(0xAA0000));
                g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, HEIGHT - 10);
            }


            if(state == ABOUT) {
                Font kaFont = new Font("Karmatic Arcade", Font.PLAIN, 24);
                FontMetrics fm = g2d.getFontMetrics(kaFont);
                String textToWrite = "ABOUT";
                int textW = fm.stringWidth(textToWrite);
                int textH = fm.getHeight();
                g2d.setFont(kaFont);
                g2d.setColor(new Color(0xAA0022));
                g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, 16 + textH);

                kaFont = new Font("Rubik", Font.BOLD, 16);
                fm = g2d.getFontMetrics(kaFont);
                textH = fm.getHeight() + 10;
                int posAbsMenu = HEIGHT / 2 - (textH * aboutText.length / 2) + 32;
                g2d.setColor(new Color(0x000000));
                g2d.setFont(kaFont);

                for(int i = 0; i < aboutText.length; i++) {
                    textToWrite = aboutText[i];
                    textW = fm.stringWidth(textToWrite);

                    g2d.drawString(textToWrite, 20, posAbsMenu + textH * i);
                }

                textToWrite = "Press ENTER to back to MENU";
                textW = fm.stringWidth(textToWrite);
                g2d.setColor(new Color(0xAA0000));
                g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, HEIGHT - 10);
            }


            if(state == CHOOSE_DIFF) {
                Font kaFont = new Font("Karmatic Arcade", Font.PLAIN, 24);
                FontMetrics fm = g2d.getFontMetrics(kaFont);
                String textToWrite = "CHOOSE DIFFICULTY";
                int textW = fm.stringWidth(textToWrite);
                int textH = fm.getHeight();
                g2d.setFont(kaFont);
                g2d.setColor(new Color(0xAA0022));
                g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, 16 + textH);

                Font kaFont1 = new Font("Karmatic Arcade", Font.PLAIN, 21);
                fm = g2d.getFontMetrics(kaFont1);
                int textH2 = fm.getHeight() + 10;
                textToWrite = "choose the difficult of enemy";
                textW = fm.stringWidth(textToWrite);

                kaFont = new Font("Karmatic Arcade", Font.PLAIN, 14);
                fm = g2d.getFontMetrics(kaFont);
                textH = fm.getHeight() + 10;

                int posAbsMenu = HEIGHT / 2 - (textH * diffText.length / 2);
                g2d.setFont(kaFont1);
                g2d.setColor(new Color(0x000033));
                g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, posAbsMenu);

                posAbsMenu += textH2 + 10;

                for(int i = 0; i < diffText.length; i++) {
                    g2d.setColor(new Color(0x000000));
                    g2d.setFont(kaFont);
                    textToWrite = diffText[i];
                    textW = fm.stringWidth(textToWrite);

                    if(i == diffState) {
                        g2d.setColor(new Color(0x990033));
                        g2d.drawImage(hand, WIDTH / 2 + textW / 2 + 16, posAbsMenu + textH * i - textH / 2, null);
                    }
                    g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, posAbsMenu + textH * i);
                }
            }


            if(state == PLAY) {
                Font kaFont = new Font("Karmatic Arcade", Font.PLAIN, 32);
                FontMetrics fm = g2d.getFontMetrics(kaFont);
                String textToWrite = String.valueOf(countdownPlay);
                if(countdownPlay <= 0) {
                    textToWrite = "GO!!";
                }
                int textW = fm.stringWidth(textToWrite);
                int textH = fm.getHeight();
                g2d.setFont(kaFont);
                g2d.setColor(new Color(0xAA0022));
                g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, HEIGHT / 2 - textH / 2);

                if(countdownPlay < 0) {
                    state = PLAYING;
                    for(int i=0; i<enemyCount;i++) {
                        bicycleEnemy[i].setCurrPedal(Bicycle.STANDBY);
                    }
                    countdownPlay = 4;
                } else {
                    if(elapsedTime % 1000 == 0) {
                        countdownPlay--;
                    }
                }
            }

            if(state == PLAYING) {
                // menggambar garis denah hingga finish
                float alpha = 0.5f;
                ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
                g2d.setComposite(ac);
                g2d.setColor(Color.white);
                g2d.fillRect(10, 10, WIDTH - 20, 10);
                alpha = 1f;
                ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
                g2d.setComposite(ac);
                g2d.setColor(Color.black);
                g2d.drawRect(10, 10, WIDTH - 20, 10);

                double posPlayerLine = (WIDTH - 10) - ((distancePlayer / finishLine) * (WIDTH - 20));
                double[] posEnemyLine = new double[enemyCount];
                for(int i=0; i<enemyCount;i++) {
                    posEnemyLine[i] = (WIDTH - 10) - ((distanceEnemy[i] / finishLine) * (WIDTH - 20));
                }

                // menggambar posisi pemain
                g2d.setColor(Color.red);
                g2d.fillRect((int)posPlayerLine, 8, 3, 14);
                g2d.setColor(Color.black);
                g2d.drawRect((int)posPlayerLine, 8, 3, 14);

                // menggambar posisi musuh
                for(int i=0; i<enemyCount;i++) {
                    g2d.setColor(Color.blue);
                    g2d.fillRect((int)posEnemyLine[i], 8, 3, 14);
                    g2d.setColor(Color.black);
                    g2d.drawRect((int)posEnemyLine[i], 8, 3, 14);
                }

                g2d.drawImage(finish, 5, 7, null);

                if(!bicyclePlayer.hasStandby) {
                    gaugeW = bicyclePlayer.getWidth() - 16;
                    int gaugeX = playerBikeX + bicyclePlayer.getWidth() / 2 - gaugeW / 2;
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
                    g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, HEIGHT / 2 - 180 - textH / 2);
                }
        
                if(bicyclePlayer.hasStandby) {
                    startStandbyAudio();
                    textToWrite = String.valueOf(bicyclePlayer.standbyDuration);
                    textW = fm.stringWidth(textToWrite);
                    g2d.setFont(kaFont);
                    g2d.setColor(new Color(0x061A40));
                    // g2d.setColor(new Color(0x7743DB));
                    g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, HEIGHT / 2 - 160 - textH / 2);
        
                    kaFont = new Font("Karmatic Arcade", Font.PLAIN, 12);
                    fm = g2d.getFontMetrics(kaFont);
                    textToWrite = "NETRAL IN..";
                    textW = fm.stringWidth(textToWrite);
                    int textH2 = fm.getHeight();
                    g2d.setFont(kaFont);
                    g2d.setColor(new Color(0x061A40));
                    // g2d.setColor(new Color(0x7743DB));
                    g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, HEIGHT / 2 - 160 - textH - textH2 / 2 - 10);
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

            if(state > PLAYING) {
                Font kaFont = new Font("Karmatic Arcade", Font.PLAIN, 24);
                FontMetrics fm = g2d.getFontMetrics(kaFont);
                String textToWrite = "";
                int textW = 0;
                int textH = fm.getHeight();
                if(state == ENEMY_WIN) {
                    g2d.setColor(new Color(0x000033));
                    textToWrite = "ENEMY WIN!! YOU LOSE!!";
                    startLoseAudio();
                } else if(state == PLAYER_WIN) {
                    g2d.setColor(new Color(0xBB1122));
                    textToWrite = "CONGRATS!! YOU WIN!!";
                    startWinAudio();
                }
                textW = fm.stringWidth(textToWrite);
                g2d.setFont(kaFont);
                g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, 16 + textH);

                kaFont = new Font("Karmatic Arcade", Font.PLAIN, 18);
                fm = g2d.getFontMetrics(kaFont);
                textH = fm.getHeight() + 10;
                int posAbsMenu = HEIGHT / 2 - (textH * gameOverText.length / 2);
                g2d.setFont(kaFont);

                for(int i = 0; i < gameOverText.length; i++) {
                    g2d.setColor(new Color(0x000000));
                    textToWrite = gameOverText[i];
                    textW = fm.stringWidth(textToWrite);

                    if(i == gameOverState) {
                        g2d.setColor(new Color(0xAA0033));
                        g2d.drawImage(hand, WIDTH / 2 + textW / 2 + 16, posAbsMenu + textH * i - textH / 2, null);
                    }
                    g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, posAbsMenu + textH * i);
                }
            }

            if(state == EXIT) {
                Font kaFont = new Font("Karmatic Arcade", Font.PLAIN, 24);
                FontMetrics fm = g2d.getFontMetrics(kaFont);
                String textToWrite = "EXIT";
                int textW = fm.stringWidth(textToWrite);
                int textH = fm.getHeight();
                g2d.setFont(kaFont);
                g2d.setColor(new Color(0xAA0022));
                g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, 16 + textH);

                Font kaFont1 = new Font("Karmatic Arcade", Font.PLAIN, 21);
                fm = g2d.getFontMetrics(kaFont1);
                int textH2 = fm.getHeight() + 10;
                textToWrite = "are you sure want to exit?";
                textW = fm.stringWidth(textToWrite);

                kaFont = new Font("Karmatic Arcade", Font.PLAIN, 14);
                fm = g2d.getFontMetrics(kaFont);
                textH = fm.getHeight() + 10;

                int posAbsMenu = HEIGHT / 2 - (textH * exitText.length / 2);
                g2d.setFont(kaFont1);
                g2d.setColor(new Color(0x000033));
                g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, posAbsMenu);

                posAbsMenu += textH2 + 10;
                
                for(int i = 0; i < exitText.length; i++) {
                    g2d.setColor(new Color(0x000000));
                    g2d.setFont(kaFont);
                    textToWrite = exitText[i];
                    textW = fm.stringWidth(textToWrite);

                    if(i == exitState) {
                        g2d.setColor(new Color(0x990033));
                        g2d.drawImage(hand, WIDTH / 2 + textW / 2 + 16, posAbsMenu + textH * i - textH / 2, null);
                    }
                    g2d.drawString(textToWrite, WIDTH / 2 - textW / 2, posAbsMenu + textH * i);
                }
            }
        }
    }

    public int playFrameBicycle(int currFrameSplash) {
        if(currFrameSplash >= 2000 - 200) {
            currFrameSplash = 0;
        } else {
            currFrameSplash += 200;
        }
        return currFrameSplash;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (elapsedTime >= Long.MAX_VALUE - 1) {
            elapsedTime = 0;
        }

        elapsedTime += FPS;

        if(state != SPLASH) {
            if(elapsedTime%500 == 0) {
                for(int i=0; i<enemyCount;i++) {
                    if(bicycleEnemy[i].gVelocity < 10) {
                        bicycleEnemy[i].gVelocity++;
                    }
                }
            }

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
                        currFramePlayer = playFrameBicycle(currFramePlayer);
                    }
                }

                bgX += Math.abs(bicyclePlayer.gVelocity);
                if(state == PLAYING) {
                    distancePlayer += Math.abs(bicyclePlayer.gVelocity);
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
                }
            }

            if(state == PLAYING) {
                if(distancePlayer >= finishLine) {
                    state = PLAYER_WIN;
                }
            }

            for(int i=0; i<enemyCount;i++) {
                if(bicycleEnemy[i].getCurrPedal() != Bicycle.STOP) {
                    if(bicycleEnemy[i].getCurrPedal() != Bicycle.NETRAL) {
                        int timeToPlay = 1;
                        if(Math.abs(bicycleEnemy[i].gVelocity) <= 2) {
                            timeToPlay = 400;
                        } else if(Math.abs(bicycleEnemy[i].gVelocity) <= 5) {
                            timeToPlay = 250;
                        } else if(Math.abs(bicycleEnemy[i].gVelocity) <= 8) {
                            timeToPlay = 100;
                        }
                        if(elapsedTime % timeToPlay == 0) {
                            currFrameEnemy[i] = playFrameBicycle(currFrameEnemy[i]);
                        }
                    }

                    enemyBikeX[i] += Math.abs(bicyclePlayer.gVelocity);
                    enemyBikeX[i] -= Math.abs(bicycleEnemy[i].gVelocity);

                    if(state == PLAYING)
                        distanceEnemy[i] += Math.abs(bicycleEnemy[i].gVelocity);
                }

                if(Math.abs(bicycleEnemy[i].gVelocity) <= 0) {
                    bicycleEnemy[i].setCurrPedal(Bicycle.STOP);
                    bicycleEnemy[i].gVelocity = 0;
                }

                if(state == PLAYING) {
                    if(distanceEnemy[i] >= finishLine) {
                        state = ENEMY_WIN;
                        break;
                    }
                }
            }

            if(Math.abs(bicyclePlayer.gVelocity) <= 0) {
                bicyclePlayer.setCurrPedal(Bicycle.STOP);
                bicyclePlayer.gVelocity = 0;
            }

            if(state == PLAYING) {
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
            }


            if(bgX >= WIDTH) {
                bgX = WIDTH - background.getWidth(null) * 3;
            }
        }

        // if (elapsedTime % 1000 == 0) {
        //     elapsedTime = 0;
        // }
        repaint();
    }
}
