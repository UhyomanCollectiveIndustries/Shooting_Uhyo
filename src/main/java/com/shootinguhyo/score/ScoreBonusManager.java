package com.shootinguhyo.score;

import com.shootinguhyo.entity.Player;

/**
 * ScoreBonusManager：スコア閾値に応じてプレイヤーに特典を与えるクラス。
 *
 * 【役割】
 *  「100万点で残機1UP」「300万点でボム+1」のような節目のボーナスを管理する。
 *  プレイヤーのスコア進行を毎フレーム監視し、閾値を越えたら特典を付与する。
 *
 * 【使い方】
 *  ScoreBonusManager bonus = new ScoreBonusManager();
 *  // 毎フレーム: bonus.checkAndApply(player);
 *
 * 【TODO】
 *  - キャラ別ボーナスの追加
 *  - グレイズ数による独自ボーナス
 *  - 1UP/ボム加算演出(画面に「Extend!」と表示)
 */
public class ScoreBonusManager {
    // 閾値リスト(到達したらフラグを立てて二度発動しないようにする)
    private static final long[] LIFE_UP_THRESHOLDS = { 1_000_000L, 3_000_000L, 6_000_000L };
    private static final long[] BOMB_UP_THRESHOLDS = { 500_000L, 2_000_000L, 4_000_000L };

    private final boolean[] lifeUpAwarded = new boolean[LIFE_UP_THRESHOLDS.length];
    private final boolean[] bombUpAwarded = new boolean[BOMB_UP_THRESHOLDS.length];

    /**
     * プレイヤーのスコアをチェックして、未獲得の閾値を越えていればボーナスを付与する。
     */
    public void checkAndApply(Player player) {
        long score = player.getScore();

        for (int i = 0; i < LIFE_UP_THRESHOLDS.length; i++) {
            if (!lifeUpAwarded[i] && score >= LIFE_UP_THRESHOLDS[i]) {
                lifeUpAwarded[i] = true;
                grantLife(player);
            }
        }
        for (int i = 0; i < BOMB_UP_THRESHOLDS.length; i++) {
            if (!bombUpAwarded[i] && score >= BOMB_UP_THRESHOLDS[i]) {
                bombUpAwarded[i] = true;
                grantBomb(player);
            }
        }
    }

    /** 1UPを付与。 */
    private void grantLife(Player player) {
        player.addLife(1);
        // TODO: 「Extend!」テキストの画面表示や効果音(SeKeys.EXTEND)を追加
    }

    /** ボムを1つ付与。 */
    private void grantBomb(Player player) {
        player.addBomb(1);
        // TODO: ボム獲得演出と効果音を追加
    }

    /** リセット(新規プレイ開始時に呼ぶ)。 */
    public void reset() {
        for (int i = 0; i < lifeUpAwarded.length; i++) lifeUpAwarded[i] = false;
        for (int i = 0; i < bombUpAwarded.length; i++) bombUpAwarded[i] = false;
    }
}
