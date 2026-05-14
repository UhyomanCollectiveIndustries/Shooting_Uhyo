package com.shootinguhyo.effect;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/**
 * BossDefeatEffect：ボス撃破時の「派手な演出」のひな型。
 *
 * 【役割】
 *  ボスを倒した瞬間に、画面全体に閃光と複数の爆発リングを表示する。
 *  プレイヤーの達成感を高める演出。
 *
 * 【仕組み】
 *  - フレーム経過で複数のリングを広げる(同心円波紋)
 *  - 全体に白いフラッシュを乗せて時間経過でフェード
 *
 * 【TODO】
 *  - ボスの破片を飛ばす(Particleで多数生成)
 *  - 効果音(爆発音)再生
 *  - 一定時間時の流れを遅くするスローモーション
 */
public class BossDefeatEffect {
    private double cx, cy;
    private int frame = 0;
    private boolean active = false;
    private static final int DURATION = 180;

    public void activate(double cx, double cy) {
        this.cx = cx;
        this.cy = cy;
        this.frame = 0;
        this.active = true;
    }

    public boolean isActive() { return active; }

    public void update() {
        if (!active) return;
        frame++;
        if (frame > DURATION) active = false;
    }

    public void draw(Graphics2D g, int fieldWidth, int fieldHeight) {
        if (!active) return;

        // 全体のフラッシュ(最初の30フレームのみ)
        if (frame < 30) {
            float a = (30 - frame) / 30.0f;
            g.setColor(new Color(1.0f, 1.0f, 1.0f, a * 0.8f));
            g.fillRect(0, 0, fieldWidth, fieldHeight);
        }

        // 同心円(複数発を時間差で広げる)
        for (int i = 0; i < 4; i++) {
            int delay = i * 20;
            int local = frame - delay;
            if (local < 0 || local > 80) continue;
            double r = local * 4.0;
            float alpha = Math.max(0, (80 - local) / 80.0f);
            g.setColor(new Color(1.0f, 0.9f, 0.4f, alpha));
            g.draw(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        }
    }
}
