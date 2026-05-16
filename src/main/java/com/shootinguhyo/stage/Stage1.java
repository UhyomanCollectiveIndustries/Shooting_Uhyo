package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

/**
 * Stage1：1面の進行を管理する具体クラス。
 *
 * <p>全長 約3分(10800フレーム / 60FPS)。
 *  序盤ステージなので、弾幕は軽め(Enemy#withLightShot)に設定して
 *  プレイヤーが操作に慣れるための余白を作る。</p>
 *
 * <p>時間進行(秒):
 *  0:00～0:30   軽い列・高速敵が断続的に
 *  0:30～1:30   雑魚＋高速敵が混じり始める
 *  1:30～2:30   密度が少しずつ上がる
 *  2:30～3:00   駆け込みで連続出現 → ボス
 *  </p>
 */
public class Stage1 implements Stage {
    /** ボス出現タイミング(60FPS換算で約3分)。 */
    private static final int BOSS_FRAME = 10800;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        // ----- 序盤(0:00〜0:30, 0〜1800) -----
        if (frame == 120)   spawnEnemyRow(enemies, 3);
        if (frame == 360)   spawnFastEnemies(fastEnemies, 2);
        if (frame == 600)   spawnEnemyRow(enemies, 3);
        if (frame == 900)   spawnFastEnemies(fastEnemies, 3);
        if (frame == 1200)  spawnEnemyDiamond(enemies, 4);
        if (frame == 1500)  spawnFastEnemies(fastEnemies, 3);

        // ----- 中盤前半(0:30〜1:30, 1800〜5400) -----
        if (frame >= 1800 && frame < 5400 && (frame - 1800) % 360 == 0) {
            int phase = ((frame - 1800) / 360) % 4;
            switch (phase) {
                case 0 -> spawnEnemyRow(enemies, 4);
                case 1 -> spawnFastEnemies(fastEnemies, 3);
                case 2 -> spawnEnemyDiamond(enemies, 4);
                case 3 -> spawnFastEnemies(fastEnemies, 4);
            }
        }

        // ----- 中盤後半(1:30〜2:30, 5400〜9000) -----
        if (frame >= 5400 && frame < 9000 && (frame - 5400) % 300 == 0) {
            int phase = ((frame - 5400) / 300) % 4;
            switch (phase) {
                case 0 -> spawnEnemyRow(enemies, 5);
                case 1 -> spawnFastEnemies(fastEnemies, 4);
                case 2 -> { spawnEnemyDiamond(enemies, 4); spawnFastEnemies(fastEnemies, 2); }
                case 3 -> spawnEnemyRow(enemies, 4);
            }
        }

        // ----- 駆け込み(2:30〜3:00, 9000〜10800) -----
        if (frame >= 9000 && frame < BOSS_FRAME && (frame - 9000) % 240 == 0) {
            int phase = ((frame - 9000) / 240) % 3;
            switch (phase) {
                case 0 -> { spawnEnemyRow(enemies, 5); spawnFastEnemies(fastEnemies, 2); }
                case 1 -> spawnFastEnemies(fastEnemies, 5);
                case 2 -> spawnEnemyDiamond(enemies, 4);
            }
        }
    }

    /** 通常敵を横一列に等間隔で配置。Stage1の敵は軽い弾幕。 */
    private void spawnEnemyRow(List<Enemy> enemies, int count) {
        for (int i = 0; i < count; i++) {
            double x = 60.0 + (260.0 / (count + 1)) * (i + 1);
            enemies.add(new Enemy(x, -20, 150, 200).withLightShot());
        }
    }

    /** 通常敵を菱形フォーメーション(◇)で配置。Stage1の敵は軽い弾幕。 */
    private void spawnEnemyDiamond(List<Enemy> enemies, int count) {
        double[][] positions = {
            {192, -20}, {130, -60}, {254, -60}, {192, -100}
        };
        for (int i = 0; i < Math.min(count, positions.length); i++) {
            enemies.add(new Enemy(positions[i][0], positions[i][1], 150, 200).withLightShot());
        }
    }

    /** 高速敵を左右交互に画面外から出現させる。 */
    private void spawnFastEnemies(List<FastEnemy> fastEnemies, int count) {
        for (int i = 0; i < count; i++) {
            boolean fromLeft = i % 2 == 0;
            double x = fromLeft ? -20 : 404;
            double y = 80 + (120.0 / count) * i;
            double vx = fromLeft ? 3.0 : -3.0;
            double vy = 1.5;
            fastEnemies.add(new FastEnemy(x, y, vx, vy));
        }
    }

    @Override
    public boolean isBossTime(int frame) { return frame >= BOSS_FRAME; }

    /** ボス出現から+5秒(300フレーム)経過したら「完全終了」とみなす。 */
    @Override
    public boolean isComplete(int frame) { return frame > BOSS_FRAME + 300; }
}
