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

    /** これより小さいアルファ値は「透明(=余白)」とみなす閾値。 */
    private static final int ALPHA_THRESHOLD = 16;

    /**
     * 透過PNGの「中身(不透明な部分)」を検出し、正方形キャンバスの中央に再配置する。
     *
     * <p>顔アイコン画像が右下や上にズレて切り取られていても、
     *  不透明ピクセルのバウンディングボックスを求めて余白をトリミング → 正方形の中央に置き直す。
     *  これにより、どの画像も会話ウィンドウで中央に揃って表示される。</p>
     *
     * <p>透過情報を持たない画像(不透明なJPG等)や、全面が不透明/透明な画像は
     *  検出のしようがないため、元画像をそのまま返す。</p>
     *
     * @param src 元画像(nullならnull)
     * @return 中央寄せした正方形画像。処理不要・不能なら元画像。
     */
    public static BufferedImage autoCenter(BufferedImage src) {
        if (src == null) return null;
        if (!src.getColorModel().hasAlpha()) return src; // 透過なし → 検出不能

        int w = src.getWidth();
        int h = src.getHeight();
        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int alpha = (src.getRGB(x, y) >>> 24);
                if (alpha >= ALPHA_THRESHOLD) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
            }
        }

        // 不透明ピクセルが無い(完全透明)→ そのまま返す
        if (maxX < minX || maxY < minY) return src;

        int contentW = maxX - minX + 1;
        int contentH = maxY - minY + 1;

        // 既にほぼ中央でぴったり収まっているなら作り直さない(無駄な拡大を防ぐ)
        boolean tightAndCentered = minX <= 1 && minY <= 1
                && (w - 1 - maxX) <= 1 && (h - 1 - maxY) <= 1;
        if (tightAndCentered && contentW == contentH) return src;

        // 正方形キャンバス(中身の長辺基準。少しだけ余白を足す)に中央配置
        int side = Math.max(contentW, contentH);
        int pad = Math.max(1, side / 16);
        side += pad * 2;

        BufferedImage out = new BufferedImage(side, side, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = out.createGraphics();
        int dx = (side - contentW) / 2;
        int dy = (side - contentH) / 2;
        g.drawImage(src,
                dx, dy, dx + contentW, dy + contentH,           // 配置先
                minX, minY, minX + contentW, minY + contentH,   // 切り出し元
                null);
        g.dispose();
        return out;
    }
}
