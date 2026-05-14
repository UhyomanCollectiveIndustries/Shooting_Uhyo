package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * WavePattern：波打つように発射方向が揺れる扇状弾幕。
 *
 * 【役割】
 *  「真下方向(π/2)」を基準にしつつ、sin関数で発射角度を少し揺らした扇状弾を作る。
 *  撃ち方が時間で変化するため、見た目に「波打って」見える。
 *
 * 【角度計算】
 *  - baseAngle = π/2 + sin(waveFrequency) * 0.5
 *    → 中心角が常に少しふらつく(横に揺れる)
 *  - tは0〜1の正規化値。「左端→右端」を線形に進める指標
 *  - angle = baseAngle - spreadAngle/2 + spreadAngle * t
 *    → 中心角から±spreadAngle/2の範囲に等間隔で配置
 *
 * 【tの計算】
 *  bulletCount=1のとき割り算で0除算しないよう、三項演算で安全に0.5を入れている。
 */
public class WavePattern implements BulletPattern {
    private double spreadAngle;     // 弾の広がり角
    private int bulletCount;        // 弾の本数
    private double waveFrequency;   // 波の位相(呼出側で時間とともに増加させる)

    public WavePattern(double spreadAngle, int bulletCount, double waveFrequency) {
        this.spreadAngle = spreadAngle;
        this.bulletCount = bulletCount;
        this.waveFrequency = waveFrequency;
    }

    @Override
    public List<EnemyBullet> generate(double x, double y, EnemyBullet.BulletSize size, Color color) {
        List<EnemyBullet> bullets = new ArrayList<>();
        double baseAngle = Math.PI / 2 + Math.sin(waveFrequency) * 0.5;
        double speed = 3.0;

        for (int i = 0; i < bulletCount; i++) {
            double t = bulletCount > 1 ? (double) i / (bulletCount - 1) : 0.5;
            double angle = baseAngle - spreadAngle / 2 + spreadAngle * t;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bullets.add(new EnemyBullet(x, y, vx, vy, size, color));
        }
        return bullets;
    }
}
