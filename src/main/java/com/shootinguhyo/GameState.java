package com.shootinguhyo;

/**
 * GameState：ゲームの「今どの状態か」を表すための列挙型(enum)。
 *
 * 【役割】
 *  ゲームは画面の場面ごとに動きが違う（タイトル画面、プレイ中、ポーズ中など）。
 *  GameStateを使って「今どの場面か」を一つの値で管理する。
 *
 * 【なぜenumを使うのか】
 *  - 「状態を表す決まった選択肢」を分かりやすく安全に書けるのがenumの強み。
 *  - intやStringだと値を間違えてもコンパイル時に気付けないが、enumなら型チェックでミスを防げる。
 *  - switch文で網羅的に分岐できるため、状態遷移の見通しが良くなる。
 *
 * 【それぞれの意味】
 *  TITLE      : タイトル画面（Enter押下待ち）
 *  PLAYING    : 通常ステージプレイ中
 *  BOSS_FIGHT : ボス戦中
 *  PAUSED     : 一時停止中
 *  GAME_OVER  : 残機0でゲームオーバー画面
 *  CLEAR      : ボスを倒してステージクリア画面
 */
public enum GameState {
    TITLE, PLAYING, BOSS_FIGHT, PAUSED, GAME_OVER, CLEAR
}
