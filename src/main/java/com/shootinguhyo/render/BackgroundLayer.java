package com.shootinguhyo.render;

import java.awt.*;
import java.util.Random;

/**
 * BackgroundLayer：ステージ背景のアニメーションを担当する1枚のレイヤー。
 *
 * 【役割】
 *  「星流れ」のような粒の流れを独立して持つ。複数枚を流れる速さ違いで重ねると
 *  「パララックススクロール」になり奥行きが出る({@link ParallaxBackground} がまとめる)。
 *
 * 【設計】
 *  - 各レイヤーは固有の速度帯(speedMin〜speedMax)・粒サイズ・色・明るさ帯を持つ
 *  - 奥のレイヤーは遅く小さく暗め、手前のレイヤーは速く大きく明るめにする
 */
public class BackgroundLayer {

    private final int width, height;
    private final int particleCount;

    private final double[] px;
    private final double[] py;
    private final double[] speed;
    private final int[] brightness;

    private final double speedMin, speedMax;
    private final int dotSize;
    private final int brightMin, brightMax;
    /** 粒の色。nullなら明るさ(brightness)に基づくグレースケール。 */
    private final Color color;

    private final Random rand = new Random();

    /**
     * 従来互換コンストラクタ(白〜灰のグレースケール星)。
     */
    public BackgroundLayer(int width, int height, int particleCount) {
        this(width, height, particleCount, 0.3, 1.8, 1, 60, 240, null);
    }

    /**
     * 詳細指定コンストラクタ。
     * @param speedMin/speedMax 流れる速度の範囲(下方向px/フレーム)
     * @param dotSize 粒の基本サイズ(px)。速い粒は+1される
     * @param brightMin/brightMax 明るさ(0-255)の範囲
     * @param color 粒の色(nullならグレースケール)
     */
    public BackgroundLayer(int width, int height, int particleCount,
                           double speedMin, double speedMax, int dotSize,
                           int brightMin, int brightMax, Color color) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.particleCount = Math.max(0, particleCount);
        this.speedMin = speedMin;
        this.speedMax = Math.max(speedMin, speedMax);
        this.dotSize = Math.max(1, dotSize);
        this.brightMin = brightMin;
        this.brightMax = Math.max(brightMin, brightMax);
        this.color = color;
        this.px = new double[this.particleCount];
        this.py = new double[this.particleCount];
        this.speed = new double[this.particleCount];
        this.brightness = new int[this.particleCount];
        reset();
    }

    /** 粒の位置・速度・明るさをランダムに撒く。 */
    public void reset() {
        for (int i = 0; i < particleCount; i++) {
            px[i] = rand.nextInt(width);
            py[i] = rand.nextInt(height);
            speed[i] = rand.nextDouble() * (speedMax - speedMin) + speedMin;
            brightness[i] = rand.nextInt(Math.max(1, brightMax - brightMin + 1)) + brightMin;
        }
    }

    /** 1フレーム分、粒を下方向に流す。 */
    public void update() {
        for (int i = 0; i < particleCount; i++) {
            py[i] += speed[i];
            if (py[i] > height) {
                py[i] = 0;
                px[i] = rand.nextInt(width);
            }
        }
    }

    /** 粒を描画。速い粒は1px大きくして遠近感を出す。 */
    public void draw(Graphics2D g) {
        for (int i = 0; i < particleCount; i++) {
            int b = brightness[i];
            if (color == null) {
                g.setColor(new Color(b, b, b));
            } else {
                // 明るさを色に乗せる(暗い粒は色も沈む)。アルファで奥行きも表現。
                float k = b / 255f;
                g.setColor(new Color(
                        (int) (color.getRed() * k),
                        (int) (color.getGreen() * k),
                        (int) (color.getBlue() * k),
                        Math.min(255, b + 30)));
            }
            int size = speed[i] > (speedMin + speedMax) / 2 ? dotSize + 1 : dotSize;
            g.fillRect((int) px[i], (int) py[i], size, size);
        }
    }
}
