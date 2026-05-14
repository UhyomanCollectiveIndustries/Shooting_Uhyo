package com.shootinguhyo.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/**
 * PixelSprite：ドット絵(ピクセルアート)を扱う簡易スプライト。
 *
 * 【役割】
 *  文字列の配列で「どこに何色のドットを置くか」を表し、画面に拡大して描画する。
 *  ドットエディタを別途用意しなくても、Javaコード内で見たまま編集できるのが利点。
 *
 * 【使い方】
 *  String[] pattern = {
 *      "..RR..",
 *      ".RWWR.",
 *      "RWWWWR",
 *      "RWWWWR",
 *      ".RWWR.",
 *      "..RR..",
 *  };
 *  Map<Character, Color> palette = Map.of('R', Color.RED, 'W', Color.WHITE);
 *  PixelSprite sprite = new PixelSprite(pattern, palette);
 *  sprite.draw(g, cx, cy, 4); // 中心(cx,cy)に4倍拡大で描画
 *
 *  '.' (ドット) は透明扱い。空白(' ')も透明扱い。
 *
 * 【なぜこの方式か】
 *  - 画像ファイルを別途用意しなくてよい (リソース管理が不要)
 *  - 学習用にコード内でドット絵が完結する
 *  - 後から本格的なPNG読み込みに置き換えやすい(SpritePaletteを差し替えるだけ)
 */
public class PixelSprite {
    private final int[][] pixels; // ARGB値の配列。0は完全透明
    private final int width;
    private final int height;

    /**
     * @param pattern 各文字列が1行のドット絵。すべて同じ長さである必要がある。
     * @param palette 文字→色 のマッピング。'.' と ' ' は自動的に透明扱い。
     */
    public PixelSprite(String[] pattern, Map<Character, Color> palette) {
        this.height = pattern.length;
        this.width = height > 0 ? pattern[0].length() : 0;
        this.pixels = new int[height][width];

        for (int y = 0; y < height; y++) {
            String row = pattern[y];
            for (int x = 0; x < row.length() && x < width; x++) {
                char ch = row.charAt(x);
                if (ch == '.' || ch == ' ') {
                    pixels[y][x] = 0; // 透明
                } else {
                    Color c = palette.get(ch);
                    pixels[y][x] = c != null ? c.getRGB() : 0;
                }
            }
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * 中心(cx,cy)を基準にscale倍で描画。
     * @param scale 1ドットあたり何ピクセルか (1なら原寸、2で2倍、…)
     */
    public void draw(Graphics2D g, double cx, double cy, int scale) {
        int drawX = (int)(cx - (width * scale) / 2.0);
        int drawY = (int)(cy - (height * scale) / 2.0);
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int argb = pixels[py][px];
                if ((argb >>> 24) == 0) continue; // 完全透明はスキップ
                g.setColor(new Color(argb, true));
                g.fillRect(drawX + px * scale, drawY + py * scale, scale, scale);
            }
        }
    }

    /**
     * パレットを書きやすくするためのヘルパー。
     * 例: PixelSprite.palette('R', Color.RED, 'G', Color.GREEN);
     */
    public static Map<Character, Color> palette(Object... kv) {
        if (kv.length % 2 != 0) {
            throw new IllegalArgumentException("palette()には文字と色のペアを渡してください");
        }
        Map<Character, Color> map = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put((Character) kv[i], (Color) kv[i + 1]);
        }
        return map;
    }
}
