package com.shootinguhyo.audio;

/**
 * SeKeys：効果音(SE)の識別キーをまとめた定数クラス。
 *
 * 【役割】
 *  - SePlayer.play("shoot") のように文字列を直接書くとタイポでバグる
 *  - 定数として持っておくと SeKeys.SHOOT のように補完が効き、安全
 *
 * 【使い方】
 *  soundManager.se().play(SeKeys.SHOOT);
 *
 * 【TODO】
 *  - 各キーに対応する音源ファイルを resources/se/ に配置
 *  - 必要に応じてキーを増やす
 */
public final class SeKeys {
    private SeKeys() {} // 定数置き場なのでインスタンス化禁止

    public static final String SHOOT = "shoot";
    public static final String ENEMY_HIT = "enemy_hit";
    public static final String EXPLOSION = "explosion";
    public static final String PLAYER_HIT = "player_hit";
    public static final String BOMB = "bomb";
    public static final String ITEM = "item";
    public static final String SPELL_DECLARE = "spell_declare";
    public static final String SPELL_GET = "spell_get";
    public static final String EXTEND = "extend"; // 1UP音
    public static final String MENU_MOVE = "menu_move";
    public static final String MENU_SELECT = "menu_select";
    public static final String MENU_CANCEL = "menu_cancel";
}
