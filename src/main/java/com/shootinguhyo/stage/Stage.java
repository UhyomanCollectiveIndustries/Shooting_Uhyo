package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

/**
 * Stage：ステージ進行を表すインターフェース。
 *
 * 【役割】
 *  「ステージ」は「経過フレーム数に応じて敵を出現させる」「ボス出現タイミングを判定する」
 *  といった役目を持つ。
 *  抽象化することで、Stage1, Stage2, … と複数ステージを同じ仕組みで扱える。
 *
 * 【なぜインターフェースか】
 *  どんなステージでも「フレーム数を渡されたら自分の判断で敵を追加する」というルールだけ
 *  決めればよい。実装の自由度を保ちながら共通の使い方を確保するためのインターフェース。
 */
public interface Stage {
    /**
     * 経過フレーム数に応じて敵リストに新しい敵を追加する。
     * リストを直接書き換える設計なので、戻り値は不要。
     */
    void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies);

    /** ボス出現タイミングか判定する。 */
    boolean isBossTime(int frame);

    /** ステージが完全に終わった(クリア後の余韻フレーム含む)か判定する。 */
    boolean isComplete(int frame);
}
