package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

/**
 * Stage1：1面の進行を管理する具体クラス。
 *
 * 【役割】
 *  特定のフレーム数(タイミング)で敵を出現させる「タイムテーブル」を提供する。
 *  例えばゲーム開始から120フレーム(=2秒)で通常敵を3体出す、240フレームで高速敵を出す、など。
 *
 * 【switchの数値の意味(60FPSなので60フレーム=1秒)】
 *  120 = 2秒、240 = 4秒、360 = 6秒、…と等間隔に敵を出すリズム。
 *  最後の720(=12秒)を超えると、BOSS_FRAME(900=15秒)でボス戦に移行する。
 *
 * 【データ駆動 vs ハードコード】
 *  本当はステージ進行データをファイル(CSVやJSON)から読み込む形にすると拡張しやすいが、
 *  ここでは学習のしやすさを優先して直接コードに書いている。
 */
public class Stage1 implements Stage {
    private static final int BOSS_FRAME = 900; // ボス出現タイミング(15秒)

    /**
     * フレームに応じて敵を出現させる。
     * switch式で「決められたタイミング」のみ実行する。
     */
    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        switch (frame) {
            case 120 -> spawnEnemyRow(enemies, 3);
            case 240 -> spawnFastEnemies(fastEnemies, 2);
            case 360 -> spawnEnemyDiamond(enemies, 4);
            case 480 -> spawnFastEnemies(fastEnemies, 4);
            case 600 -> spawnEnemyRow(enemies, 5);
            case 720 -> spawnFastEnemies(fastEnemies, 3);
        }
    }

    /**
     * 通常敵を横一列に等間隔で配置。
     * 画面の幅 60〜320 を count+1 等分した位置に敵を置く。
     * y=-20と画面上端の少し外に出すのは「画面外から登場してくる」演出のため。
     */
    private void spawnEnemyRow(List<Enemy> enemies, int count) {
        for (int i = 0; i < count; i++) {
            double x = 60.0 + (260.0 / (count + 1)) * (i + 1);
            enemies.add(new Enemy(x, -20, 150, 200));
        }
    }

    /**
     * 通常敵を菱形フォーメーション(◇)で配置。
     * 位置を2次元配列で直接持っておく方式 → コードがシンプルで読みやすい。
     */
    private void spawnEnemyDiamond(List<Enemy> enemies, int count) {
        double[][] positions = {
            {192, -20}, {130, -60}, {254, -60}, {192, -100}
        };
        for (int i = 0; i < Math.min(count, positions.length); i++) {
            enemies.add(new Enemy(positions[i][0], positions[i][1], 150, 200));
        }
    }

    /**
     * 高速敵を左右交互に画面外から出現させる。
     * - 偶数番目は左から右へ(vx=+3)
     * - 奇数番目は右から左へ(vx=-3)
     * yを少しずつずらして1列に並ばないようにする(視認性のため)。
     */
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
