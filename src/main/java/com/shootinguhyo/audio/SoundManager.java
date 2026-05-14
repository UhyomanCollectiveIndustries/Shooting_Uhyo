package com.shootinguhyo.audio;

/**
 * SoundManager：BgmPlayerとSePlayerをまとめて管理する窓口クラス。
 *
 * 【役割】
 *  ゲーム全体に1つだけ存在し、どこからでも音響を呼べるようにする。
 *  GameOptionsの音量設定を反映したり、ミュート制御をここでまとめる。
 *
 * 【使い方の想定】
 *  SoundManager sound = new SoundManager();
 *  sound.bgm().play("stage1");
 *  sound.se().play("shoot");
 */
public class SoundManager {
    private final BgmPlayer bgm = new BgmPlayer();
    private final SePlayer se = new SePlayer();

    public BgmPlayer bgm() { return bgm; }
    public SePlayer se() { return se; }

    /** GameOptionsの音量設定を反映する。 */
    public void applyVolumes(int bgmVolume, int seVolume) {
        bgm.setVolume(bgmVolume);
        se.setVolume(seVolume);
    }
}
