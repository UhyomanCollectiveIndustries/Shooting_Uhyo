package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class WavePattern implements BulletPattern {
    private double spreadAngle;
    private int bulletCount;
    private double waveFrequency;

    public WavePattern(double spreadAngle, int bulletCount, double waveFrequency) {
        this.spreadAngle = spreadAngle;
        this.bulletCount = bulletCount;
        this.waveFrequency = waveFrequency;
    }

    @Override
    public List<EnemyBullet> generate(double x, double y, EnemyBullet.BulletSize size, Color color) {
        List<EnemyBullet> bullets = new ArrayList<>();
        double baseAngle = Math.PI / 2 + Math.sin(waveFrequency) * 0.5;
        double speed = 3.0;

        for (int i = 0; i < bulletCount; i++) {
            double t = bulletCount > 1 ? (double) i / (bulletCount - 1) : 0.5;
            double angle = baseAngle - spreadAngle / 2 + spreadAngle * t;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bullets.add(new EnemyBullet(x, y, vx, vy, size, color));
        }
        return bullets;
    }
}
