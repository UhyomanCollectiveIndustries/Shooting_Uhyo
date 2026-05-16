package com.shootinguhyo.graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * ImageLoader：クラスパスから画像を読み込んでキャッシュするユーティリティ。
 *
 * 【役割】
 *  キャラクター絵やタイトル絵など、リソースとして配置したPNG/JPGを
 *  毎回ディスクから読まずに、起動後1回だけロードして使い回す。
 *
 * 【使い方】
 *  BufferedImage img = ImageLoader.load("/character/uhyoman.png");
 *  if (img != null) { ... 画像が見つかった ... }
 *  else            { ... フォールバック ... }
 */
public final class ImageLoader {
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static final Map<String, Boolean> MISSED = new HashMap<>();

    private ImageLoader() {}

    /**
     * 単一パスから画像を取得。見つからなければnull。
     */
    public static BufferedImage load(String resourcePath) {
        if (resourcePath == null) return null;
        if (CACHE.containsKey(resourcePath)) return CACHE.get(resourcePath);
        if (Boolean.TRUE.equals(MISSED.get(resourcePath))) return null;

        try (InputStream in = ImageLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                MISSED.put(resourcePath, Boolean.TRUE);
                return null;
            }
            BufferedImage img = ImageIO.read(in);
            if (img != null) {
                CACHE.put(resourcePath, img);
                return img;
            }
        } catch (Exception ignored) {
            // 次回もnullを返す
        }
        MISSED.put(resourcePath, Boolean.TRUE);
        return null;
    }

    /**
     * 複数候補の中から、最初に見つかった画像を返す。すべて無ければnull。
     */
    public static BufferedImage loadAny(String... resourcePaths) {
        for (String p : resourcePaths) {
            BufferedImage img = load(p);
            if (img != null) return img;
        }
        return null;
    }
}
