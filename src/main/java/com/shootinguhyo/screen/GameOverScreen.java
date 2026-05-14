package com.shootinguhyo.screen;

import com.shootinguhyo.InputHandler;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * GameOverScreen：ゲームオーバー画面のひな型。
 *
 * 【役割】
 *  残機0になった時に表示する。スコア・コンティニュー選択を予定。
 *
 * 【TODO】
 *  - コンティニュー選択(YES/NO)
 *  - リザルト表示(スコア・グレイズ数・撃破数)
 *  - SE/BGM切替
 */
public class GameOverScreen implements Screen {
    private final long finalScore;
    private int frame = 0;
    private Screen next;

    public GameOverScreen(long finalScore) {
        this.finalScore = finalScore;
    }

    @Override
    public void onEnter() {
        // TODO: BgmPlayer.play("gameover");
    }

    @Override
    public void update(InputHandler input) {
        frame++;
        if (frame > 120 && input.isJustPressed(KeyEvent.VK_ENTER)) {
            next = new TitleScreen();
        }
    }

    @Override
    public Screen nextScreen() { return next; }

    @Override
    public void render(Graphics2D g, int width, int height) {
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(0, 0, width, height);

        g.setFont(new Font("SansSerif", Font.BOLD, 36));
        g.setColor(new Color(255, 80, 80));
        String msg = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2 - 30);

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(Color.WHITE);
        String score = "Score: " + finalScore;
        fm = g.getFontMetrics();
        g.drawString(score, (width - fm.stringWidth(score)) / 2, height / 2 + 10);

        if (frame > 120) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(new Color(220, 220, 255));
            String hint = "Press ENTER to return to title";
            fm = g.getFontMetrics();
            g.drawString(hint, (width - fm.stringWidth(hint)) / 2, height / 2 + 50);
        }
    }
}
