package com.shootinguhyo.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * BgmPlayer：BGM再生。
 *
 * <p>クラスパス {@code /bgm/{name}.wav} を {@link Clip} でループ再生する。
 *  ファイルが見つからない場合はログを出してフォールバック(何もしない)。</p>
 *
 * <p>同一のBGMが既に再生中の場合は再起動しない。
 *  別のBGMに切り替わるときは前のClipを停止+解放してから新規に開く。</p>
 */
public class BgmPlayer {
    private Clip currentClip;
    private String currentBgm;
    private int volume = 70;
    /** 1度ロード失敗したリソースは再試行しないためのキャッシュ。 */
    private final Set<String> missingResources = new HashSet<>();

    /** BGMを再生開始(同名なら無視)。 */
    public void play(String name) {
        if (name == null) return;
        if (name.equals(currentBgm) && currentClip != null && currentClip.isRunning()) {
            return;
        }
        stopInternal();
        currentBgm = name;

        String path = "/bgm/" + name + ".wav";
        if (missingResources.contains(path)) {
            // 既に欠落確認済み。静かにスキップ。
            return;
        }
        Clip clip = loadClip(path);
        if (clip == null) {
            missingResources.add(path);
            System.out.println("[BGM] file not found: " + path);
            return;
        }
        currentClip = clip;
        applyVolume(currentClip);
        currentClip.setFramePosition(0);
        currentClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /** BGMを停止して解放。 */
    public void stop() {
        stopInternal();
        currentBgm = null;
    }

    private void stopInternal() {
        if (currentClip != null) {
            try {
                currentClip.stop();
                currentClip.close();
            } catch (Exception ignored) { /* no-op */ }
            currentClip = null;
        }
    }

    /**
     * フェードアウトしながら停止。
     * 簡易実装: 別スレッドで一定間隔ごとに音量を下げて、最後に stop() する。
     */
    public void fadeOut(int frames) {
        if (currentClip == null) { stop(); return; }
        Clip target = currentClip;
        int durationMs = Math.max(50, frames * 1000 / 60);
        new Thread(() -> {
            try {
                int steps = 20;
                long stepMs = Math.max(1, durationMs / steps);
                FloatControl gain = getMasterGain(target);
                if (gain == null) { target.stop(); target.close(); return; }
                float startVal = gain.getValue();
                float min = gain.getMinimum();
                for (int i = 0; i < steps; i++) {
                    float t = (i + 1) / (float) steps;
                    float v = startVal + (min - startVal) * t;
                    try { gain.setValue(Math.max(min, v)); } catch (IllegalArgumentException ignored) {}
                    Thread.sleep(stepMs);
                }
                target.stop();
                target.close();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } catch (Exception ignored) { /* no-op */ }
        }, "bgm-fade").start();
        // 参照を切ってこの後の play() で新規開始させる
        currentClip = null;
        currentBgm = null;
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        if (currentClip != null) applyVolume(currentClip);
    }

    public int getVolume() { return volume; }

    // ---------- 内部ユーティリティ ----------

    private Clip loadClip(String resourcePath) {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) return null;
            BufferedInputStream bin = new BufferedInputStream(in);
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(bin)) {
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                return clip;
            }
        } catch (Exception e) {
            System.out.println("[BGM] failed to load " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    private FloatControl getMasterGain(Clip clip) {
        try {
            return (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void applyVolume(Clip clip) {
        FloatControl gain = getMasterGain(clip);
        if (gain == null) return;
        // 線形音量(0-100)を gain (dB) に変換。0=完全無音、100=最大。
        float min = gain.getMinimum();
        float max = gain.getMaximum();
        float frac = Math.max(0.0001f, volume / 100f);
        // 知覚的に下げ過ぎないよう、対数的にマッピング(0.0001→min, 1.0→max)
        float db = (float) (Math.log10(frac) * 20.0);
        float value = Math.max(min, Math.min(max, db));
        try { gain.setValue(value); } catch (IllegalArgumentException ignored) {}
    }
}
