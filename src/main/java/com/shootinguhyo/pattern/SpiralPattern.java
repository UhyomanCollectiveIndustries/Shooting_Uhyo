package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * SpiralPattern：螺旋(らせん)弾幕を生成するパターン。
 *
 * 【役割】
 *  全方位弾を毎フレーム少しずつ角度をずらして撃つことで、弾が「らせん状」に広がる
 *  美しい弾幕を作る。
 *
 * 【状態を持つ理由】
 *  currentAngleがメンバ変数になっていて、呼ぶたびに angleStep ずつ加算される。
 *  Radialとの違いは「呼出間の角度継続性」。連続して呼ぶことで回転を表現する。
 *
 * 【注意：このプロジェクトではBossが直接スパイラル処理を書いている】
 *  → クラス自体は用意してあるので、将来的にスパイラル弾幕を再利用したい場合に
 *  「new SpiralPattern(...).generate(...)」で呼べる。
 */
public class SpiralPattern implements BulletPattern {
    private double angleStep;     // 1回の呼出毎に進める角度
    private int bulletCount;      // 1呼出で撃つ弾数(円周上に等間隔)
    private double speed;
    private double currentAngle;  // 現在の角度(呼出ごとに増えていく)

    public SpiralPattern(double angleStep, int bulletCount, double speed) {
        this.angleStep = angleStep;
        this.bulletCount = bulletCount;
        this.speed = speed;
        this.currentAngle = 0;
    }

    @Override
    public List<EnemyBullet> generate(double x, double y, EnemyBullet.BulletSize size, Color color) {
        List<EnemyBullet> bullets = new ArrayList<>();
        // 円周上にbulletCount発撃つ。基準角はcurrentAngle
        for (int i = 0; i < bulletCount; i++) {
            double angle = currentAngle + Math.PI * 2 / bulletCount * i;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bullets.add(new EnemyBullet(x, y, vx, vy, size, color));
        }
        currentAngle += angleStep; // 次回呼出で少し回転する
        return bullets;
    }

    /** 角度をリセット。曲のセクションが変わるタイミング等で呼ぶ想定。 */
    public void resetAngle() { currentAngle = 0; }
}
