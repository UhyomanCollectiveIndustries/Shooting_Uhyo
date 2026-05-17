package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.SpiralPattern;
import com.shootinguhyo.pattern.WinderPattern;

import java.awt.Color;
import java.util.List;

/**
 * Stage3：3面のひな型。約3分。
 * 雑魚と高速敵が同時出現する複合ステージ。
 */
public class Stage3 implements Stage {
    private static final int BOSS_FRAME = 10800;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        // 序盤(0〜1800) 軽め
        if (frame > 0 && frame < 1800 && frame % 300 == 0) {
            spawnLightPair(enemies, fastEnemies);
        }
        // 中盤(1800〜7200) 通常
        if (frame >= 1800 && frame < 7200 && (frame - 1800) % 240 == 0) {
            spawnPair(enemies, fastEnemies);
        }
        // 後半(7200〜) 密度UP
        if (frame >= 7200 && frame < BOSS_FRAME && (frame - 7200) % 180 == 0) {
            spawnPair(enemies, fastEnemies);
        }
    }

    /** 序盤: 通常雑魚＋FastEnemy。1体だけワインダー。 */
    private void spawnLightPair(List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        Enemy w = new Enemy(150, -20, 200, 300);
        w.withPattern(WinderPattern.downward(40, 2.4),
                new Color(120, 220, 255), EnemyBullet.BulletSize.SMALL)
         .withInterval(60);
        enemies.add(w);
        fastEnemies.add(new FastEnemy(-20, 120, 3.0, 1.5));
    }

    /** 中盤以降: ワインダー＋うずまき(スパイラル)を混ぜる。 */
    private void spawnPair(List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        // 左:ワインダー
        Enemy left = new Enemy(100, -20, 200, 300);
        left.withPattern(WinderPattern.downward(45, 2.4),
                new Color(120, 220, 255), EnemyBullet.BulletSize.SMALL)
            .withInterval(50);
        enemies.add(left);

        // 右:スパイラル(うずまき)
        Enemy right = new Enemy(284, -20, 200, 300);
        right.withPattern(new SpiralPattern(0.18, 6, 2.0),
                new Color(255, 180, 80), EnemyBullet.BulletSize.SMALL)
             .withInterval(20);
        enemies.add(right);

        fastEnemies.add(new FastEnemy(-20, 120, 3.0, 1.5));
        fastEnemies.add(new FastEnemy(404, 120, -3.0, 1.5));
    }

    @Override
    public int midBossFrame() { return 3600; }

    @Override
    public boolean isBossTime(int frame) { return frame >= BOSS_FRAME; }

    @Override
    public boolean isComplete(int frame) { return frame > BOSS_FRAME + 300; }

    /** 夕暮れ風の赤紫色。 */
    @Override
    public java.awt.Color backgroundTint() { return new java.awt.Color(60, 20, 40, 70); }
}
