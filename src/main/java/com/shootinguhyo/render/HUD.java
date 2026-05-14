package com.shootinguhyo.render;

import com.shootinguhyo.entity.Player;
import com.shootinguhyo.entity.Boss;

import java.awt.*;

public class HUD {
    private static final int HUD_X = 384;
    private static final int HUD_WIDTH = 192;
    private long hiScore = 0;

    public void update(long score) {
        if (score > hiScore) hiScore = score;
    }

    public void draw(Graphics2D g, Player player, Boss boss) {
        g.setColor(new Color(10, 5, 30));
        g.fillRect(HUD_X, 0, HUD_WIDTH, 448);
        g.setColor(new Color(100, 80, 150));
        g.drawLine(HUD_X, 0, HUD_X, 448);

        int x = HUD_X + 12;
        int y = 30;

        g.setFont(new Font("Monospaced", Font.PLAIN, 11));

        g.setColor(new Color(200, 180, 255));
        g.drawString("SCORE", x, y);
        g.setColor(Color.WHITE);
        g.drawString(String.format("%09d", player.getScore()), x, y + 16);

        y += 40;
        g.setColor(new Color(200, 180, 255));
        g.drawString("HI-SCORE", x, y);
        g.setColor(Color.WHITE);
        g.drawString(String.format("%09d", hiScore), x, y + 16);

        y += 40;
        g.setColor(new Color(200, 180, 255));
        g.drawString("PLAYER", x, y);
        g.setColor(new Color(255, 100, 100));
        StringBuilder lives = new StringBuilder();
        for (int i = 0; i < player.getLives(); i++) lives.append("\u2665");
        g.drawString(lives.toString(), x, y + 16);

        y += 40;
        g.setColor(new Color(200, 180, 255));
        g.drawString("BOMB", x, y);
        g.setColor(new Color(100, 200, 255));
        StringBuilder bombs = new StringBuilder();
        for (int i = 0; i < player.getBombs(); i++) bombs.append("\u25c6");
        g.drawString(bombs.toString(), x, y + 16);

        y += 40;
        g.setColor(new Color(200, 180, 255));
        g.drawString("POWER", x, y);
        g.setColor(Color.WHITE);
        g.drawString(String.format("%.2f", player.getPower() / 100.0), x, y + 16);

        y += 40;
        g.setColor(new Color(200, 180, 255));
        g.drawString("GRAZE", x, y);
        g.setColor(Color.WHITE);
        g.drawString(String.format("%04d", player.getGraze()), x, y + 16);

        if (boss != null && !boss.isDefeated()) {
            Boss.Phase phase = boss.getPhase();
            if (phase == Boss.Phase.SPELL1 || phase == Boss.Phase.SPELL2 || phase == Boss.Phase.SPELL3) {
                y += 50;
                int timerSec = boss.getSpellTimer() / 60;
                g.setColor(new Color(255, 220, 100));
                g.drawString("TIME", x, y);
                g.setColor(timerSec < 10 ? Color.RED : Color.WHITE);
                g.setFont(new Font("Monospaced", Font.BOLD, 18));
                g.drawString(String.format("%02d", timerSec), x, y + 22);
            }
        }
    }
}
