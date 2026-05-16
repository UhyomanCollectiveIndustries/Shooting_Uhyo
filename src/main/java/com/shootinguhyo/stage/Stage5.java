package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
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

    private void spawnLightWave(List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        enemies.add(new Enemy(192, -20, 300, 400));
        fastEnemies.add(new FastEnemy(-20, 60, 4.0, 1.0));
        fastEnemies.add(new FastEnemy(404, 60, -4.0, 1.0));
    }

    private void spawnDense(List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        enemies.add(new Enemy(80, -20, 300, 400));
        enemies.add(new Enemy(192, -20, 300, 400));
        enemies.add(new Enemy(304, -20, 300, 400));
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
