package com.shootinguhyo.audio;

/**
 * BgmPlayer：BGM再生のひな型クラス。
 *
 * 【役割】
 *  ステージごとのBGMを再生する。フェードイン/アウト、ループ再生を扱う想定。
 *
 * 【実装方針(検討中)】
 *  - javax.sound.sampled の Clip を使うか、外部ライブラリ(jaco-mp3, JLayer等)を使うか検討
 *  - .wav はそのままClipで再生可能、.mp3 や .ogg は別ライブラリ必要
 *  - 短いBGMをClipでループ、長いBGMはSourceDataLineでストリーミング
 *
 * 【TODO】
 *  - 実際の音源ファイル読み込み(resources/bgm/ にwavを置く想定)
 *  - 音量制御(0-100)
 *  - フェードイン/アウト
 *  - 現在再生中のBGM切替時のクロスフェード
 */
public class BgmPlayer {
    private String currentBgm;
    private int volume = 70;

    /** BGMを再生開始(ファイル名から推定)。 */
    public void play(String name) {
        if (name.equals(currentBgm)) return; // 既に同じ曲を再生中なら何もしない
        currentBgm = name;
        // TODO: 実際の音源を読み込んで再生する
        System.out.println("[BGM] play: " + name);
    }

    /** BGMを停止。 */
    public void stop() {
        currentBgm = null;
        // TODO: Clipまたはストリーミング停止
    }

    /** フェードアウトしながら停止。 */
    public void fadeOut(int frames) {
        // TODO: 音量を徐々に下げて停止
        stop();
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        // TODO: 再生中の音にも反映
    }

    public int getVolume() { return volume; }
}
