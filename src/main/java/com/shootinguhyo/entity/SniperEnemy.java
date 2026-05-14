package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.AimedPattern;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

/**
 * SniperEnemy：狙撃型の敵。
 *
 * 【コンセプト】
 *  画面外からゆっくり接近し、長い溜め時間の後に自機狙いの高速単発弾を撃つ。
 *  数は少ないが「予期せぬ被弾」を引き起こす嫌な敵。
 *
 * 【設計】
 *  - 通常状態(進入中) → 溜め状態(その場で止まる) → 発射 → 退避
 *  - フェーズを enum で管理
 *
 * 【TODO】
 *  - 溜め中のレーザー予告線(警告表示)
 *  - 専用ドット絵
 */
public class SniperEnemy extends Entity {
    private enum Phase { APPROACH, CHARGE, RETREAT }

    private Phase phase = Phase.APPROACH;
    private int hp = 200;
    private int frame = 0;
    private int phaseFrame = 0;
    private double playerX, playerY;
    private java.util.List<EnemyBullet> newBullets = new java.util.ArrayList<>();
    private final double approachY;

    public SniperEnemy(double x, double y, double approachY) {
        super(x, y);
        this.approachY = approachY;
    }

    public void setPlayerPosition(double px, double py) {
        this.playerX = px;
        this.playerY = py;
    }

    @Override
    public void update() {
        frame++;
        phaseFrame++;
        switch (phase) {
            case APPROACH -> {
                y += 1.5;
                if (y >= approachY) {
                    phase = Phase.CHARGE;
                    phaseFrame = 0;
                }
            }
            case CHARGE -> {
                // 90フレーム溜めた後に発射
                if (phaseFrame >= 90) {
                    AimedPattern ap = new AimedPattern(1, 0, 7.0); // 1発・高速
                    newBullets.addAll(ap.generate(x, y, playerX, playerY,
                            EnemyBullet.BulletSize.SMALL, new Color(255, 60, 60)));
                    phase = Phase.RETREAT;
                    phaseFrame = 0;
                }
            }
            case RETREAT -> {
                y -= 2.0; // 上に退避
                if (y < -30) active = false;
            }
        }
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            active = false;
        }
    }

    public boolean isDefeated() { return hp <= 0; }
    public int getScore() { return 300; }

    public java.util.List<EnemyBullet> getAndClearNewBullets() {
        java.util.List<EnemyBullet> bullets = new java.util.ArrayList<>(newBullets);
        newBullets.clear();
        return bullets;
    }

    @Override
    public void draw(Graphics2D g) {
        int size = 8;
        // CHARGE中は赤く点滅して警告
        Color body = (phase == Phase.CHARGE && (phaseFrame / 5) % 2 == 0)
                ? new Color(255, 80, 80) : new Color(150, 30, 30);
        Path2D tri = new Path2D.Double();
        tri.moveTo(x, y + size);
        tri.lineTo(x - size, y - size);
        tri.lineTo(x + size, y - size);
        tri.closePath();
        g.setColor(body);
        g.fill(tri);
        g.setColor(Color.WHITE);
        g.draw(tri);
    }
}
