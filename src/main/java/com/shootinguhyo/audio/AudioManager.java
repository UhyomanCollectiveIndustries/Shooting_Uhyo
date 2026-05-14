package com.shootinguhyo.audio;

import com.shootinguhyo.config.GameOptions;

/**
 * AudioManager：BGMとSEの再生窓口を一本化するファサードクラス。
 *
 * 【役割】
 *  - 既存のBgmPlayerとSePlayerをまとめて扱う
 *  - 音量設定(GameOptions)との同期を取る
 *  - シーン切替に応じてBGMを変えるための一段抽象化された呼び出し口
 *
 * 【使い方】
 *  AudioManager audio = new AudioManager(gameOptions);
 *  audio.playBgmForStage(1);  // ステージ1のBGMをかける
 *  audio.playSe("shoot");     // ショット音
 *
 * 【TODO】
 *  - ステージ番号→BGMファイル名のマップ
 *  - SEの定数キー(SeKeysクラス等)
 *  - シーン別(タイトル/ボス戦/エンディング)BGM切替
 */
public class AudioManager {

    private final BgmPlayer bgm = new BgmPlayer();
    private final SePlayer se = new SePlayer();
    private final GameOptions options;

    public AudioManager(GameOptions options) {
        this.options = options;
        syncVolume();
    }

    /** GameOptionsの音量設定をPlayerに反映する。 */
    public void syncVolume() {
        if (options != null) {
            bgm.setVolume(options.getBgmVolume());
            se.setVolume(options.getSeVolume());
        }
    }

    // ----- BGM -----

    public void playTitleBgm() { bgm.play("title"); }
    public void playBgmForStage(int stage) { bgm.play("stage" + stage); }
    public void playBossBgm(int stage) { bgm.play("stage" + stage + "_boss"); }
    public void playEndingBgm() { bgm.play("ending"); }
    public void playGameOverBgm() { bgm.play("gameover"); }
    public void stopBgm() { bgm.stop(); }
    public void fadeOutBgm(int frames) { bgm.fadeOut(frames); }

    // ----- SE -----

    public void playSe(String name) { se.play(name); }

    /** よく使うSEを定数化(タイポ防止)。 */
    public static final class Se {
        private Se() {}
        public static final String SHOOT = "shoot";
        public static final String ENEMY_HIT = "enemy_hit";
        public static final String EXPLOSION = "explosion";
        public static final String PLAYER_HIT = "player_hit";
        public static final String BOMB = "bomb";
        public static final String ITEM = "item";
        public static final String SPELL_DECLARE = "spell_declare";
        public static final String SPELL_GET = "spell_get";
        public static final String MENU_MOVE = "menu_move";
        public static final String MENU_SELECT = "menu_select";
    }
}
