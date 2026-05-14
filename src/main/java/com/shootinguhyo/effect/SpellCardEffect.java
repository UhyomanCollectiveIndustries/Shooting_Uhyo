package com.shootinguhyo.effect;

import java.awt.*;

/**
 * SpellCardEffect：ボスの「スペルカード(必殺技)」開始時の画面演出。
 *
 * 【役割】
 *  - スペル開始時に画面全体を薄い色で染める
 *  - スペル名を画面上部に大きく表示し、2秒(120フレーム)かけてフェードアウト
 *
 * 【設計の考え方】
 *  GamePanelとBossの両方が触ると複雑になるので、演出専用の小さなクラスに分離。
 *  activate()/deactivate()で状態を切り替えられるだけのシンプルなオブジェクト。
 */
public class SpellCardEffect {
    private Color overlayColor;  // 画面全体に被せる色
    private String spellName;    // 表示するスペル名
    private boolean active;
    private int displayTimer;    // スペル名表示用の残り時間

    public SpellCardEffect() {
        this.active = false;
    }

    /**
     * 演出開始。色・名前を設定して2秒のフェード表示を開始する。
     */
    public void activate(Color color, String name) {
        this.overlayColor = color;
        this.spellName = name;
        this.active = true;
        this.displayTimer = 120; // 2秒(60FPS基準)
    }

    /** 演出を切る。スペル終了時に呼ばれる。 */
    public void deactivate() {
        this.active = false;
    }

    /** タイマーを1フレーム分減らす。 */
    public void update() {
        if (displayTimer > 0) displayTimer--;
    }

    public boolean isActive() { return active; }

    /**
     * 画面に被せる色と、スペル名テキストを描画。
     *
     * 【フェードロジック】
     *  - 残り時間が20フレーム以上なら alpha = 1.0(濃い)
     *  - 20フレーム未満なら displayTimer/20.0 で線形にフェードアウト
     *  これで「サッと出てしばらく表示、最後にスッと消える」自然な見せ方になる。
     */
    public void draw(Graphics2D g, int fieldWidth, int fieldHeight) {
        if (!active) return;

        // 画面全体に薄い色を被せる
        g.setColor(overlayColor);
        g.fillRect(0, 0, fieldWidth, fieldHeight);

        // スペル名のフェード文字
        float alpha = displayTimer > 20 ? 1.0f : displayTimer / 20.0f;
        g.setColor(new Color(1.0f, 1.0f, 1.0f, alpha));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(spellName);
        g.drawString(spellName, (fieldWidth - textW) / 2, 35); // 横中央揃え
    }
}
