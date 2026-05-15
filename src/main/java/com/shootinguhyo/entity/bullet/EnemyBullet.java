package com.shootinguhyo.entity.bullet;

import com.shootinguhyo.config.Difficulty;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * EnemyBullet：敵が撃つ弾。
 *
 * 【特徴】
 *  - サイズ(SMALL/MEDIUM/LARGE)を選べる。弾の半径が違うので避ける難易度が変わる
 *  - 4重円(暗→中間→明→白い反射)で描いて、立体感のある「球」に見せる
 *  - 色を指定して撃てるので、フェーズや弾種を見た目で区別しやすい
 *
 * 【なぜサイズをenumで分けるのか】
 *  半径を直接渡すよりも、用途(小さい掠り弾、中ボス級、大型弾)で区別する方が
 *  デザイン意図が伝わりやすい。
 */
public class EnemyBullet extends Bullet {
    public enum BulletSize { SMALL, MEDIUM, LARGE }

    private BulletSize size;
    private Color color;

    public EnemyBullet(double x, double y, double vx, double vy, BulletSize size, Color color) {
        super(x, y, vx * Difficulty.current().bulletSpeedMul,
                    vy * Difficulty.current().bulletSpeedMul,
                    getRadiusForSize(size));
        this.size = size;
        this.color = color;
    }

    /** サイズに応じた半径を返す。staticにしているのはコンストラクタ内から呼ぶため。 */
    private static double getRadiusForSize(BulletSize size) {
        return switch (size) {
            case SMALL -> 5;
            case MEDIUM -> 8;
            case LARGE -> 12;
        };
    }

    /**
     * 敵弾を「光る球」として描く。4層構成：
     *  1. 外側の暗い円(輪郭になる)
     *  2. 元の色の円(70%サイズ)
     *  3. 明るい色の円(35%サイズ)
     *  4. 中央の白いハイライト(15%サイズ)
     * これで球がリアルに光って見える。
     */
    @Override
    public void draw(Graphics2D g) {
        float r = (float) radius;
        Color bright = color.brighter().brighter();
        Color dark = color.darker().darker();

        g.setColor(dark);
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));

        g.setColor(color);
        g.fill(new Ellipse2D.Double(x - r * 0.7, y - r * 0.7, r * 1.4, r * 1.4));

        g.setColor(bright);
        g.fill(new Ellipse2D.Double(x - r * 0.35, y - r * 0.35, r * 0.7, r * 0.7));

        // 中央の小さな白い光点でツヤを表現
        g.setColor(new Color(255, 255, 255, 200));
        g.fill(new Ellipse2D.Double(x - r * 0.15, y - r * 0.15, r * 0.3, r * 0.3));
    }
}
