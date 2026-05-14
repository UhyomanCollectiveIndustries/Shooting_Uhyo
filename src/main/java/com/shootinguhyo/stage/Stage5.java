package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

/**
 * Stage5：5面のひな型。後半の高難度ステージ。
 *
 * 【コンセプト案】
 *  - 弾幕が一気に激しくなる
 *  - スペルカード持ちの中ボスが出現
 *
 * 【未実装のひな型】
 *  - 中ボスとそのスペルカード
 *  - ステージ専用シューター敵
 *  - 弾幕パターンを増やすための専用ボス
 */
public class Stage5 implements Stage {
    private static final int BOSS_FRAME = 2100;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        if (frame > 0 && frame < BOSS_FRAME && frame % 100 == 0) {
            spawnDense(enemies, fastEnemies);
        }
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
}
