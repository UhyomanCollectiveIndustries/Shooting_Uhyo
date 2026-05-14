package com.shootinguhyo.screen;

import com.shootinguhyo.InputHandler;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * StageClearScreen：1ステージクリア時の画面のひな型。
 *
 * 【役割】
 *  ステージ番号を表示し、ボーナス点を表示してから次ステージへ進む。
 *  最終ステージなら EndingScreen に遷移する。
 *
 * 【TODO】
 *  - ボーナス計算(残機ボーナス、ボムボーナス、グレイズボーナス)
 *  - 次ステージへの遷移
 *  - クリア演出(キャラクターの勝利モーション、SE)
 */
public class StageClearScreen implements Screen {
    private final int stageNo;
    private final long score;
    private final boolean lastStage;
    private int frame = 0;
    private Screen next;

    public StageClearScreen(int stageNo, long score, boolean lastStage) {
        this.stageNo = stageNo;
        this.score = score;
        this.lastStage = lastStage;
    }

    @Override
    public void update(InputHandler input) {
        frame++;
        if (frame > 120 && input.isJustPressed(KeyEvent.VK_ENTER)) {
            if (lastStage) {
                next = new EndingScreen(score);
            } else {
                // TODO: 次のステージへの遷移処理
                next = new TitleScreen();
            }
        }
    }

    @Override
    public Screen nextScreen() { return next; }

    @Override
    public void render(Graphics2D g, int width, int height) {
        g.setColor(new Color(0, 0, 30, 200));
        g.fillRect(0, 0, width, height);

        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.setColor(new Color(255, 220, 100));
        String msg = "STAGE " + stageNo + " CLEAR!";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2 - 30);

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(Color.WHITE);
        String scoreStr = "Score: " + score;
        fm = g.getFontMetrics();
        g.drawString(scoreStr, (width - fm.stringWidth(scoreStr)) / 2, height / 2 + 10);

        if (frame > 120) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(new Color(220, 220, 255));
            String hint = "Press ENTER to continue";
            fm = g.getFontMetrics();
            g.drawString(hint, (width - fm.stringWidth(hint)) / 2, height / 2 + 50);
        }
    }
}
