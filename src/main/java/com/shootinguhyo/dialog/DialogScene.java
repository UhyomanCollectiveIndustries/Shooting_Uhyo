package com.shootinguhyo.dialog;

import com.shootinguhyo.InputHandler;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * DialogScene：複数のDialogLineを順に表示する会話シーン。
 *
 * 【役割】
 *  ボス戦突入前後の「会話パート」を表示。
 *  Zキー/Enterで次のセリフへ。
 *
 * 【表示の仕組み】
 *  - 1文字ずつ表示する「タイプライター」演出
 *  - typedFrame でフレーム数を数えて、3フレームに1文字出す
 *  - 全部出し終わったら「次へ」プロンプトを表示
 *
 * 【TODO】
 *  - SE("typewriter"音)の再生
 *  - スキップ機能(Xキーで一気に表示)
 *  - 立ち絵の表情切替(怒り、笑い等)
 */
public class DialogScene {
    private final List<DialogLine> lines;
    private int currentIndex = 0;
    private int typedFrame = 0;
    private boolean finished = false;

    public DialogScene(List<DialogLine> lines) {
        this.lines = lines;
    }

    public boolean isFinished() { return finished; }

    public void update(InputHandler input) {
        if (finished) return;
        typedFrame++;

        DialogLine line = lines.get(currentIndex);
        int fullyShown = line.getText().length() * 3; // 1文字3フレーム

        // 次セリフへ進む or 全文一気表示
        if (input.isJustPressed(KeyEvent.VK_ENTER) || input.isJustPressed(KeyEvent.VK_Z)) {
            if (typedFrame < fullyShown) {
                typedFrame = fullyShown; // 一気に最後まで表示
            } else {
                advance();
            }
        }
    }

    private void advance() {
        currentIndex++;
        typedFrame = 0;
        if (currentIndex >= lines.size()) {
            finished = true;
        }
    }

    public void draw(Graphics2D g, int width, int height) {
        if (finished || lines.isEmpty()) return;

        DialogLine line = lines.get(currentIndex);

        // ダイアログボックス(画面下)
        int boxY = height - 110;
        g.setColor(new Color(0, 0, 30, 200));
        g.fillRect(10, boxY, width - 20, 90);
        g.setColor(new Color(120, 120, 200));
        g.drawRect(10, boxY, width - 20, 90);

        // 立ち絵(左右どちらかに描画)
        if (line.getPortrait() != null) {
            double px = (line.getSide() == DialogLine.Side.LEFT) ? 60 : (width - 60);
            line.getPortrait().draw(g, px, height / 2.0, 4);
        }

        // 話者名
        g.setColor(new Color(255, 220, 120));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString(line.getSpeakerName(), 24, boxY + 22);

        // セリフ本文(タイプライター)
        int shown = Math.min(line.getText().length(), typedFrame / 3);
        String visible = line.getText().substring(0, shown);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        // 改行処理(\nで折り返し)
        int textY = boxY + 44;
        for (String row : visible.split("\n")) {
            g.drawString(row, 24, textY);
            textY += 18;
        }

        // 「次へ」プロンプトを点滅表示
        if (shown >= line.getText().length()) {
            if ((typedFrame / 20) % 2 == 0) {
                g.setColor(new Color(200, 200, 255));
                g.setFont(new Font("Monospaced", Font.PLAIN, 10));
                g.drawString("> Z/Enter", width - 90, boxY + 80);
            }
        }
    }
}
