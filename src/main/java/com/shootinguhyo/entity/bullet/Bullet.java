package com.shootinguhyo.entity.bullet;

import com.shootinguhyo.entity.Entity;

public abstract class Bullet extends Entity {
    protected double vx, vy;
    protected double radius;

    public Bullet(double x, double y, double vx, double vy, double radius) {
        super(x, y);
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
    }

    public double getRadius() { return radius; }

    @Override
    public void update() {
        x += vx;
        y += vy;
        if (x < -50 || x > 500 || y < -50 || y > 550) {
            active = false;
        }
    }
}
