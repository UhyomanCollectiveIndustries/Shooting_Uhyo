package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.RadialPattern;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/**
 * TankEnemy：高HP・低速の重装敵。
 *
 * 【コンセプト】
 *  動きはゆっくりだがHPが高く、定期的に多方向弾を放つ硬い敵。
 *  Stage2以降の中盤に登場予定。
 *
 * 【設計】
 *  既存のEnemyを継承して挙動の一部を上書きする。
 *  これにより、出現/撃破/弾発射のロジックは共通利用できる。
 *
 * 【TODO】
 *  - 専用ドット絵スプライト
 *  - 装甲ゲージを別途表示
 */
public class TankEnemy extends Enemy {

    public TankEnemy(double x, double y) {
        super(x, y, 800, 800); // HP=800、スコア=800
    }

    @Override
    public void update() {
        frame++;
        y += 0.3; // ゆっくり進む

        // 60フレームに1回、12方向弾
        if (frame % 60 == 0 && hp > 0) {
            RadialPattern pattern = new RadialPattern(12, 1.8, frame * 0.05);
            newBullets.addAll(pattern.generate(x, y,
                    EnemyBullet.BulletSize.MEDIUM, new Color(200, 80, 50)));
        }

        if (y > 500) active = false;
    }

    @Override
    public void draw(Graphics2D g) {
        int r = 14;
        // 外殻
        g.setColor(new Color(120, 50, 30));
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        // 内側
        g.setColor(new Color(200, 100, 80));
        g.fill(new Ellipse2D.Double(x - r * 0.6, y - r * 0.6, r * 1.2, r * 1.2));
        // 中心
        g.setColor(new Color(255, 200, 100));
        g.fill(new Ellipse2D.Double(x - 4, y - 4, 8, 8));
    }
}
