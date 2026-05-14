package com.shootinguhyo.entity;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public class Item extends Entity {
    public enum ItemType { POWER, POINT }

    private ItemType type;
    private double vy = 1.5;
    private int frame = 0;

    public Item(double x, double y, ItemType type) {
        super(x, y);
        this.type = type;
    }

    @Override
    public void update() {
        frame++;
        y += vy;
        if (y > 480) active = false;
    }

    public ItemType getType() { return type; }
    public double getRadius() { return 8; }

    @Override
    public void draw(Graphics2D g) {
        if (type == ItemType.POWER) {
            g.setColor(new Color(50, 100, 255));
            g.fill(new Ellipse2D.Double(x - 8, y - 8, 16, 16));
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 9));
            g.drawString("P", (int)x - 3, (int)y + 3);
        } else {
            g.setColor(new Color(255, 220, 0));
            drawStar(g, (int)x, (int)y, 8, 4, 5);
        }
    }

    private void drawStar(Graphics2D g, int cx, int cy, int outerR, int innerR, int points) {
        Path2D star = new Path2D.Double();
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI / points * i - Math.PI / 2;
            double r = (i % 2 == 0) ? outerR : innerR;
            double px = cx + Math.cos(angle) * r;
            double py = cy + Math.sin(angle) * r;
            if (i == 0) star.moveTo(px, py);
            else star.lineTo(px, py);
        }
        star.closePath();
        g.fill(star);
    }
}
