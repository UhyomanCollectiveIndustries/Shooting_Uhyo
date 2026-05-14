package com.shootinguhyo.effect;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * BackgroundAnimator：背景アニメーションのひな型。
 *
 * 【役割】
 *  既存の星空に加えて、ステージごとに異なる「動く背景」を提供する。
 *  例: 流れ星、雲、桜の花びら、雨など。
 *
 * 【現在の実装】
 *  斜めにスクロールする「流れ星」風の長い線を多数表示するだけのデモ。
 *  ステージごとにこのクラスを継承して見た目を切り替える想定。
 *
 * 【TODO】
 *  - ステージ別の派生クラス(ForestBackground, NightBackground 等)
 *  - 桜の花びら、雨、雪などのパーティクル背景
 *  - 「奥行きパララックス(遠/近で異なる速度)」の追加
 */
public class BackgroundAnimator {
    private static final int STREAK_COUNT = 30;
    private final double[] sx = new double[STREAK_COUNT];
    private final double[] sy = new double[STREAK_COUNT];
    private final double[] speed = new double[STREAK_COUNT];
    private final int[] length = new int[STREAK_COUNT];
    private final Random rand = new Random();
    private final int fieldWidth;
    private final int fieldHeight;

    public BackgroundAnimator(int fieldWidth, int fieldHeight) {
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
        for (int i = 0; i < STREAK_COUNT; i++) {
            sx[i] = rand.nextInt(fieldWidth);
            sy[i] = rand.nextInt(fieldHeight);
            speed[i] = rand.nextDouble() * 3 + 1;
            length[i] = rand.nextInt(15) + 5;
        }
    }

    public void update() {
        for (int i = 0; i < STREAK_COUNT; i++) {
            sx[i] -= speed[i] * 0.5; // 斜めの動き
            sy[i] += speed[i];
            if (sy[i] > fieldHeight + 20 || sx[i] < -20) {
                sx[i] = rand.nextInt(fieldWidth) + 20;
                sy[i] = -20;
            }
        }
    }

    public void draw(Graphics2D g) {
        for (int i = 0; i < STREAK_COUNT; i++) {
            float a = Math.min(1.0f, (float) speed[i] / 4.0f);
            g.setColor(new Color(0.6f, 0.7f, 1.0f, a));
            int x1 = (int) sx[i];
            int y1 = (int) sy[i];
            int x2 = (int) (sx[i] + speed[i] * 0.5 * length[i] / speed[i]);
            int y2 = (int) (sy[i] - length[i]);
            g.drawLine(x1, y1, x2, y2);
        }
    }
}
