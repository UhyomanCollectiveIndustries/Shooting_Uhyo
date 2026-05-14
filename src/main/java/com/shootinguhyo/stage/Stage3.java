package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

/**
 * Stage3：3面のひな型。
 *
 * 【コンセプト案】
 *  - 雑魚と高速敵が同時出現する複合ステージ
 *  - 背景は「夕暮れの空」イメージ(TODO)
 *
 * 【TODO】
 *  - 専用パターン弾を撃つ「シューター型」雑魚の追加
 *  - 専用ボスとスペルカードの追加
 */
public class Stage3 implements Stage {
    private static final int BOSS_FRAME = 1500;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        // TODO: より複雑なスポーンパターンを実装
        if (frame > 0 && frame < BOSS_FRAME && frame % 150 == 0) {
            spawnPair(enemies, fastEnemies);
        }
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
}
