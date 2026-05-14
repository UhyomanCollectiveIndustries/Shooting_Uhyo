package com.shootinguhyo.screen;

import com.shootinguhyo.InputHandler;
import com.shootinguhyo.character.PlayerCharacter;
import com.shootinguhyo.character.Uhyoman;
import com.shootinguhyo.character.Uhyowoman;
import com.shootinguhyo.config.GameConfig;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

/**
 * CharacterSelectScreen：キャラクター選択画面のひな型。
 *
 * 【役割】
 *  左右キーでキャラを選び、Z/Enterで決定。
 *  選択されたキャラを GameConfig に格納してプレイ画面に渡す想定。
 *
 * 【TODO】
 *  - 立ち絵を大きく表示する
 *  - キャラ説明テキスト改行対応
 *  - プレイ画面への遷移処理(GamePanelとの統合)
 */
public class CharacterSelectScreen implements Screen {

    // 選べるキャラ一覧。新キャラが増えたらここに追加。
    private final List<PlayerCharacter> characters = Arrays.asList(
            new Uhyoman(),
            new Uhyowoman()
    );

    private int index = 0;
    private GameConfig config; // 決定したキャラを記録する場所
    private Screen next;

    public CharacterSelectScreen() {
        this(new GameConfig());
    }

    public CharacterSelectScreen(GameConfig config) {
        this.config = config;
    }

    @Override
    public void update(InputHandler input) {
        if (input.isJustPressed(KeyEvent.VK_LEFT) || input.isJustPressed(KeyEvent.VK_A)) {
            index = (index - 1 + characters.size()) % characters.size();
        }
        if (input.isJustPressed(KeyEvent.VK_RIGHT) || input.isJustPressed(KeyEvent.VK_D)) {
            index = (index + 1) % characters.size();
        }
        if (input.isJustPressed(KeyEvent.VK_ENTER) || input.isJustPressed(KeyEvent.VK_Z)) {
            config.setCharacter(characters.get(index));
            // TODO: PlayScreen(またはGamePanel)へ遷移する
            // next = new PlayScreen(config);
        }
        if (input.isJustPressed(KeyEvent.VK_ESCAPE) || input.isJustPressed(KeyEvent.VK_X)) {
            next = new TitleScreen();
        }
    }

    @Override
    public Screen nextScreen() { return next; }

    @Override
    public void render(Graphics2D g, int width, int height) {
        g.setColor(new Color(5, 0, 20));
        g.fillRect(0, 0, width, height);

        // タイトル
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.setColor(new Color(255, 220, 255));
        String title = "Select Character";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (width - fm.stringWidth(title)) / 2, 50);

        // 現在のキャラ
        PlayerCharacter cur = characters.get(index);

        // 立ち絵(ドット絵スプライト)を大きく描画(4倍)
        cur.getPortraitSprite().draw(g, width / 2.0, height / 2.0 - 20, 4);

        // 名前
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        fm = g.getFontMetrics();
        g.drawString(cur.getDisplayName(), (width - fm.stringWidth(cur.getDisplayName())) / 2,
                height - 120);

        // プロフィール (改行対応の簡易処理)
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(200, 200, 220));
        int yLine = height - 95;
        for (String line : cur.getProfile().split("\n")) {
            fm = g.getFontMetrics();
            g.drawString(line, (width - fm.stringWidth(line)) / 2, yLine);
            yLine += 18;
        }

        // 操作ヒント
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(new Color(120, 120, 160));
        String hint = "Left/Right: select   Z: confirm   X: back";
        fm = g.getFontMetrics();
        g.drawString(hint, (width - fm.stringWidth(hint)) / 2, height - 20);
    }
}
