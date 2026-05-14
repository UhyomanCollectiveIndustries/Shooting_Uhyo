package com.shootinguhyo.entity.bullet;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class PlayerBullet extends Bullet {
    private int damage;

    public PlayerBullet(double x, double y, double vx, double vy, int damage) {
        super(x, y, vx, vy, 4);
        this.damage = damage;
    }

    public int getDamage() { return damage; }

    @Override
    public void draw(Graphics2D g) {
        int w = 4, h = 12;
        g.setColor(new Color(0, 255, 255, 220));
        g.fill(new Ellipse2D.Double(x - w / 2.0, y - h / 2.0, w, h));
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(x - 1, y - 4, 2, 8));
    }
}
