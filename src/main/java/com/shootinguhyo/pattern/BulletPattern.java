package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import java.awt.Color;
import java.util.List;

public interface BulletPattern {
    List<EnemyBullet> generate(double x, double y, EnemyBullet.BulletSize size, Color color);
}
