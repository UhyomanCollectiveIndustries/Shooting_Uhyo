package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

/**
 * Stage6：最終ステージのひな型。約3分。
 * 雑魚は控えめ、決戦への前奏として徐々に密度UP。
 */
public class Stage6 implements Stage {
    private static final int BOSS_FRAME = 10800;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        // 序盤(0〜2400) — しっとり目
        if (frame > 0 && frame < 2400 && frame % 400 == 0) {
            enemies.add(new Enemy(192, -20, 400, 500));
        }
        // 中盤(2400〜7200) — 通常密度
        if (frame >= 2400 && frame < 7200 && (frame - 2400) % 300 == 0) {
            enemies.add(new Enemy(192, -20, 400, 500));
            fastEnemies.add(new FastEnemy(-20, 80, 3.5, 1.5));
            fastEnemies.add(new FastEnemy(404, 80, -3.5, 1.5));
        }
        // 終盤(7200〜) — 激化
        if (frame >= 7200 && frame < BOSS_FRAME && (frame - 7200) % 200 == 0) {
            enemies.add(new Enemy(120, -20, 400, 500));
            enemies.add(new Enemy(264, -20, 400, 500));
            fastEnemies.add(new FastEnemy(-20, 80, 3.5, 1.5));
            fastEnemies.add(new FastEnemy(404, 80, -3.5, 1.5));
        }
    }

    @Override
    public boolean isBossTime(int frame) { return frame >= BOSS_FRAME; }

    @Override
    public boolean isComplete(int frame) { return frame > BOSS_FRAME + 600; }

    /** 宇宙風の濃紫色(ラスボス感)。 */
    @Override
    public java.awt.Color backgroundTint() { return new java.awt.Color(30, 0, 60, 100); }
}
