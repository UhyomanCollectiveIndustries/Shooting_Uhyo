package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.RadialPattern;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class Enemy extends Entity {
    protected int hp;
    protected int maxHp;
    protected int score;
    protected int frame = 0;
    protected double startX;
    protected List<EnemyBullet> newBullets = new ArrayList<>();

    public Enemy(double x, double y, int hp, int score) {
        super(x, y);
        this.hp = hp;
        this.maxHp = hp;
        this.score = score;
        this.startX = x;
    }

    @Override
    public void update() {
        frame++;
        y += 0.8;
        x = startX + Math.sin(frame * 0.03) * 40;

        if (frame % 90 == 45 && hp > 0) {
            RadialPattern pattern = new RadialPattern(8, 2.0, frame * 0.1);
            newBullets.addAll(pattern.generate(x, y, EnemyBullet.BulletSize.SMALL, new Color(200, 100, 255)));
        }

        if (y > 500) active = false;
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
        int size = 10;
        Path2D diamond = new Path2D.Double();
        diamond.moveTo(x, y - size);
        diamond.lineTo(x + size, y);
        diamond.lineTo(x, y + size);
        diamond.lineTo(x - size, y);
        diamond.closePath();

        g.setColor(new Color(150, 50, 220));
        g.fill(diamond);
        g.setColor(new Color(200, 100, 255));
        g.draw(diamond);

        int is = 5;
        Path2D inner = new Path2D.Double();
        inner.moveTo(x, y - is);
        inner.lineTo(x + is, y);
        inner.lineTo(x, y + is);
        inner.lineTo(x - is, y);
        inner.closePath();
        g.setColor(new Color(220, 150, 255));
        g.fill(inner);
    }
}
