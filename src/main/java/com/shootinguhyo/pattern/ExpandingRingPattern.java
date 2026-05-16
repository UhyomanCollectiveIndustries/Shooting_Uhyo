package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * ExpandingRingPattern：青(時計回り)＋赤(反時計回り)の2色回転拡大円弾幕。
 *
 * <p>1回の呼出で「同一中心から放射状にN発」を2セット撃つ。
 *  毎回呼出ごとに片方は+rotateStep、もう片方は-rotateStepだけ回転オフセットを変える。
 *  低速で出続けることで、見た目は「2方向に回転する2色の同心円が外へ広がる」ように見える。</p>
 *
 * <p>呼出側(EnemyやBoss)が一定間隔で繰り返し呼ぶ前提のパターン。</p>
 */
public class ExpandingRingPattern implements BulletPattern {
    private int counter = 0;
    private final int bulletsPerRing;
    private final double speed;
    private final double rotateStep; // 1ステップごとの角度オフセット(ラジアン)
    private final Color blueColor;
    private final Color redColor;

    /**
     * @param bulletsPerRing 1リングあたりの弾数
     * @param speed          弾速(遅めだと拡大が綺麗に見える: 1.4〜2.0推奨)
     * @param rotateStepRad  1ステップごとの角度オフセット
     */
    public ExpandingRingPattern(int bulletsPerRing, double speed, double rotateStepRad) {
        this.bulletsPerRing = Math.max(2, bulletsPerRing);
        this.speed = speed;
        this.rotateStep = rotateStepRad;
        this.blueColor = new Color(80, 160, 255);
        this.redColor  = new Color(255, 100, 110);
    }

    /** 標準設定: 12発リング、速さ1.8、回転0.08rad/step。 */
    public static ExpandingRingPattern standard() {
        return new ExpandingRingPattern(12, 1.8, 0.08);
    }

    /**
     * 色は無視され、青CW+赤CCWで固定。インターフェース要件を満たすために color 引数は残す。
     */
    @Override
    public List<EnemyBullet> generate(double x, double y,
                                      EnemyBullet.BulletSize size, Color ignored) {
        List<EnemyBullet> bullets = new ArrayList<>();
        double baseAngle = counter * rotateStep;
        double step = Math.PI * 2 / bulletsPerRing;

        // 青リング(時計回り = baseAngleが増えるほど時計回りに進む)
        for (int i = 0; i < bulletsPerRing; i++) {
            double angle = baseAngle + step * i;
            bullets.add(new EnemyBullet(x, y,
                    Math.cos(angle) * speed,
                    Math.sin(angle) * speed,
                    size, blueColor));
        }
        // 赤リング(反時計回り)
        for (int i = 0; i < bulletsPerRing; i++) {
            double angle = -baseAngle + step * i;
            bullets.add(new EnemyBullet(x, y,
                    Math.cos(angle) * speed,
                    Math.sin(angle) * speed,
                    size, redColor));
        }
        counter++;
        return bullets;
    }
}
