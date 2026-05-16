package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
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

    private void spawnLightPair(List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        enemies.add(new Enemy(150, -20, 200, 300));
        fastEnemies.add(new FastEnemy(-20, 120, 3.0, 1.5));
    }

    private void spawnPair(List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        enemies.add(new Enemy(100, -20, 200, 300));
        enemies.add(new Enemy(284, -20, 200, 300));
        fastEnemies.add(new FastEnemy(-20, 120, 3.0, 1.5));
        fastEnemies.add(new FastEnemy(404, 120, -3.0, 1.5));
    }

    @Override
    public boolean isBossTime(int frame) { return frame >= BOSS_FRAME; }

    @Override
    public boolean isComplete(int frame) { return frame > BOSS_FRAME + 300; }

    /** 夕暮れ風の赤紫色。 */
    @Override
    public java.awt.Color backgroundTint() { return new java.awt.Color(60, 20, 40, 70); }
}
