package com.shootinguhyo.screen;

import com.shootinguhyo.InputHandler;
import com.shootinguhyo.config.Difficulty;
import com.shootinguhyo.config.GameOptions;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * OptionScreen：オプション画面のひな型。
 *
 * 【役割】
 *  難易度・音量・ヒットボックス表示などの設定を変更する。
 *  上下キーで項目選択、左右キーで値変更、X/ESCで戻る。
 *
 * 【TODO】
 *  - キーバインド変更機能
 *  - 全画面切替
 *  - 設定の即時保存(GameOptions.save())
 */
public class OptionScreen implements Screen {
    private final GameOptions options;
    private int index = 0;
    private Screen next;

    private static final String[] LABELS = {
            "Difficulty", "BGM Volume", "SE Volume", "Show Hitbox", "Back"
    };

    public OptionScreen() {
        this(new GameOptions());
    }

    public OptionScreen(GameOptions options) {
        this.options = options;
    }

    @Override
    public void update(InputHandler input) {
        if (input.isJustPressed(KeyEvent.VK_UP) || input.isJustPressed(KeyEvent.VK_W)) {
            index = (index - 1 + LABELS.length) % LABELS.length;
        }
        if (input.isJustPressed(KeyEvent.VK_DOWN) || input.isJustPressed(KeyEvent.VK_S)) {
            index = (index + 1) % LABELS.length;
        }
        if (input.isJustPressed(KeyEvent.VK_LEFT) || input.isJustPressed(KeyEvent.VK_A)) {
            changeValue(-1);
        }
        if (input.isJustPressed(KeyEvent.VK_RIGHT) || input.isJustPressed(KeyEvent.VK_D)) {
            changeValue(1);
        }
        if (input.isJustPressed(KeyEvent.VK_ENTER) || input.isJustPressed(KeyEvent.VK_Z)) {
            if (index == LABELS.length - 1) { // Back
                back();
            }
        }
        if (input.isJustPressed(KeyEvent.VK_ESCAPE) || input.isJustPressed(KeyEvent.VK_X)) {
            back();
        }
    }

    private void back() {
        options.save();
        next = new TitleScreen();
    }

    private void changeValue(int dir) {
        switch (index) {
            case 0 -> {
                Difficulty[] all = Difficulty.values();
                int cur = options.getDifficulty().ordinal();
                int newIdx = (cur + dir + all.length) % all.length;
                options.setDifficulty(all[newIdx]);
            }
            case 1 -> options.setBgmVolume(options.getBgmVolume() + dir * 5);
            case 2 -> options.setSeVolume(options.getSeVolume() + dir * 5);
            case 3 -> options.setShowHitbox(!options.isShowHitbox());
        }
    }

    @Override
    public Screen nextScreen() { return next; }

    @Override
    public void render(Graphics2D g, int width, int height) {
        g.setColor(new Color(5, 0, 20));
        g.fillRect(0, 0, width, height);

        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.setColor(new Color(255, 220, 255));
        String title = "Option";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (width - fm.stringWidth(title)) / 2, 60);

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        int startY = 130;
        for (int i = 0; i < LABELS.length; i++) {
            boolean selected = i == index;
            g.setColor(selected ? new Color(255, 240, 120) : new Color(180, 180, 220));
            String label = (selected ? "> " : "  ") + LABELS[i];
            g.drawString(label, 80, startY + i * 32);

            String value = currentValueText(i);
            if (value != null) {
                g.drawString(value, 240, startY + i * 32);
            }
        }

        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(new Color(120, 120, 160));
        String hint = "Up/Down: move   Left/Right: change   X: back";
        fm = g.getFontMetrics();
        g.drawString(hint, (width - fm.stringWidth(hint)) / 2, height - 20);
    }

    private String currentValueText(int i) {
        return switch (i) {
            case 0 -> "< " + options.getDifficulty().displayName + " >";
            case 1 -> "< " + options.getBgmVolume() + " >";
            case 2 -> "< " + options.getSeVolume() + " >";
            case 3 -> "< " + (options.isShowHitbox() ? "ON" : "OFF") + " >";
            default -> null;
        };
    }
}
