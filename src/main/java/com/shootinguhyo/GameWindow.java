package com.shootinguhyo;

import javax.swing.*;

public class GameWindow extends JFrame {
    public GameWindow() {
        setTitle("Shooting Uhyo - \u5e7b\u60f3\u306e\u5996\u7cbe");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        gamePanel.startGame();
    }
}
