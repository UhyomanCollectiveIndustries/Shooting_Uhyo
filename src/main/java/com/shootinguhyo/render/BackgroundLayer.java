package com.shootinguhyo.render;

import java.awt.*;
import java.util.Random;

/**
 * BackgroundLayer：ステージ背景のアニメーションを担当するクラス(ひな型)。
 *
 * 【役割】
 *  既存のGamePanelが持つ「星流れ」処理を切り出して独立クラスにし、
 *  ステージごとに違う背景アニメーションへ差し替えられるようにする。
 *
 * 【設計】
 *  - レイヤー(layer)を複数持ち、奥(layer=0)から手前(layer=N)へ描画
 *  - 各レイヤーで流れる速さを変えると「パララックススクロール」になり、奥行きが出る
 *
 * 【ステージ毎の例(TODO)】
 *  - Stage1 : 星空(現状の処理を流用)
 *  - Stage2 : 雲が流れる空
 *  - Stage3 : 夕焼け＋光の粒
 *  - Stage4 : 夜の森(横スクロール木々)
 *  - Stage5 : 雷雨(稲妻)
 *  - Stage6 : 宇宙＋オーロラ
 */
public class BackgroundLayer {

    private final int width, height;
    private final int particleCount;

    private final double[] px;
    private final double[] py;
    private final double[] speed;
    private final int[] brightness;

    private final Random rand = new Random();

    /**
     * @param width 描画領域の幅
     * @param height 描画領域の高さ
     * @param particleCount 流す粒の数(星や雲など)
     */
    public BackgroundLayer(int width, int height, int particleCount) {
        this.width = width;
        this.height = height;
        this.particleCount = particleCount;
        this.px = new double[particleCount];
        this.py = new double[particleCount];
        this.speed = new double[particleCount];
        this.brightness = new int[particleCount];
        reset();
    }

    /** 粒の位置・速度・明るさをランダムに撒く。 */
    public void reset() {
        for (int i = 0; i < particleCount; i++) {
            px[i] = rand.nextInt(width);
            py[i] = rand.nextInt(height);
            speed[i] = rand.nextDouble() * 1.5 + 0.3;
            brightness[i] = rand.nextInt(180) + 60;
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

    /** 粒を点として描画。速い粒は2x2、遅い粒は1x1にして遠近感を出す。 */
    public void draw(Graphics2D g) {
        for (int i = 0; i < particleCount; i++) {
            int b = brightness[i];
            g.setColor(new Color(b, b, b));
            int size = speed[i] > 1.2 ? 2 : 1;
            g.fillRect((int) px[i], (int) py[i], size, size);
        }
    }
}
