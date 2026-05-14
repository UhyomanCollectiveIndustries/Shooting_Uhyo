package com.shootinguhyo.entity.bullet;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * PlayerBullet：自機が撃つ弾。
 *
 * 【特徴】
 *  - 縦長(4×12)の楕円形で描画。シューティングらしい「レーザー風」の見た目
 *  - damage(攻撃力)を持ち、当たった敵にこの値分のダメージを与える
 *
 * 【なぜradius=4？】
 *  当たり判定の半径。敵への当たり判定を「距離 < 12」で行うので、
 *  実質的にはGamePanel側の判定式の方が支配的。
 */
public class PlayerBullet extends Bullet {
    private int damage;

    public PlayerBullet(double x, double y, double vx, double vy, int damage) {
        super(x, y, vx, vy, 4);
        this.damage = damage;
    }

    public int getDamage() { return damage; }

    /**
     * 自機弾の描画。
     * 外側にやや透明な水色の縦長楕円、中央に白の細い芯を重ねて
     * 光るレーザーらしさを表現している。
     */
    @Override
    public void draw(Graphics2D g) {
        int w = 4, h = 12;
        g.setColor(new Color(0, 255, 255, 220));
        g.fill(new Ellipse2D.Double(x - w / 2.0, y - h / 2.0, w, h));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(x - 1, y - 4, 2, 8));
    }
}
