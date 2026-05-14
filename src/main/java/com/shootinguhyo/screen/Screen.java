package com.shootinguhyo.screen;

import com.shootinguhyo.InputHandler;
import java.awt.Graphics2D;

/**
 * Screen：個別画面(タイトル、キャラ選択、オプション等)の共通インターフェース。
 *
 * 【役割】
 *  GamePanelに全ての画面ロジックを書くと巨大化するので、
 *  画面ごとにクラスを分離する基盤としてのインターフェース。
 *
 * 【使い方の想定】
 *  GamePanel側で「現在の画面」を1つ持ち、毎フレーム update→render を呼ぶ。
 *  画面遷移は ScreenManager(後述)が一括管理する。
 */
public interface Screen {
    /** 入場時に1回呼ばれる初期化処理。 */
    default void onEnter() {}

    /** 退場時に1回呼ばれる後始末処理。 */
    default void onExit() {}

    /** 毎フレームのロジック更新。 */
    void update(InputHandler input);

    /** 毎フレームの描画。 */
    void render(Graphics2D g, int width, int height);

    /**
     * 次に遷移したい画面を返す。nullなら遷移なし。
     * 簡易なステート遷移管理。
     */
    default Screen nextScreen() { return null; }
}
