package com.shootinguhyo.config;

/**
 * GameOptions：ゲーム全体の設定値。
 *
 * 【役割】
 *  オプション画面で変更される値を集中管理する。
 *  音量、表示設定、キーバインドなどを今後ここに集める想定。
 *
 * 【シングルトンっぽい使い方】
 *  ゲーム起動時に1つ作って、必要な箇所から参照する。
 *  本格的にはセーブ/ロード機能(JSON, Propertiesファイル)も追加したい。
 *
 * 【TODO】
 *  - 設定ファイルへの保存・読み込み (java.util.Properties等)
 *  - キーバインド変更機能
 *  - フルスクリーン切替
 *  - 弾密度を下げる等のアクセシビリティ設定
 */
public class GameOptions {
    private Difficulty difficulty = Difficulty.NORMAL;
    private int bgmVolume = 70;    // 0-100
    private int seVolume = 80;     // 0-100
    private boolean showHitbox = false; // 常時ヒットボックス表示

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        // 静的な現在値も同期(オプション画面から変更されたとき用)
        Difficulty.setCurrent(difficulty);
    }

    public int getBgmVolume() { return bgmVolume; }
    public void setBgmVolume(int bgmVolume) {
        this.bgmVolume = clamp(bgmVolume);
    }

    public int getSeVolume() { return seVolume; }
    public void setSeVolume(int seVolume) {
        this.seVolume = clamp(seVolume);
    }

    public boolean isShowHitbox() { return showHitbox; }
    public void setShowHitbox(boolean showHitbox) { this.showHitbox = showHitbox; }

    /** 0-100の範囲に収める。 */
    private int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }

    /**
     * 設定ファイルへ書き出すひな型。
     * TODO: 実装する。
     */
    public void save() {
        // TODO: Propertiesに書き出し
    }

    /**
     * 設定ファイルから読み込むひな型。
     * TODO: 実装する。
     */
    public void load() {
        // TODO: Propertiesから読み込み
    }
}
