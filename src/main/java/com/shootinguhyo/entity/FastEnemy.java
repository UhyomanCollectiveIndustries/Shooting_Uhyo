package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.AimedPattern;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * FastEnemy：高速移動型の敵キャラ。
 *
 * 【役割】
 *  - 画面左右から斜めに高速で横切る敵
 *  - 1度だけ自機狙いの弾(AimedPattern)を撃つ
 *  - 通常敵(Enemy)とは違って画面に長居しない「奇襲役」
 *
 * 【設計のポイント】
 *  - vx,vy : 一定方向の速度ベクトル。常に同じ向きへ直進する
 *  - firedShotで「1回だけ撃ったか」を管理。連射されると弾が多すぎて避けられないため
 *  - playerX,playerY : 自機狙いに必要なので外から教えてもらう(setPlayerPosition)
 */
public class FastEnemy extends Entity {
    private double vx, vy;             // 速度ベクトル
    private int frame = 0;
    private boolean firedShot = false; // 既に弾を撃ったか
    private double playerX, playerY;
    private List<EnemyBullet> newBullets = new ArrayList<>();
    private int score = 150;
    private int hp = 30;

    public FastEnemy(double x, double y, double vx, double vy) {
        super(x, y);
        this.vx = vx;
        this.vy = vy;
    }

    /** プレイヤー位置を外部から教えてもらう。GamePanelが毎フレーム呼ぶ。 */
    public void setPlayerPosition(double px, double py) {
        this.playerX = px;
        this.playerY = py;
    }

    /**
     * 高速敵の1フレーム更新。
     * - 直進移動
     * - 画面内に入ったら一度だけ弾を発射
     * - 画面外に出たら消滅
     */
    @Override
    public void update() {
        frame++;
        x += vx;
        y += vy;

        // 画面内に入ったタイミングで1度だけ発射 (画面外で撃つと弾が見えない)
        if (!firedShot && y > 0 && y < 448 && x > 0 && x < 384) {
            firedShot = true;
            // AimedPattern: 自機方向を中心に3way(扇状3発)で撃つ
            AimedPattern pattern = new AimedPattern(3, Math.toRadians(15), 3.5);
            newBullets.addAll(pattern.generate(x, y, playerX, playerY,
                    EnemyBullet.BulletSize.SMALL, new Color(0, 220, 220)));
        }

        // 画面外(余裕を持ってチェック)に出たら消滅
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

    /**
     * 高速敵の描画。小さい菱形と進行方向への軌跡線を描く。
     *
     * 【軌跡線の表現】
     *  drawLineで「進んできた方向(過去の位置)」から「現在位置」に半透明の線を引く。
     *  3フレーム前の位置を簡易計算 (-vx*3, -vy*3) して、速度感を演出している。
     */
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

        // 残像のような尾を引く軌跡線
        g.setColor(new Color(0, 255, 255, 100));
        g.drawLine((int)(x - vx * 3), (int)(y - vy * 3), (int)x, (int)y);
    }
}
