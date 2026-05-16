package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

/**
 * Stage1：1面の進行を管理する具体クラス。
 *
 * <p>全長 約1:30(5400フレーム / 60FPS)。
 *  序盤ステージなので弾幕は軽め(Enemy#withLightShot)に抑えるが、
 *  中盤〜終盤にかけて出現する敵の数は増やして、間延びしないようにする。</p>
 *
 * <p>時間進行(秒):
 *  0:00〜0:20 (0〜1200)  序盤: 軽い波が断続的に
 *  0:20〜0:50 (1200〜3000) 中盤: 敵の数が増え始める
 *  0:50〜1:20 (3000〜4800) 終盤: 雑魚＋高速敵が同時にやってくる
 *  1:20〜1:30 (4800〜5400) ボス直前のラッシュ
 *  </p>
 */
public class Stage1 implements Stage {
    /** ボス出現タイミング(約1分30秒)。 */
    private static final int BOSS_FRAME = 5400;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        // ----- 序盤(0〜1200, 0:00〜0:20) -----
        if (frame == 120)  spawnEnemyRow(enemies, 3);
        if (frame == 360)  spawnFastEnemies(fastEnemies, 2);
        if (frame == 600)  spawnEnemyRow(enemies, 3);
        if (frame == 840)  spawnFastEnemies(fastEnemies, 3);
        if (frame == 1080) spawnEnemyDiamond(enemies, 4);

        // ----- 中盤(1200〜3000, 0:20〜0:50) — 240F間隔、敵を増やす -----
        if (frame >= 1200 && frame < 3000 && (frame - 1200) % 240 == 0) {
            int phase = ((frame - 1200) / 240) % 4;
            switch (phase) {
                case 0 -> { spawnEnemyRow(enemies, 5); spawnFastEnemies(fastEnemies, 2); }
                case 1 -> spawnFastEnemies(fastEnemies, 5);
                case 2 -> { spawnEnemyDiamond(enemies, 4); spawnFastEnemies(fastEnemies, 2); }
                case 3 -> spawnEnemyRow(enemies, 6);
            }
        }

        // ----- 終盤(3000〜4800, 0:50〜1:20) — 200F間隔、雑魚＋高速敵が同時に -----
        if (frame >= 3000 && frame < 4800 && (frame - 3000) % 200 == 0) {
            int phase = ((frame - 3000) / 200) % 4;
            switch (phase) {
                case 0 -> { spawnEnemyRow(enemies, 6); spawnFastEnemies(fastEnemies, 3); }
                case 1 -> { spawnEnemyDiamond(enemies, 4); spawnFastEnemies(fastEnemies, 4); }
                case 2 -> spawnFastEnemies(fastEnemies, 6);
                case 3 -> { spawnEnemyRow(enemies, 5); spawnEnemyDiamond(enemies, 4); }
            }
        }

        // ----- 直前ラッシュ(4800〜5400, 1:20〜1:30) — 150F間隔の駆け込み -----
        if (frame >= 4800 && frame < BOSS_FRAME && (frame - 4800) % 150 == 0) {
            int phase = ((frame - 4800) / 150) % 3;
            switch (phase) {
                case 0 -> { spawnEnemyRow(enemies, 6); spawnFastEnemies(fastEnemies, 3); }
                case 1 -> spawnFastEnemies(fastEnemies, 6);
                case 2 -> { spawnEnemyDiamond(enemies, 4); spawnFastEnemies(fastEnemies, 4); }
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
