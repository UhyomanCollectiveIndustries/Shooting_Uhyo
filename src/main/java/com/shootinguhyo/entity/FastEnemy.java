package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.AimedPattern;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class FastEnemy extends Entity {
    private double vx, vy;
    private int frame = 0;
    private boolean firedShot = false;
    private double playerX, playerY;
    private List<EnemyBullet> newBullets = new ArrayList<>();
    private int score = 150;
    private int hp = 75;

    public FastEnemy(double x, double y, double vx, double vy) {
        super(x, y);
        this.vx = vx;
        this.vy = vy;
    }

    public void setPlayerPosition(double px, double py) {
        this.playerX = px;
        this.playerY = py;
    }

    @Override
    public void update() {
        frame++;
        x += vx;
        y += vy;

        if (!firedShot && y > 0 && y < 448 && x > 0 && x < 384) {
            firedShot = true;
            AimedPattern pattern = new AimedPattern(3, Math.toRadians(15), 3.5);
            newBullets.addAll(pattern.generate(x, y, playerX, playerY,
                    EnemyBullet.BulletSize.SMALL, new Color(0, 220, 220)));
        }

        if (x < -50 || x > 450 || y < -50 || y > 510) active = false;
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            active = false;
        }
    }

    public List<EnemyBullet> getAndClearNewBullets() {
        List<EnemyBullet> bullets = new ArrayList<>(newBullets);
        newBullets.clear();
        return bullets;
    }

    public boolean isDefeated() { return hp <= 0; }
    public int getScore() { return score; }
    public int getHp() { return hp; }

    @Override
    public void draw(Graphics2D g) {
        int size = 7;
        Path2D diamond = new Path2D.Double();
        diamond.moveTo(x, y - size);
        diamond.lineTo(x + size, y);
        diamond.lineTo(x, y + size);
        diamond.lineTo(x - size, y);
        diamond.closePath();

        g.setColor(new Color(0, 200, 220));
        g.fill(diamond);
        g.setColor(Color.CYAN);
        g.draw(diamond);

        g.setColor(new Color(0, 255, 255, 100));
        g.drawLine((int)(x - vx * 3), (int)(y - vy * 3), (int)x, (int)y);
    }
}
