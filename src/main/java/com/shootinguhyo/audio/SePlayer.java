package com.shootinguhyo.audio;

/**
 * SePlayer：効果音(SE: Sound Effect)再生のひな型クラス。
 *
 * 【役割】
 *  ショット音、被弾音、ボム音、撃破音などの短い音を即座に再生する。
 *
 * 【実装方針】
 *  - javax.sound.sampled.Clip で短いwavをメモリに読み込んでおく
 *  - 連射に耐えるため、同じ音でも複数のClipインスタンスを使い回す(プール)
 *
 * 【TODO】
 *  - 音源プールの実装
 *  - 同じ音のオーバーラップ再生
 *  - 音量制御
 *  - 同一フレームで同じ音が複数回鳴ったらまとめる(ショット音などのスパム対策)
 */
public class SePlayer {
    private int volume = 80;

    /** 効果音を再生。nameは音源の識別子(例: "shoot", "explode", "bomb")。 */
    public void play(String name) {
        // TODO: 音源を再生
        // System.out.println("[SE] " + name);
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
    }

    public int getVolume() { return volume; }
}
