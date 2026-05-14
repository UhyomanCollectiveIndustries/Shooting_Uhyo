package com.shootinguhyo.entity.bullet;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class EnemyBullet extends Bullet {
    public enum BulletSize { SMALL, MEDIUM, LARGE }

    private BulletSize size;
    private Color color;

    public EnemyBullet(double x, double y, double vx, double vy, BulletSize size, Color color) {
        super(x, y, vx, vy, getRadiusForSize(size));
        this.size = size;
        this.color = color;
    }

    private static double getRadiusForSize(BulletSize size) {
        return switch (size) {
            case SMALL -> 5;
            case MEDIUM -> 8;
            case LARGE -> 12;
        };
    }

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

        g.setColor(new Color(255, 255, 255, 200));
        g.fill(new Ellipse2D.Double(x - r * 0.15, y - r * 0.15, r * 0.3, r * 0.3));
    }
}
