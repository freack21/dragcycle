package kel3;

import javax.swing.*;

public class Window extends JFrame {

    public Window() {
        super();

        Panel panel = new Panel();
        this.setTitle("DragCycle");
        this.setIconImage(new ImageIcon("resources/icon.png").getImage());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.addKeyListener(panel.controller);
    }
}