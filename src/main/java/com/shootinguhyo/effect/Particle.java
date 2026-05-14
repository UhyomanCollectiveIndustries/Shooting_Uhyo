package com.shootinguhyo.effect;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

/**
 * Particle：爆発などの「破片1個」を表すクラス。
 *
 * 【役割】
 *  敵やボスが爆発したときの細かい火花。多数集まると派手な爆発エフェクトになる。
 *
 * 【仕組み】
 *  - ランダムな方向・速度で飛び散る
 *  - 摩擦で減速(vx,vyに0.95を掛けることで毎フレーム5%ずつ減速)
 *  - 寿命(life)を持ち、時間とともに透明化して消える
 *
 * 【なぜEntityを継承しないのか】
 *  「位置を持つ」「描画する」までは共通だが、戦闘ロジックに関わらないため
 *  シンプルなクラスとして独立させている。
 */
public class Particle {
    public double x, y;
    public double vx, vy;       // 速度ベクトル
    public int life, maxLife;   // 残り寿命と初期寿命
    public Color color;
    public double radius;
    public boolean active = true;

    // staticなランダム生成器 → 全パーティクルで1つを共有してメモリ節約
    private static final Random rand = new Random();

    /**
     * 中心(x,y)から色colorの破片を1個生成。
     * 飛ぶ方向・速度・寿命・サイズはランダムにすることで自然な見た目になる。
     */
    public Particle(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        double angle = rand.nextDouble() * Math.PI * 2; // 0〜360度どこへでも
        double speed = rand.nextDouble() * 4 + 1;       // 1〜5の速度
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
        this.life = rand.nextInt(20) + 15;              // 15〜34フレームの寿命
        this.maxLife = this.life;
        this.radius = rand.nextDouble() * 3 + 2;        // 2〜5pxの大きさ
    }

    /**
     * 1フレーム更新：移動 → 減速 → 寿命減算。
     * 寿命が0になったらactiveを偽にしてGamePanel側で除去される。
     */
    public void update() {
        x += vx;
        y += vy;
        vx *= 0.95; // 摩擦による減速。1未満を掛けるだけで自然に止まっていく
        vy *= 0.95;
        life--;
        if (life <= 0) active = false;
    }

    /**
     * 寿命に応じて透明化＆縮小しながら描画。
     * alpha = life / maxLife なので、生成直後は1(濃い)→消滅直前は0(透明)に。
     * 半径も alpha 倍するので、フェードしながら縮む。
     */
    public void draw(Graphics2D g) {
        float alpha = (float) life / maxLife;
        Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 200));
        g.setColor(c);
        double r = radius * alpha;
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
    }
}
