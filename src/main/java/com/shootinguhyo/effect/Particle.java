package com.shootinguhyo.effect;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

public class Particle {
    public double x, y;
    public double vx, vy;
    public int life, maxLife;
    public Color color;
    public double radius;
    public boolean active = true;

    private static final Random rand = new Random();

    public Particle(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        double angle = rand.nextDouble() * Math.PI * 2;
        double speed = rand.nextDouble() * 4 + 1;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.life = rand.nextInt(20) + 15;
        this.maxLife = this.life;
        this.radius = rand.nextDouble() * 3 + 2;
    }

    public void update() {
        x += vx;
        y += vy;
        vx *= 0.95;
        vy *= 0.95;
        life--;
        if (life <= 0) active = false;
    }

    public void draw(Graphics2D g) {
        float alpha = (float) life / maxLife;
        Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 200));
        g.setColor(c);
        double r = radius * alpha;
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
    }
}
