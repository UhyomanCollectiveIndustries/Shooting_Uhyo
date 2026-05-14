package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

/**
 * Stage2：2面のひな型。
 *
 * 【コンセプト案】
 *  - 高速敵が増え、横方向の敵の動きが激しくなる
 *  - 中ボス出現タイミングを途中に挟む(TODO)
 *
 * 【TODO】
 *  - 中ボス出現とフェーズ管理
 *  - 専用のBGM/背景設定
 *  - ステージ専用ボスの追加
 */
public class Stage2 implements Stage {
    private static final int BOSS_FRAME = 1200;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        switch (frame) {
            case 120 -> spawnFastWave(fastEnemies, 3);
            case 240 -> spawnEnemyRow(enemies, 4);
            case 360 -> spawnFastWave(fastEnemies, 4);
            case 480 -> spawnEnemyRow(enemies, 5);
            case 600 -> spawnFastWave(fastEnemies, 5);
            case 720 -> spawnEnemyRow(enemies, 6);
            case 900 -> spawnFastWave(fastEnemies, 4);
            // TODO: 中ボス出現
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
}
