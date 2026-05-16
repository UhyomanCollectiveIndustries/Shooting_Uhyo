package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.ExpandingRingPattern;
import com.shootinguhyo.pattern.SpiralPattern;
import com.shootinguhyo.pattern.SwitchbackPattern;
import com.shootinguhyo.pattern.WinderPattern;

import java.util.List;

/**
 * Stage5：5面のひな型。後半の高難度ステージ。約3分。
 */
public class Stage5 implements Stage {
    private static final int BOSS_FRAME = 10800;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        // 序盤(0〜1800)
        if (frame > 0 && frame < 1800 && frame % 240 == 0) {
            spawnLightWave(enemies, fastEnemies);
        }
        // 中盤(1800〜7200) — 200フレーム間隔
        if (frame >= 1800 && frame < 7200 && (frame - 1800) % 200 == 0) {
            spawnDense(enemies, fastEnemies);
        }
        // 後半(7200〜) — さらに密度UP
        if (frame >= 7200 && frame < BOSS_FRAME && (frame - 7200) % 150 == 0) {
            spawnDense(enemies, fastEnemies);
        }
    }

    /** 序盤の軽め波。1体だけスパイラル。 */
    private void spawnLightWave(List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        Enemy mid = new Enemy(192, -20, 300, 400);
        mid.withPattern(new SpiralPattern(0.20, 6, 2.2))
           .withInterval(18);
        enemies.add(mid);
        fastEnemies.add(new FastEnemy(-20, 60, 4.0, 1.0));
        fastEnemies.add(new FastEnemy(404, 60, -4.0, 1.0));
    }

    /**
     * 中盤以降の密配置:
     *  - 左 = ワインダー
     *  - 中央 = 拡大円(青CW＋赤CCW)
     *  - 右 = 切り返し
     */
    private void spawnDense(List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        Enemy left = new Enemy(80, -20, 300, 400);
        left.withPattern(WinderPattern.downward(50, 2.6))
            .withInterval(40);
        enemies.add(left);

        // 中央: 拡大円(色はパターン側で固定)
        Enemy mid = new Enemy(192, -20, 320, 450);
        mid.withPattern(ExpandingRingPattern.standard(), null, EnemyBullet.BulletSize.MEDIUM)
           .withInterval(35);
        enemies.add(mid);

        Enemy right = new Enemy(304, -20, 300, 400);
        right.withPattern(SwitchbackPattern.standard(2.6))
             .withInterval(28);
        enemies.add(right);

        fastEnemies.add(new FastEnemy(-20, 60, 4.0, 1.0));
        fastEnemies.add(new FastEnemy(404, 60, -4.0, 1.0));
    }

    @Override
    public boolean isBossTime(int frame) { return frame >= BOSS_FRAME; }

    @Override
    public boolean isComplete(int frame) { return frame > BOSS_FRAME + 300; }

    /** 雷雨風の青寄り暗色。 */
    @Override
    public java.awt.Color backgroundTint() { return new java.awt.Color(10, 10, 50, 90); }
}
