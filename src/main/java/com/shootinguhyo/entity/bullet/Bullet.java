package com.shootinguhyo.entity.bullet;

import com.shootinguhyo.entity.Entity;

/**
 * Bullet：弾の共通土台。
 *
 * 【役割】
 *  自機弾(PlayerBullet)と敵弾(EnemyBullet)で共通する処理(速度を持って移動・画面外で消滅)
 *  をまとめる。当たり判定の半径(radius)もここで保持する。
 *
 * 【なぜabstract(抽象)か】
 *  描画(draw)の見た目は弾の種類ごとに変えたいので、サブクラスで実装する。
 *
 * 【画面外チェック】
 *  -50〜500/550の範囲を出たら active=false。
 *  画面端ぴったりではなく余裕を持たせるのは「画面端で消えるのが見えると不自然」だから。
 */
public abstract class Bullet extends Entity {
    protected double vx, vy; // 速度ベクトル(1フレームでの移動量)
    protected double radius; // 当たり判定の半径

    public Bullet(double x, double y, double vx, double vy, double radius) {
        super(x, y);
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
    }

    public double getRadius() { return radius; }

    /**
     * 弾の共通動作：速度ベクトルだけ移動して、画面外なら消滅。
     * 単純なので継承先で特に上書きしなくてもよい設計。
     */
    @Override
    public void update() {
        x += vx;
        y += vy;
        if (x < -50 || x > 500 || y < -50 || y > 550) {
            active = false;
        }
    }
}
