package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

/**
 * Stage6：最終ステージのひな型。
 *
 * 【コンセプト案】
 *  - 雑魚は控えめ、ラスボス戦に重点
 *  - 道中は「決戦への前奏曲」的にしっとり始まり、終盤で激化
 *
 * 【未実装のひな型】
 *  - 最終ボス(ラスボス)とその5〜6個のスペルカード
 *  - 撃破後のエンディング演出への接続
 *  - 専用BGM(タイトルテーマのアレンジを想定)
 */
public class Stage6 implements Stage {
    private static final int BOSS_FRAME = 1500;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        if (frame > 0 && frame < BOSS_FRAME && frame % 200 == 0) {
            enemies.add(new Enemy(192, -20, 400, 500));
            fastEnemies.add(new FastEnemy(-20, 80, 3.5, 1.5));
            fastEnemies.add(new FastEnemy(404, 80, -3.5, 1.5));
        }
    }

    @Override
    public boolean isBossTime(int frame) { return frame >= BOSS_FRAME; }

    @Override
    public boolean isComplete(int frame) { return frame > BOSS_FRAME + 600; }
}
