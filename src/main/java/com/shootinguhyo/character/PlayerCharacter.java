package com.shootinguhyo.character;

import com.shootinguhyo.entity.bullet.PlayerBullet;
import com.shootinguhyo.graphics.PixelSprite;

import java.util.List;

/**
 * PlayerCharacter：プレイヤーが選べる「キャラクター」を表すインターフェース。
 *
 * 【役割】
 *  キャラクターごとに変えたい以下の要素を抽象化する。
 *   - 見た目(スプライト)
 *   - 攻撃方法 (弾の出方・本数・速さ・色 等)
 *   - 移動速度や当たり判定の大きさ
 *   - スペル(ボム)の効果
 *
 * 【設計のポイント】
 *  Player本体に直接「キャラA用の処理」「キャラB用の処理」を書くと
 *  if文だらけになるので、キャラクター側に処理を持たせる「委譲」設計にする。
 *
 * 【TODO 実装予定】
 *  - スペル(ボム)発動時の専用エフェクト
 *  - 立ち絵(ダイアログ用)のスプライト
 *  - 自機オプション(追従弾)の本数や位置
 */
public interface PlayerCharacter {
    /** 内部識別子(セーブやUI表示で使う) */
    String getId();

    /** 表示用の名前 */
    String getDisplayName();

    /** プロフィール文(キャラ選択画面で表示する1〜2行のテキスト) */
    String getProfile();

    /** ゲーム中の自機スプライト(ドット絵) */
    PixelSprite getInGameSprite();

    /** キャラ選択画面用の立ち絵 */
    PixelSprite getPortraitSprite();

    /** 通常移動速度 */
    double getNormalSpeed();

    /** フォーカス(低速)移動速度 */
    double getFocusSpeed();

    /** 当たり判定の半径 */
    double getHitboxRadius();

    /**
     * 通常ショット時の弾を生成する。
     *
     * @param x 自機x座標
     * @param y 自機y座標
     * @param power 現在のパワー値(0-400)
     * @param focus フォーカスモード中か
     * @return 発射する弾のリスト
     */
    List<PlayerBullet> createShot(double x, double y, int power, boolean focus);

    /**
     * ボム(スペル)を発動。
     * 弾消し効果に加えて、キャラ独自の演出/追加効果を返す。
     * 現状はTODO: ひな型として何もしない実装でよい。
     */
    default void onBomb(double x, double y) {
        // TODO: キャラ固有のボム演出を実装
    }
}
