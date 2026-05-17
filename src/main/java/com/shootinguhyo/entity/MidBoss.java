package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.BulletPattern;
import com.shootinguhyo.pattern.RadialPattern;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * MidBoss：ステージ中盤に登場する中ボス。
 *
 * <p>Bossより軽量で、単一フェーズ・単一パターン。
 *  プレイヤーは雑魚に邪魔されずに中ボスと1対1で戦う想定。</p>
 *
 * <p>パターンと弾の色は外部から差し替えられる(ステージ別の個性)。</p>
 */
public class MidBoss extends Entity {
    private int hp;
    private final int maxHp;
    private int frame = 0;
    private double moveDir = 1;
    private final BulletPattern pattern;
    private final Color bulletColor;
    private final EnemyBullet.BulletSize bulletSize;
    private final int shootInterval;
    private final List<EnemyBullet> newBullets = new ArrayList<>();

    public MidBoss(double x, double y, int hp, BulletPattern pattern,
                   Color bulletColor, EnemyBullet.BulletSize bulletSize,
                   int shootInterval) {
        super(x, y);
        // 難易度に応じてHPをスケール
        int scaledHp = Math.max(1, (int) (hp * com.shootinguhyo.config.Difficulty.current().enemyHpMul));
        this.hp = scaledHp;
        this.maxHp = scaledHp;
        this.pattern = pattern != null ? pattern : new RadialPattern(10, 2.0, 0);
        this.bulletColor = bulletColor != null ? bulletColor : new Color(255, 160, 220);
        this.bulletSize = bulletSize != null ? bulletSize : EnemyBullet.BulletSize.SMALL;
        this.shootInterval = Math.max(8, shootInterval);
    }

    @Override
    public void update() {
        frame++;
        // 左右にゆっくり移動(画面端で反転)
        x += moveDir * 1.0;
        if (x < 80)  moveDir = 1;
        if (x > 304) moveDir = -1;
        // 上下にもサインで揺らす
        y = 90 + Math.sin(frame * 0.02) * 10;

        if (frame % shootInterval == shootInterval / 2 && hp > 0) {
            newBullets.addAll(pattern.generate(x, y, bulletSize, bulletColor));
        }
    }

    public void takeDamage(int dmg) {
        if (hp <= 0) return;
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            active = false;
        }
    }

    public boolean isDefeated() { return hp <= 0; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }

    public List<EnemyBullet> getAndClearNewBullets() {
        List<EnemyBullet> b = new ArrayList<>(newBullets);
        newBullets.clear();
        return b;
    }

    @Override
    public void draw(Graphics2D g) {
        // 本体: 紫の丸+回転する菱形オーラ
        Composite oldComp = g.getComposite();
        // 外側のオーラ
        for (int r = 28; r > 18; r -= 4) {
            float a = (28 - r) / 14f + 0.2f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            g.setColor(new Color(180, 80, 200));
            g.fillOval((int) x - r, (int) y - r, r * 2, r * 2);
        }
        g.setComposite(oldComp);
        // 本体
        g.setColor(new Color(220, 100, 220));
        g.fillOval((int) x - 16, (int) y - 16, 32, 32);
        g.setColor(new Color(255, 200, 240));
        g.drawOval((int) x - 16, (int) y - 16, 32, 32);
        // 回転する内側マーク
        double a = frame * 0.05;
        Path2D mark = new Path2D.Double();
        for (int i = 0; i < 4; i++) {
            double ang = a + Math.PI / 2 * i;
            double px = x + Math.cos(ang) * 8;
            double py = y + Math.sin(ang) * 8;
            if (i == 0) mark.moveTo(px, py);
            else mark.lineTo(px, py);
        }
        mark.closePath();
        g.setColor(new Color(255, 240, 255));
        g.fill(mark);

        // HPバー(画面上部に細長く)
        drawHpBar(g);
    }

    private void drawHpBar(Graphics2D g) {
        int barX = 20;
        int barY = 8;
        int barW = 344;
        int barH = 7;
        // 背景
        g.setColor(new Color(40, 20, 50));
        g.fillRect(barX, barY, barW, barH);
        // HP
        float ratio = maxHp > 0 ? (float) hp / maxHp : 0;
        int fill = Math.max(0, (int) (barW * ratio));
        g.setColor(new Color(220, 120, 220));
        g.fillRect(barX, barY, fill, barH);
        // 枠
        g.setColor(new Color(255, 220, 240));
        g.drawRect(barX, barY, barW, barH);
        // 「MID BOSS」ラベル
        g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 9));
        g.setColor(new Color(255, 220, 240));
        g.drawString("MID BOSS", barX + 2, barY + barH + 9);
    }
}
