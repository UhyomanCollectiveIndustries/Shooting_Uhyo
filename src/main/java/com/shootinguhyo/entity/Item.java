package com.shootinguhyo.entity;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * Item：敵を倒したときに落ちるアイテム。
 *
 * 【役割】
 *  - POWER : 取ると自機のパワーが上がる(青いP)
 *  - POINT : 取ると得点ボーナス(黄色い星)
 *
 * 【動き】
 *  - 一定速度で下に落ちる
 *  - 画面外に出たら自動消滅
 *
 * 【設計のポイント】
 *  ItemTypeをenumで管理 → 種類分岐がswitch一発で書けて読みやすい。
 *  典型的な「タイプ駆動の描画分岐」パターン。
 */
public class Item extends Entity {
    public enum ItemType { POWER, POINT }

    private ItemType type;
    private boolean big = false;  // 大Pかどうか(POWERのみ意味を持つ)
    private double vy = 1.5; // 落下速度(縦方向)
    private int frame = 0;

    public Item(double x, double y, ItemType type) {
        super(x, y);
        this.type = type;
    }

    /** 大きい(価値の高い)アイテムを作るコンストラクタ。 */
    public Item(double x, double y, ItemType type, boolean big) {
        super(x, y);
        this.type = type;
        this.big = big;
    }

    public boolean isBig() { return big; }

    /** 1フレーム更新：下方向に移動し、画面外で消滅。 */
    @Override
    public void update() {
        frame++;
        y += vy;
        if (y > 480) active = false;
    }

    public ItemType getType() { return type; }
    public double getRadius() { return 8; } // 取得判定の半径

    /**
     * 種類によって描画を切り替え。
     * POWER : 青円に「P」の文字
     * POINT : 黄色い星型
     */
    @Override
    public void draw(Graphics2D g) {
        if (type == ItemType.POWER) {
            if (big) {
                // 大P: 一回り大きく、輪郭付きで黄色寄り
                g.setColor(new Color(255, 220, 60));
                g.fill(new Ellipse2D.Double(x - 11, y - 11, 22, 22));
                g.setColor(new Color(255, 255, 220));
                g.draw(new Ellipse2D.Double(x - 11, y - 11, 22, 22));
                g.setColor(new Color(40, 40, 120));
                g.setFont(new Font("Arial", Font.BOLD, 12));
                g.drawString("P", (int)x - 4, (int)y + 4);
            } else {
                g.setColor(new Color(50, 100, 255));
                g.fill(new Ellipse2D.Double(x - 8, y - 8, 16, 16));
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 9));
                g.drawString("P", (int)x - 3, (int)y + 3);
            }
        } else {
            g.setColor(new Color(255, 220, 0));
            drawStar(g, (int)x, (int)y, 8, 4, 5);
        }
    }

    /**
     * 星型を描くヘルパー。
     *
     * 【星型のアルゴリズム】
     *  点数(points)の2倍の頂点を持つ多角形を作り、
     *  偶数番目を外側の半径(outerR)、奇数番目を内側の半径(innerR)にすると
     *  星型になる。
     *
     * 例：5つの点を持つ星 → 10頂点を交互に外/内で配置。
     */
    private void drawStar(Graphics2D g, int cx, int cy, int outerR, int innerR, int points) {
        Path2D star = new Path2D.Double();
        for (int i = 0; i < points * 2; i++) {
            double angle = Math.PI / points * i - Math.PI / 2; // -π/2で「真上」を基準
            double r = (i % 2 == 0) ? outerR : innerR;
            double px = cx + Math.cos(angle) * r;
            double py = cy + Math.sin(angle) * r;
            if (i == 0) star.moveTo(px, py);
            else star.lineTo(px, py);
        }
        star.closePath();
        g.fill(star);
    }
}
