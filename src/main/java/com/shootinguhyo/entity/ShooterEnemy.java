package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.AimedPattern;

import java.awt.*;
import java.awt.geom.Path2D;

/**
 * ShooterEnemy：射撃に特化した中型敵(ひな型)。
 *
 * 【コンセプト】
 *  - 画面上部の決まった位置で停止し、自機狙い弾を断続的に撃つ
 *  - 通常敵より動きが少ない代わりに、攻撃間隔が短く避けにくい
 *
 * 【動き方】
 *  - 指定したstopY座標まで降りてきて停止
 *  - 停止後は左右に短く揺れる
 *  - playerX,playerY を外から教えてもらい、それを狙って撃つ
 *
 * 【TODO】
 *  - ドット絵スプライトに差し替え
 *  - 攻撃パターンを複数(直線、扇、ばら撒き)からランダム選択
 *  - 撃破時のドロップ強化
 */
public class ShooterEnemy extends Enemy {

    private final double stopY;
    private boolean stopped = false;
    private double playerX, playerY;

    public ShooterEnemy(double x, double stopY) {
        super(x, -20, 250, 350);
        this.stopY = stopY;
    }

    /** 自機座標を教えてもらう(GamePanel側で毎フレーム呼ぶ想定)。 */
    public void setPlayerPosition(double px, double py) {
        this.playerX = px;
        this.playerY = py;
    }

    @Override
    public void update() {
        frame++;
        if (!stopped) {
            y += 1.2;
            if (y >= stopY) { stopped = true; y = stopY; }
        } else {
            // 停止後は小刻みに揺れる
            x = startX + Math.sin(frame * 0.05) * 15;
        }

        // 停止中は60フレームに1回、自機狙い3way
        if (stopped && frame % 60 == 0 && hp > 0) {
            AimedPattern ap = new AimedPattern(3, Math.toRadians(8), 3.0);
            newBullets.addAll(ap.generate(x, y, playerX, playerY,
                    EnemyBullet.BulletSize.SMALL, new Color(255, 180, 80)));
        }

        // 一定時間経過したら下に逃げる
        if (frame > 600) {
            y += 1.0;
            stopped = false;
            if (y > 520) active = false;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        int size = 12;
        Path2D shape = new Path2D.Double();
        shape.moveTo(x, y - size);
        shape.lineTo(x + size, y - size * 0.3);
        shape.lineTo(x + size * 0.6, y + size);
        shape.lineTo(x - size * 0.6, y + size);
        shape.lineTo(x - size, y - size * 0.3);
        shape.closePath();

        g.setColor(new Color(180, 120, 50));
        g.fill(shape);
        g.setColor(new Color(255, 200, 80));
        g.draw(shape);
    }
}
