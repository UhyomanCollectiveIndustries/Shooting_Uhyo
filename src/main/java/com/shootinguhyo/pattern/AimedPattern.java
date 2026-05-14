package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.util.MathUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class AimedPattern implements BulletPattern {
    private int ways;
    private double spreadAngle;
    private double speed;

    public AimedPattern(int ways, double spreadAngle, double speed) {
        this.ways = ways;
        this.spreadAngle = spreadAngle;
        this.speed = speed;
    }

    public List<EnemyBullet> generate(double x, double y, double targetX, double targetY,
                                       EnemyBullet.BulletSize size, Color color) {
        List<EnemyBullet> bullets = new ArrayList<>();
        double baseAngle = MathUtil.angle(x, y, targetX, targetY);

        for (int i = 0; i < ways; i++) {
            double offset = ways > 1 ? spreadAngle * (i - (ways - 1) / 2.0) : 0;
            double angle = baseAngle + offset;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            bullets.add(new EnemyBullet(x, y, vx, vy, size, color));
        }
        return bullets;
    }

    @Override
    public List<EnemyBullet> generate(double x, double y, EnemyBullet.BulletSize size, Color color) {
        return generate(x, y, x, y + 100, size, color);
    }
}
