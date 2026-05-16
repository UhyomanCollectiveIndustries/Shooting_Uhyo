package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

/**
 * Stage2：2面のひな型。約3分。
 *
 * <p>高速敵が増え、横方向の敵の動きが激しくなる。
 *  Stage1より弾幕が少しだけ濃い(デフォルトのEnemy)。</p>
 */
public class Stage2 implements Stage {
    private static final int BOSS_FRAME = 10800;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        // 序盤
        if (frame == 120)  spawnFastWave(fastEnemies, 3);
        if (frame == 360)  spawnEnemyRow(enemies, 4);
        if (frame == 720)  spawnFastWave(fastEnemies, 3);
        if (frame == 1080) spawnEnemyRow(enemies, 4);
        if (frame == 1440) spawnFastWave(fastEnemies, 4);

        // 中盤(1800〜7200) — 360フレーム間隔
        if (frame >= 1800 && frame < 7200 && (frame - 1800) % 360 == 0) {
            int phase = ((frame - 1800) / 360) % 4;
            switch (phase) {
                case 0 -> spawnEnemyRow(enemies, 4);
                case 1 -> spawnFastWave(fastEnemies, 4);
                case 2 -> spawnEnemyRow(enemies, 5);
                case 3 -> spawnFastWave(fastEnemies, 5);
            }
        }

        // 後半(7200〜10800) — 280フレーム間隔で密度UP
        if (frame >= 7200 && frame < BOSS_FRAME && (frame - 7200) % 280 == 0) {
            int phase = ((frame - 7200) / 280) % 3;
            switch (phase) {
                case 0 -> { spawnEnemyRow(enemies, 5); spawnFastWave(fastEnemies, 2); }
                case 1 -> spawnFastWave(fastEnemies, 5);
                case 2 -> spawnEnemyRow(enemies, 6);
            }
        }
    }

    private void spawnFastWave(List<FastEnemy> list, int count) {
        for (int i = 0; i < count; i++) {
            boolean fromLeft = i % 2 == 0;
            double x = fromLeft ? -20 : 404;
            double y = 60 + (160.0 / count) * i;
            double vx = fromLeft ? 3.5 : -3.5;
            list.add(new FastEnemy(x, y, vx, 1.2));
        }
    }

    private void spawnEnemyRow(List<Enemy> list, int count) {
        for (int i = 0; i < count; i++) {
            double x = 60.0 + (260.0 / (count + 1)) * (i + 1);
            list.add(new Enemy(x, -20, 180, 250));
        }
    }

    @Override
    public boolean isBossTime(int frame) { return frame >= BOSS_FRAME; }

    @Override
    public boolean isComplete(int frame) { return frame > BOSS_FRAME + 300; }

    /** 朝焼け風の淡いオレンジ。 */
    @Override
    public java.awt.Color backgroundTint() { return new java.awt.Color(40, 20, 10, 60); }
}
