package com.shootinguhyo.entity;

import com.shootinguhyo.InputHandler;
import com.shootinguhyo.entity.bullet.PlayerBullet;
import com.shootinguhyo.util.MathUtil;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {
    public static final int FIELD_WIDTH = 384;
    public static final int FIELD_HEIGHT = 448;
    private static final double NORMAL_SPEED = 5.0;
    private static final double FOCUS_SPEED = 2.5;
    private static final double HITBOX_RADIUS = 4.0;

    private int lives = 3;
    private int bombs = 3;
    private int power = 0;
    private long score = 0;
    private int graze = 0;

    private int shootCooldown = 0;
    private int invincibleFrames = 0;
    private int bombFrames = 0;
    private boolean bombing = false;

    private List<PlayerBullet> newBullets = new ArrayList<>();

    public Player(double x, double y) {
        super(x, y);
    }

    public void update(InputHandler input) {
        boolean focus = input.isDown(KeyEvent.VK_SHIFT);
        double speed = focus ? FOCUS_SPEED : NORMAL_SPEED;

        double dx = 0, dy = 0;
        if (input.isDown(KeyEvent.VK_LEFT) || input.isDown(KeyEvent.VK_A)) dx -= speed;
        if (input.isDown(KeyEvent.VK_RIGHT) || input.isDown(KeyEvent.VK_D)) dx += speed;
        if (input.isDown(KeyEvent.VK_UP) || input.isDown(KeyEvent.VK_W)) dy -= speed;
        if (input.isDown(KeyEvent.VK_DOWN) || input.isDown(KeyEvent.VK_S)) dy += speed;

        if (dx != 0 && dy != 0) {
            dx *= 0.7071;
            dy *= 0.7071;
        }

        x = MathUtil.clamp(x + dx, HITBOX_RADIUS, FIELD_WIDTH - HITBOX_RADIUS);
        y = MathUtil.clamp(y + dy, HITBOX_RADIUS, FIELD_HEIGHT - HITBOX_RADIUS);

        if (input.isDown(KeyEvent.VK_Z) && shootCooldown <= 0) {
            shoot(focus);
            shootCooldown = 5;
        }
        if (shootCooldown > 0) shootCooldown--;

        if (input.isJustPressed(KeyEvent.VK_X) && bombs > 0 && bombFrames <= 0) {
            bombs--;
            bombFrames = 180;
            bombing = true;
            invincibleFrames = Math.max(invincibleFrames, 180);
        }
        if (bombFrames > 0) bombFrames--;
        else bombing = false;

        if (invincibleFrames > 0) invincibleFrames--;
    }

    private void shoot(boolean focus) {
        int dmg = 10;
        if (focus) {
            newBullets.add(new PlayerBullet(x, y - 10, 0, -15, dmg * 2));
            newBullets.add(new PlayerBullet(x - 3, y - 10, 0, -15, dmg * 2));
            newBullets.add(new PlayerBullet(x + 3, y - 10, 0, -15, dmg * 2));
        } else {
            int ways = getWayCount();
            double baseSpeed = 12.0;
            double spread = Math.toRadians(10);
            for (int i = 0; i < ways; i++) {
                double angle = -Math.PI / 2;
                if (ways > 1) {
                    angle += spread * (i - (ways - 1) / 2.0);
                }
                double vx = Math.cos(angle) * baseSpeed;
                double vy = Math.sin(angle) * baseSpeed;
                newBullets.add(new PlayerBullet(x, y - 5, vx, vy, dmg));
            }
        }
    }

    private int getWayCount() {
        if (power < 100) return 2;
        if (power < 200) return 3;
        if (power < 300) return 4;
        if (power < 400) return 5;
        return 6;
    }

    public boolean isInvincible() { return invincibleFrames > 0; }
    public boolean isBombing() { return bombing; }

    public void hit() {
        if (isInvincible()) return;
        lives--;
        power = power / 2;
        invincibleFrames = 180;
        bombing = false;
        bombFrames = 0;
    }

    public List<PlayerBullet> getAndClearNewBullets() {
        List<PlayerBullet> bullets = new ArrayList<>(newBullets);
        newBullets.clear();
        return bullets;
    }

    public void addPower(int amount) { power = Math.min(400, power + amount); }
    public void addScore(long amount) { score += amount; }
    public void addGraze(int amount) { graze += amount; }

    public int getLives() { return lives; }
    public int getBombs() { return bombs; }
    public int getPower() { return power; }
    public long getScore() { return score; }
    public int getGraze() { return graze; }
    public double getHitboxRadius() { return HITBOX_RADIUS; }
    public boolean isAlive() { return lives > 0; }

    @Override
    public void update() { /* use update(InputHandler) instead */ }

    @Override
    public void draw(Graphics2D g) {
        if (invincibleFrames > 0 && (invincibleFrames / 5) % 2 == 0 && !bombing) return;

        Path2D triangle = new Path2D.Double();
        triangle.moveTo(x, y - 14);
        triangle.lineTo(x - 10, y + 10);
        triangle.lineTo(x + 10, y + 10);
        triangle.closePath();

        g.setColor(new Color(180, 240, 255));
        g.fill(triangle);
        g.setColor(Color.WHITE);
        g.draw(triangle);

        Path2D inner = new Path2D.Double();
        inner.moveTo(x, y - 8);
        inner.lineTo(x - 5, y + 5);
        inner.lineTo(x + 5, y + 5);
        inner.closePath();
        g.setColor(new Color(0, 200, 255));
        g.fill(inner);
    }

    public void drawHitbox(Graphics2D g) {
        g.setColor(new Color(255, 0, 0, 180));
        g.fill(new Ellipse2D.Double(x - HITBOX_RADIUS, y - HITBOX_RADIUS,
                HITBOX_RADIUS * 2, HITBOX_RADIUS * 2));
        g.setColor(Color.RED);
        g.draw(new Ellipse2D.Double(x - HITBOX_RADIUS, y - HITBOX_RADIUS,
                HITBOX_RADIUS * 2, HITBOX_RADIUS * 2));
    }

    public void drawBomb(Graphics2D g, int fieldWidth, int fieldHeight) {
        if (!bombing) return;
        float alpha = Math.min(1.0f, bombFrames / 30.0f);
        g.setColor(new Color(1.0f, 1.0f, 1.0f, alpha * 0.5f));
        g.fillRect(0, 0, fieldWidth, fieldHeight);
    }
}
