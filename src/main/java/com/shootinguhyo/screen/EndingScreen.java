package com.shootinguhyo.screen;

import com.shootinguhyo.InputHandler;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * EndingScreen：全ステージクリア後のエンディング画面のひな型。
 *
 * 【役割】
 *  - エンディングテキストを流す
 *  - スタッフロール風の演出
 *  - 最終スコアを表示
 *  - END後にタイトル画面に戻る
 *
 * 【TODO】
 *  - テキストを上にスクロールさせる演出
 *  - キャラ別エンディングの分岐
 *  - 専用BGMの再生
 */
public class EndingScreen implements Screen {
    private final long finalScore;
    private int frame = 0;
    private Screen next;

    // 流すテキスト(キャラ別に切替予定)
    private static final String[] LINES = {
            "～ ENDING ～",
            "",
            "幻想郷に再び平穏が戻った...",
            "",
            "(あなたのエンディング演出をここに書く)",
            "",
            "Thanks for playing!"
    };

    public EndingScreen(long finalScore) {
        this.finalScore = finalScore;
    }

    @Override
    public void onEnter() {
        // TODO: BgmPlayer.play("ending");
    }

    @Override
    public void update(InputHandler input) {
        frame++;
        if (frame > 300 && input.isJustPressed(KeyEvent.VK_ENTER)) {
            next = new TitleScreen();
        }
    }

    @Override
    public Screen nextScreen() { return next; }

    @Override
    public void render(Graphics2D g, int width, int height) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        // テキストをゆっくり上にスクロール
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(new Color(220, 220, 255));
        int baseY = height + 20 - frame / 3;
        for (int i = 0; i < LINES.length; i++) {
            String line = LINES[i];
            FontMetrics fm = g.getFontMetrics();
            int x = (width - fm.stringWidth(line)) / 2;
            int y = baseY + i * 28;
            if (y > -20 && y < height + 20) {
                g.drawString(line, x, y);
            }
        }

        // ある程度待ってからスコアと操作ヒントを表示
        if (frame > 300) {
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            g.setColor(new Color(255, 220, 120));
            String s = "Final Score: " + finalScore;
            FontMetrics fm = g.getFontMetrics();
            g.drawString(s, (width - fm.stringWidth(s)) / 2, height / 2 + 60);

            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g.setColor(new Color(200, 200, 220));
            String hint = "Press ENTER to return to title";
            fm = g.getFontMetrics();
            g.drawString(hint, (width - fm.stringWidth(hint)) / 2, height - 30);
        }
    }
}
