package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class SpiralPattern implements BulletPattern {
    private double angleStep;
    private int bulletCount;
    private double speed;
    private double currentAngle;

    public SpiralPattern(double angleStep, int bulletCount, double speed) {
        this.angleStep = angleStep;
        this.bulletCount = bulletCount;
        this.speed = speed;
        this.currentAngle = 0;
    }

    @Override
    public List<EnemyBullet> generate(double x, double y, EnemyBullet.BulletSize size, Color color) {
        List<EnemyBullet> bullets = new ArrayList<>();
        for (int i = 0; i < bulletCount; i++) {
            double angle = currentAngle + Math.PI * 2 / bulletCount * i;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bullets.add(new EnemyBullet(x, y, vx, vy, size, color));
        }
        currentAngle += angleStep;
        return bullets;
    }

    public void resetAngle() { currentAngle = 0; }
}
