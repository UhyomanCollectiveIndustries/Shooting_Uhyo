package com.shootinguhyo.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;

/**
 * GameOptions：ゲーム全体の設定値。
 *
 * 【役割】
 *  オプション画面で変更される値を集中管理する。
 *  音量、表示設定、キーバインドなどを今後ここに集める想定。
 *
 * 【シングルトンっぽい使い方】
 *  ゲーム起動時に1つ作って、必要な箇所から参照する。
 *  設定は {@code ~/.shooting_uhyo/options.properties} に永続化される。
 *
 * 【TODO】
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

    // ===================== セーブ/ロード =====================

    /** 設定ファイルの保存先: ~/.shooting_uhyo/options.properties */
    private static File saveFile() {
        File dir = new File(System.getProperty("user.home", "."), ".shooting_uhyo");
        return new File(dir, "options.properties");
    }

    /**
     * 現在の設定値をPropertiesファイルへ書き出す。
     * 書き込みに失敗しても致命的ではないので、例外は握りつぶしてログのみ。
     */
    public void save() {
        Properties p = new Properties();
        p.setProperty("difficulty", difficulty.name());
        p.setProperty("bgmVolume", Integer.toString(bgmVolume));
        p.setProperty("seVolume", Integer.toString(seVolume));
        p.setProperty("showHitbox", Boolean.toString(showHitbox));

        File file = saveFile();
        try {
            Files.createDirectories(file.getParentFile().toPath());
            try (OutputStream out = Files.newOutputStream(file.toPath())) {
                p.store(out, "Shooting_Uhyo options");
            }
        } catch (IOException e) {
            System.err.println("[GameOptions] 設定の保存に失敗しました: " + e.getMessage());
        }
    }

    /**
     * Propertiesファイルから設定値を読み込む。
     * ファイルが無い・壊れている場合は既定値のまま何もしない(初回起動など)。
     */
    public void load() {
        File file = saveFile();
        if (!file.exists()) return;

        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file.toPath())) {
            p.load(in);
        } catch (IOException e) {
            System.err.println("[GameOptions] 設定の読み込みに失敗しました: " + e.getMessage());
            return;
        }

        String diff = p.getProperty("difficulty");
        if (diff != null) {
            try {
                setDifficulty(Difficulty.valueOf(diff));
            } catch (IllegalArgumentException ignored) {
                // 未知の値は無視して既定値を維持
            }
        }
        setBgmVolume(parseInt(p.getProperty("bgmVolume"), bgmVolume));
        setSeVolume(parseInt(p.getProperty("seVolume"), seVolume));
        showHitbox = Boolean.parseBoolean(p.getProperty("showHitbox", Boolean.toString(showHitbox)));
    }

    private static int parseInt(String s, int fallback) {
        if (s == null) return fallback;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
