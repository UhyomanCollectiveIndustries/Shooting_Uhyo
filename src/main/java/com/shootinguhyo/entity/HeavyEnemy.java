package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.RadialPattern;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * HeavyEnemy：耐久力の高い大型敵(ひな型)。
 *
 * 【コンセプト】
 *  - HP高め(通常敵の3〜4倍)
 *  - ゆっくり進入してきて、大型の弾を多方向に撃つ
 *  - 倒すと大量のアイテムをドロップする想定
 *  - 画面に長く居座る「壁役」
 *  - いわゆる「中ボス」的な立ち位置の敵キャラのひな型
 *
 * 【既存Enemyとの違い】
 *  - サイズ、HP、得点が大きい
 *  - 弾サイズが MEDIUM
 *
 * 【TODO】
 *  - ドット絵スプライトでの描画に差し替え
 *  - 撃破時の専用爆発演出
 *  - 中ボス用の派生クラス(MidBoss)を作る
 */
public class HeavyEnemy extends Enemy {

    public HeavyEnemy(double x, double y) {
        super(x, y, 2000, 150000);
    }

    @Override
    public void update() {
        frame++;
        y += 0.4; // 通常敵より遅く進入
        x = startX + Math.sin(frame * 0.02) * 30;

        // 120フレームに1回、12方向の大型弾を撃つ
        if (frame % 120 == 60 && hp > 0) {
            RadialPattern rp = new RadialPattern(12, 2.5, frame * 0.05);
            newBullets.addAll(rp.generate(x, y, EnemyBullet.BulletSize.MEDIUM, new Color(255, 100, 100)));
        }

        if (y > 520) active = false;
    }

    @Override
    public void draw(Graphics2D g) {
        int r = 18; // 大きめ
        g.setColor(new Color(160, 40, 60));
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        g.setColor(new Color(220, 80, 100));
        g.fill(new Ellipse2D.Double(x - r * 0.7, y - r * 0.7, r * 1.4, r * 1.4));
        g.setColor(new Color(255, 200, 200));
        g.fill(new Ellipse2D.Double(x - r * 0.3, y - r * 0.3, r * 0.6, r * 0.6));
    }
}
