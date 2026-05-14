package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * RadialPattern：全方位弾(放射状)を生成するパターン。
 *
 * 【役割】
 *  指定した発射元から、円周上に等間隔で directions 個の方向へ弾を撃つ。
 *  例：directions=8なら45度ごとに8発の弾が放射状に広がる。
 *
 * 【角度の計算式】
 *  - 円1周は 2π ラジアン
 *  - 1発あたりの角度差は 2π / directions
 *  - i番目の弾の角度 = 2π/directions * i + angleOffset
 *  → angleOffsetを変えると全体が回転する(連続発射時に少しずつ回せば「回転弾幕」になる)
 */
public class RadialPattern implements BulletPattern {
    private int directions;   // 何方向に撃つか
    private double speed;     // 弾の速さ
    private double angleOffset; // 全体の角度オフセット

    public RadialPattern(int directions, double speed, double angleOffset) {
        this.directions = directions;
        this.speed = speed;
        this.angleOffset = angleOffset;
    }

    @Override
    public List<EnemyBullet> generate(double x, double y, EnemyBullet.BulletSize size, Color color) {
        List<EnemyBullet> bullets = new ArrayList<>();
        for (int i = 0; i < directions; i++) {
            // 円周上に等間隔で配置
            double angle = Math.PI * 2 / directions * i + angleOffset;
            // 角度から速度ベクトル(vx,vy)を求める：単位円のcos/sinに速度を掛ける
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bullets.add(new EnemyBullet(x, y, vx, vy, size, color));
        }
        return bullets;
    }
}
