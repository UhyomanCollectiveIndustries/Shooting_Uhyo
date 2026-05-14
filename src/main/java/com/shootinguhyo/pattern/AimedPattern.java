package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.util.MathUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * AimedPattern：自機狙い弾(扇状)を生成するパターン。
 *
 * 【役割】
 *  プレイヤー(targetX, targetY)に向かう角度を中心に、複数の弾を扇状に発射する。
 *  「自機を狙う」だけでなく「扇状の広がり」を持つので、立ち止まっていると確実に当たる。
 *
 * 【主な計算】
 *  1. baseAngle = atan2(target-y - y, target-x - x) で自機方向の角度を求める
 *  2. ways個の弾を、中心(baseAngle)を基準に左右対称になるよう角度をずらして配置
 *
 * 【中央配置の式 (i - (ways-1)/2.0)】
 *  ways=5なら i=0..4、(ways-1)/2.0 = 2 となり、結果は -2, -1, 0, 1, 2 と
 *  中央を0にする並びになる。これに spreadAngle を掛けることで対称に広がる。
 */
public class AimedPattern implements BulletPattern {
    private int ways;             // 弾の本数
    private double spreadAngle;   // 隣接弾との角度差(ラジアン)
    private double speed;         // 速さ

    public AimedPattern(int ways, double spreadAngle, double speed) {
        this.ways = ways;
        this.spreadAngle = spreadAngle;
        this.speed = speed;
    }

    /**
     * ターゲット指定版：プレイヤー位置を渡して自機狙い弾を作る。
     */
    public List<EnemyBullet> generate(double x, double y, double targetX, double targetY,
                                    EnemyBullet.BulletSize size, Color color) {
        List<EnemyBullet> bullets = new ArrayList<>();
        double baseAngle = MathUtil.angle(x, y, targetX, targetY);

        for (int i = 0; i < ways; i++) {
            // 中央(i = (ways-1)/2)を0として、左右対称に広がる角度オフセット
            double offset = ways > 1 ? spreadAngle * (i - (ways - 1) / 2.0) : 0;
            double angle = baseAngle + offset;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bullets.add(new EnemyBullet(x, y, vx, vy, size, color));
        }
        return bullets;
    }

    /**
     * インターフェース要件の実装。
     * ターゲットが指定されない場合は、デフォルトで「真下方向(y+100)」を狙う形にしている。
     */
    @Override
    public List<EnemyBullet> generate(double x, double y, EnemyBullet.BulletSize size, Color color) {
        return generate(x, y, x, y + 100, size, color);
    }
}
