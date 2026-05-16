package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.SpiralPattern;
import com.shootinguhyo.pattern.SwitchbackPattern;
import com.shootinguhyo.pattern.WinderPattern;

import java.awt.Color;
import java.util.List;

/**
 * Stage4：4面のひな型。中盤の山場ステージ。約3分。
 */
public class Stage4 implements Stage {
    private static final int BOSS_FRAME = 10800;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        // 序盤(0〜1800)
        if (frame > 0 && frame < 1800 && frame % 240 == 0) {
            int idx = frame / 240;
            if (idx % 2 == 0) spawnLine(enemies, 4);
            else              spawnFastSwarm(fastEnemies, 3);
        }
        // 中盤(1800〜7200) — 200フレーム間隔
        if (frame >= 1800 && frame < 7200 && (frame - 1800) % 200 == 0) {
            int idx = (frame - 1800) / 200;
            if (idx % 2 == 0) spawnLine(enemies, 5);
            else              spawnFastSwarm(fastEnemies, 4);
        }
        // 後半(7200〜) 同時出現
        if (frame >= 7200 && frame < BOSS_FRAME && (frame - 7200) % 180 == 0) {
            int idx = (frame - 7200) / 180;
            if (idx % 3 == 0) { spawnLine(enemies, 5); spawnFastSwarm(fastEnemies, 2); }
            else if (idx % 3 == 1) spawnFastSwarm(fastEnemies, 5);
            else spawnLine(enemies, 6);
        }
    }

    /**
     * Stage4の列: 中央のリーダー1体が切り返し弾(SwitchbackPattern)、
     * 列の端2体がワインダーとスパイラル、残りは通常Radial。
     */
    private void spawnLine(List<Enemy> enemies, int count) {
        int mid = count / 2;
        for (int i = 0; i < count; i++) {
            double x = 50.0 + (280.0 / (count + 1)) * (i + 1);
            Enemy e = new Enemy(x, -20, 250, 350);
            if (i == mid) {
                e.withPattern(SwitchbackPattern.standard(2.6),
                        new Color(255, 220, 100), EnemyBullet.BulletSize.SMALL)
                 .withInterval(30);
            } else if (i == 0) {
                e.withPattern(WinderPattern.downward(45, 2.6),
                        new Color(120, 220, 255), EnemyBullet.BulletSize.SMALL)
                 .withInterval(45);
            } else if (i == count - 1) {
                e.withPattern(new SpiralPattern(0.22, 6, 2.2),
                        new Color(255, 130, 80), EnemyBullet.BulletSize.SMALL)
                 .withInterval(18);
            }
            enemies.add(e);
        }
    }

    private void spawnFastSwarm(List<FastEnemy> list, int count) {
        for (int i = 0; i < count; i++) {
            boolean fromLeft = i % 2 == 0;
            double x = fromLeft ? -20 : 404;
            double y = 80 + i * 30;
            double vx = fromLeft ? 4.0 : -4.0;
            list.add(new FastEnemy(x, y, vx, 1.0));
        }
    }

    @Override
    public boolean isBossTime(int frame) { return frame >= BOSS_FRAME; }

    @Override
    public boolean isComplete(int frame) { return frame > BOSS_FRAME + 300; }

    /** 夜の森風の深緑色。 */
    @Override
    public java.awt.Color backgroundTint() { return new java.awt.Color(10, 30, 15, 80); }
}
