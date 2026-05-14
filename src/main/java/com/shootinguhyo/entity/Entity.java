package com.shootinguhyo.entity;

import java.awt.Graphics2D;

public abstract class Entity {
    public double x, y;
    public boolean active = true;

    public Entity(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public abstract void update();
    public abstract void draw(Graphics2D g);
}
