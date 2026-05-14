package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.*;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class Boss extends Entity {
    public enum Phase { PHASE1, SPELL1, SPELL2, SPELL3, DEFEATED }

    private Phase phase = Phase.PHASE1;
    private int phase1Hp = 500;
    private int spell1Hp = 400;
    private int spell2Hp = 400;
    private int spell3Hp = 600;
    private int currentHp;
    private int maxHp;

    private int frame = 0;
    private int spellTimer = 0;
    private int spellMaxTime = 0;
    private double moveDir = 1;
    private double playerX, playerY;
    private List<EnemyBullet> newBullets = new ArrayList<>();

    private double spiralAngle1 = 0;
    private double spiralAngle2 = Math.PI;
    private double waveAngle = 0;
    private double radialAngle = 0;

    public Boss(double x, double y) {
        super(x, y);
        currentHp = phase1Hp;
        maxHp = phase1Hp;
    }

    public void setPlayerPosition(double px, double py) {
        this.playerX = px;
        this.playerY = py;
    }

    @Override
    public void update() {
        if (phase == Phase.DEFEATED) return;

        frame++;
        updateMovement();
        updatePattern();
        checkPhaseTransition();
    }

    private void updateMovement() {
        x += moveDir * 1.5;
        if (x > 340) moveDir = -1;
        if (x < 44) moveDir = 1;
        y = 80 + Math.sin(frame * 0.02) * 20;
    }

    private void updatePattern() {
        switch (phase) {
            case PHASE1 -> updatePhase1();
            case SPELL1 -> updateSpell1();
            case SPELL2 -> updateSpell2();
            case SPELL3 -> updateSpell3();
            default -> {}
        }
        if (spellTimer > 0) spellTimer--;
    }

    private void updatePhase1() {
        if (frame % 180 == 0) {
            RadialPattern rp = new RadialPattern(8, 3.0, radialAngle);
            newBullets.addAll(rp.generate(x, y, EnemyBullet.BulletSize.MEDIUM, new Color(255, 100, 100)));
            radialAngle += 0.2;
        }
    }

    private void updateSpell1() {
        spiralAngle1 += 0.08;
        spiralAngle2 -= 0.08;

        if (frame % 3 == 0) {
            double spd = 2.5;
            for (int i = 0; i < 3; i++) {
                double a = spiralAngle1 + Math.PI * 2 / 3 * i;
                newBullets.add(new EnemyBullet(x, y,
                        Math.cos(a) * spd, Math.sin(a) * spd,
                        EnemyBullet.BulletSize.SMALL, new Color(255, 150, 200)));
            }
            for (int i = 0; i < 3; i++) {
                double a = spiralAngle2 + Math.PI * 2 / 3 * i;
                newBullets.add(new EnemyBullet(x, y,
                        Math.cos(a) * spd, Math.sin(a) * spd,
                        EnemyBullet.BulletSize.SMALL, new Color(150, 200, 255)));
            }
        }
    }

    private void updateSpell2() {
        waveAngle += 0.05;
        if (frame % 20 == 0) {
            WavePattern wp = new WavePattern(Math.toRadians(60), 7, waveAngle);
            newBullets.addAll(wp.generate(x, y, EnemyBullet.BulletSize.SMALL, new Color(255, 150, 180)));
        }
        if (frame % 60 == 30) {
            AimedPattern ap = new AimedPattern(3, Math.toRadians(20), 3.0);
            newBullets.addAll(ap.generate(x, y, playerX, playerY,
                    EnemyBullet.BulletSize.MEDIUM, new Color(255, 200, 220)));
        }
    }

    private void updateSpell3() {
        radialAngle += 0.05;
        spiralAngle1 += 0.12;

        if (frame % 60 == 0) {
            RadialPattern rp = new RadialPattern(16, 3.5, radialAngle);
            newBullets.addAll(rp.generate(x, y, EnemyBullet.BulletSize.MEDIUM, new Color(255, 220, 100)));
        }
        if (frame % 4 == 0) {
            double spd = 3.0;
            for (int i = 0; i < 4; i++) {
                double a = spiralAngle1 + Math.PI / 2 * i;
                newBullets.add(new EnemyBullet(x, y,
                        Math.cos(a) * spd, Math.sin(a) * spd,
                        EnemyBullet.BulletSize.SMALL, new Color(255, 255, 200)));
            }
        }
        if (frame % 90 == 45) {
            AimedPattern ap = new AimedPattern(5, Math.toRadians(15), 4.0);
            newBullets.addAll(ap.generate(x, y, playerX, playerY,
                    EnemyBullet.BulletSize.SMALL, new Color(200, 255, 200)));
        }
    }

    private void checkPhaseTransition() {
        if (phase == Phase.PHASE1 && currentHp <= 0) {
            transitionTo(Phase.SPELL1, 400, 20 * 60);
        } else if (phase == Phase.SPELL1 && (currentHp <= 0 || spellTimer <= 0)) {
            transitionTo(Phase.SPELL2, 400, 20 * 60);
        } else if (phase == Phase.SPELL2 && (currentHp <= 0 || spellTimer <= 0)) {
            transitionTo(Phase.SPELL3, 600, 30 * 60);
        } else if (phase == Phase.SPELL3 && (currentHp <= 0 || spellTimer <= 0)) {
            phase = Phase.DEFEATED;
            active = false;
        }
    }

    private void transitionTo(Phase newPhase, int hp, int timer) {
        phase = newPhase;
        currentHp = hp;
        maxHp = hp;
        spellTimer = timer;
        spellMaxTime = timer;
        frame = 0;
        newBullets.clear();
    }

    public void takeDamage(int dmg) {
        if (phase == Phase.DEFEATED) return;
        currentHp -= dmg;
        if (currentHp < 0) currentHp = 0;
    }

    public List<EnemyBullet> getAndClearNewBullets() {
        List<EnemyBullet> bullets = new ArrayList<>(newBullets);
        newBullets.clear();
        return bullets;
    }

    public Phase getPhase() { return phase; }
    public int getCurrentHp() { return currentHp; }
    public int getMaxHp() { return maxHp; }
    public int getSpellTimer() { return spellTimer; }
    public int getSpellMaxTime() { return spellMaxTime; }
    public boolean isDefeated() { return phase == Phase.DEFEATED; }

    public String getSpellName() {
        return switch (phase) {
            case SPELL1 -> "\u5922\u7b26\u300e\u5e7b\u60f3\u306e\u5f3e\u5e55\u300f";
            case SPELL2 -> "\u82b1\u7b26\u300e\u685c\u5439\u96ea\u300f";
            case SPELL3 -> "\u9748\u7b26\u300e\u5e7b\u60f3\u306e\u7d50\u754c\u300f";
            default -> "";
        };
    }

    public Color getSpellColor() {
        return switch (phase) {
            case SPELL1 -> new Color(255, 150, 200, 60);
            case SPELL2 -> new Color(255, 200, 220, 60);
            case SPELL3 -> new Color(255, 220, 100, 60);
            default -> new Color(0, 0, 0, 0);
        };
    }

    @Override
    public void draw(Graphics2D g) {
        int size = 30;
        g.setColor(new Color(180, 50, 150));
        drawDecorativeShape(g, (int)x, (int)y, size + 5);

        g.setColor(new Color(220, 80, 180));
        drawDecorativeShape(g, (int)x, (int)y, size);

        g.setColor(new Color(255, 150, 220));
        g.fill(new Ellipse2D.Double(x - 15, y - 15, 30, 30));

        g.setColor(new Color(255, 220, 240));
        g.fill(new Ellipse2D.Double(x - 8, y - 8, 16, 16));

        drawHpBar(g);
    }

    private void drawDecorativeShape(Graphics2D g, int cx, int cy, int size) {
        Path2D shape = new Path2D.Double();
        int points = 8;
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 / points * i - Math.PI / 8 + frame * 0.02;
            double r = (i % 2 == 0) ? size : size * 0.6;
            double px = cx + Math.cos(angle) * r;
            double py = cy + Math.sin(angle) * r;
            if (i == 0) shape.moveTo(px, py);
            else shape.lineTo(px, py);
        }
        shape.closePath();
        g.fill(shape);
    }

    private void drawHpBar(Graphics2D g) {
        int barW = 300, barH = 8;
        int barX = 42, barY = 14;
        g.setColor(new Color(80, 0, 0));
        g.fillRect(barX, barY, barW, barH);
        float ratio = maxHp > 0 ? (float) currentHp / maxHp : 0;
        Color barColor = switch (phase) {
            case SPELL1 -> new Color(255, 150, 200);
            case SPELL2 -> new Color(255, 200, 220);
            case SPELL3 -> new Color(255, 220, 100);
            default -> new Color(255, 80, 80);
        };
        g.setColor(barColor);
        g.fillRect(barX, barY, (int)(barW * ratio), barH);
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barW, barH);
    }
}
