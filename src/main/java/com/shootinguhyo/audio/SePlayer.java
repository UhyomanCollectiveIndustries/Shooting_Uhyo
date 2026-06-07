package com.shootinguhyo.audio;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * SePlayer：効果音(SE)再生。
 *
 * <p>クラスパス {@code /se/{name}.wav} を {@link Clip} にロードしてキャッシュ。
 *  毎回 {@code setFramePosition(0); start();} することで即座に再生する。</p>
 *
 * <p>同じSEがオーバーラップする場面では、再生中のClipを止めて頭から鳴らし直す。
 *  完全な多重再生が必要なら別途プール化する必要があるが、
 *  Touhou系STGの操作音は連射してもOKに聞こえれば十分とする。</p>
 *
 * <p>ファイルが見つからない場合は1回ログを出して以後無音にする。</p>
 */
public class SePlayer {
    private int volume = 80;
    private final Map<String, Clip> cache = new HashMap<>();
    private final Set<String> missing = new HashSet<>();

    /** SEを再生。 */
    public void play(String name) {
        if (name == null) return;
        Clip clip = getClip(name);
        if (clip == null) return;
        try {
            if (clip.isRunning()) clip.stop();
            clip.setFramePosition(0);
            applyVolume(clip);
            clip.start();
        } catch (Exception ignored) { /* no-op */ }
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        // キャッシュ済みClipは次回play()時に再適用される
    }

    public int getVolume() { return volume; }

    // ---------- 内部ユーティリティ ----------

    private Clip getClip(String name) {
        if (missing.contains(name)) return null;
        Clip cached = cache.get(name);
        if (cached != null) return cached;

        String path = "/se/" + name + ".wav";
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) {
                missing.add(name);
                System.out.println("[SE] file not found: " + path);
                return null;
            }
            BufferedInputStream bin = new BufferedInputStream(in);
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(bin)) {
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                cache.put(name, clip);
                return clip;
            }
        } catch (Exception e) {
            missing.add(name);
            System.out.println("[SE] failed to load " + path + ": " + e.getMessage());
            return null;
        }
    }

    private void applyVolume(Clip clip) {
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gain.getMinimum();
            float max = gain.getMaximum();
            float frac = Math.max(0.0001f, volume / 100f);
            float db = (float) (Math.log10(frac) * 20.0);
            float value = Math.max(min, Math.min(max, db));
            gain.setValue(value);
        } catch (Exception ignored) { /* no-op */ }
    }
}
