package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

/**
 * Stage4：4面のひな型。中盤の山場ステージ。
 *
 * 【コンセプト案】
 *  - 通常敵HPアップ、攻撃が激化
 *  - 背景は「夜の森」など暗めの雰囲気(TODO)
 *
 * 【TODO 実装すべきもの】
 *  - 敵HPと攻撃頻度の上方修正
 *  - 中ボスフェーズ
 *  - 専用ボス
 */
public class Stage4 implements Stage {
    private static final int BOSS_FRAME = 1800;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        if (frame > 0 && frame < BOSS_FRAME && frame % 120 == 0) {
            int idx = frame / 120;
            if (idx % 2 == 0) {
                spawnLine(enemies, 5);
            } else {
                spawnFastSwarm(fastEnemies, 4);
            }
        }
    }

    private void spawnLine(List<Enemy> enemies, int count) {
        for (int i = 0; i < count; i++) {
            double x = 50.0 + (280.0 / (count + 1)) * (i + 1);
            enemies.add(new Enemy(x, -20, 250, 350));
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
}
