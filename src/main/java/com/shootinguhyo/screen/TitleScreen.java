package com.shootinguhyo.screen;

import com.shootinguhyo.InputHandler;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * TitleScreen：タイトル画面のひな型クラス。
 *
 * 【役割】
 *  メニュー選択(NEW GAME / OPTION / QUIT 等)とBGM呼び出しを担当する画面。
 *
 * 【現状】
 *  GamePanel内のrenderTitle/updateTitleと役割がかぶる。
 *  将来的にこちらに移行する想定でひな型を用意。
 *
 * 【TODO】
 *  - 「→ NEW GAME / OPTION / QUIT」のメニュー実装
 *  - BGM再生(BgmPlayerを呼ぶ)
 *  - 背景アニメーション(キャラのドット絵を浮かべる等)
 */
public class TitleScreen implements Screen {
    private int frame = 0;
    private int menuIndex = 0;
    private final String[] menu = { "NEW GAME", "OPTION", "QUIT" };
    private Screen next;

    @Override
    public void onEnter() {
        frame = 0;
        menuIndex = 0;
        next = null;
        // TODO: BgmPlayer.play("title");
    }

    @Override
    public void update(InputHandler input) {
        frame++;
        if (input.isJustPressed(KeyEvent.VK_UP) || input.isJustPressed(KeyEvent.VK_W)) {
            menuIndex = (menuIndex - 1 + menu.length) % menu.length;
        }
        if (input.isJustPressed(KeyEvent.VK_DOWN) || input.isJustPressed(KeyEvent.VK_S)) {
            menuIndex = (menuIndex + 1) % menu.length;
        }
        if (input.isJustPressed(KeyEvent.VK_ENTER) || input.isJustPressed(KeyEvent.VK_Z)) {
            handleSelect();
        }
    }

    private void handleSelect() {
        switch (menuIndex) {
            case 0 -> next = new CharacterSelectScreen();
            case 1 -> next = new OptionScreen();
            case 2 -> {
                // TODO: アプリ終了処理 (System.exit(0) など)
            }
        }
    }

    @Override
    public Screen nextScreen() { return next; }

    @Override
    public void render(Graphics2D g, int width, int height) {
        g.setColor(new Color(5, 0, 20));
        g.fillRect(0, 0, width, height);

        g.setFont(new Font("SansSerif", Font.BOLD, 32));
        g.setColor(new Color(255, 200, 255));
        String title = "Shooting Uhyo";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (width - fm.stringWidth(title)) / 2, 130);

        // メニュー
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        for (int i = 0; i < menu.length; i++) {
            boolean selected = i == menuIndex;
            g.setColor(selected ? new Color(255, 240, 120) : new Color(180, 180, 220));
            String label = (selected ? "> " : "  ") + menu[i];
            fm = g.getFontMetrics();
            g.drawString(label, (width - fm.stringWidth(label)) / 2, 230 + i * 36);
        }

        // 操作ヒント
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(new Color(120, 120, 160));
        String hint = "Up/Down: select   Z/Enter: confirm";
        fm = g.getFontMetrics();
        g.drawString(hint, (width - fm.stringWidth(hint)) / 2, height - 20);
    }
}
