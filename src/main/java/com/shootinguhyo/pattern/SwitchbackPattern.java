package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * SwitchbackPattern：切り返し(きりかえし)弾幕。
 *
 * <p>扇状のwayを左右どちらかに寄せて発射、一定回数撃ったら反対側に切り替える。
 *  プレイヤーは「ある方向に避け続け」「ある瞬間に切り返す」操作を強いられる。</p>
 *
 * <p>動作:
 *  <ol>
 *    <li>左側へ寄った扇状をphaseLength回発射</li>
 *    <li>次のphaseLength回は右側へ寄った扇状</li>
 *    <li>以降ループ</li>
 *  </ol>
 * </p>
 */
public class SwitchbackPattern implements BulletPattern {
    private int counter = 0;
    private final int ways;
    private final double spreadAngleRad;
    private final double speed;
    private final double centerAngle;
    private final double swingAmplitude;
    private final int phaseLength;

    /**
     * @param ways           1回あたりの弾数(扇状)
     * @param spreadDeg      扇の広がり(度)
     * @param speed          弾速
     * @param centerAngle    中心角(π/2 = 真下)
     * @param swingDeg       片側に寄る角度(度)
     * @param phaseLength    同じ方向に連続発射する回数
     */
    public SwitchbackPattern(int ways, double spreadDeg, double speed,
                             double centerAngle, double swingDeg, int phaseLength) {
        this.ways = Math.max(1, ways);
        this.spreadAngleRad = Math.toRadians(spreadDeg);
        this.speed = speed;
        this.centerAngle = centerAngle;
        this.swingAmplitude = Math.toRadians(swingDeg);
        this.phaseLength = Math.max(1, phaseLength);
    }

    /** よく使う設定: 真下基準・5way・swing±20°・3発で切替。 */
    public static SwitchbackPattern standard(double speed) {
        return new SwitchbackPattern(5, 16.0, speed, Math.PI / 2, 20.0, 3);
    }

    @Override
    public List<EnemyBullet> generate(double x, double y,
                                      EnemyBullet.BulletSize size, Color color) {
        List<EnemyBullet> bullets = new ArrayList<>();
        // 「左/右」のフェーズ判定
        int phase = (counter / phaseLength) % 2;
        double swing = (phase == 0 ? -1 : 1) * swingAmplitude;
        double baseAngle = centerAngle + swing;

        for (int i = 0; i < ways; i++) {
            double offset = ways > 1
                    ? spreadAngleRad * (i - (ways - 1) / 2.0)
                    : 0;
            double angle = baseAngle + offset;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bullets.add(new EnemyBullet(x, y, vx, vy, size, color));
        }
        counter++;
        return bullets;
    }
}
