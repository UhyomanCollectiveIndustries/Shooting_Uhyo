package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * WinderPattern：ワインダー(winder)弾幕。
 *
 * <p>1回の発射ごとに発射角度をsin波で揺らし、連続発射すると
 *  弾列が「ジグザグの帯」のように見えるパターン。
 *  避けるには左右の小刻みなずらしが要求される。</p>
 *
 * <p>状態:
 *  <ul>
 *    <li>{@code counter} — 呼出回数。sinの位相をここから計算する</li>
 *    <li>{@code centerAngle} — 中心角(基本は真下=π/2)</li>
 *    <li>{@code amplitude} — 振幅(ラジアン)</li>
 *    <li>{@code frequency} — 1ステップあたりの位相増分(小さいほどうねりが緩やか)</li>
 *  </ul>
 * </p>
 */
public class WinderPattern implements BulletPattern {
    private int counter = 0;
    private final double centerAngle;
    private final double amplitude;
    private final double frequency;
    private final int bulletsPerStep;
    private final double speed;

    public WinderPattern(double centerAngle, double amplitudeRad,
                         double frequency, double speed, int bulletsPerStep) {
        this.centerAngle = centerAngle;
        this.amplitude = amplitudeRad;
        this.frequency = frequency;
        this.bulletsPerStep = Math.max(1, bulletsPerStep);
        this.speed = speed;
    }

    /** 真下方向(centerAngle=π/2)を基準にしたデフォルト構成。 */
    public static WinderPattern downward(double amplitudeDeg, double speed) {
        return new WinderPattern(Math.PI / 2, Math.toRadians(amplitudeDeg),
                0.6, speed, 1);
    }

    @Override
    public List<EnemyBullet> generate(double x, double y,
                                      EnemyBullet.BulletSize size, Color color) {
        List<EnemyBullet> bullets = new ArrayList<>();
        double angle = centerAngle + Math.sin(counter * frequency) * amplitude;
        for (int i = 0; i < bulletsPerStep; i++) {
            // 同時発射する場合は速度を微妙に変えて散らす
            double s = speed * (1.0 + (i - (bulletsPerStep - 1) / 2.0) * 0.05);
            double vx = Math.cos(angle) * s;
            double vy = Math.sin(angle) * s;
            bullets.add(new EnemyBullet(x, y, vx, vy, size, color));
        }
        counter++;
        return bullets;
    }
}
