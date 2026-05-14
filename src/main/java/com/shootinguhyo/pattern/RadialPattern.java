package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class RadialPattern implements BulletPattern {
    private int directions;
    private double speed;
    private double angleOffset;

    public RadialPattern(int directions, double speed, double angleOffset) {
        this.directions = directions;
        this.speed = speed;
        this.angleOffset = angleOffset;
    }

    @Override
    public List<EnemyBullet> generate(double x, double y, EnemyBullet.BulletSize size, Color color) {
        List<EnemyBullet> bullets = new ArrayList<>();
        for (int i = 0; i < directions; i++) {
            double angle = Math.PI * 2 / directions * i + angleOffset;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bullets.add(new EnemyBullet(x, y, vx, vy, size, color));
        }
        return bullets;
    }
}
