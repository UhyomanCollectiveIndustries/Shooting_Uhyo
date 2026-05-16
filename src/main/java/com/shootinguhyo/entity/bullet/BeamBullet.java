package com.shootinguhyo.entity.bullet;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;

/**
 * BeamBullet：縦長のレーザー風自機弾。
 *
 * <p>uhyowomanのオプションが連射することで「ビーム」のような見た目を作る。
 * 当たり判定はGamePanel側の点距離判定がそのまま使えるよう、PlayerBulletを継承している。</p>
 *
 * <p>描画は中央に白い芯、外側に薄い水色のグロー。beamWidthでビーム太さを変える。</p>
 */
public class BeamBullet extends PlayerBullet {
    private final int beamWidth;
    private final int beamHeight;

    public BeamBullet(double x, double y, double vx, double vy, int damage, int beamWidth) {
        super(x, y, vx, vy, damage);
        this.beamWidth = Math.max(2, beamWidth);
        // 太さに合わせて長さも少し変化(太いほど存在感を出す)
        this.beamHeight = 20 + beamWidth * 2;
    }

    @Override
    public void draw(Graphics2D g) {
        int w = beamWidth;
        int h = beamHeight;
        int bx = (int) x - w / 2;
        int by = (int) y - h / 2;

        Composite old = g.getComposite();
        // 外側の薄いグロー
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g.setColor(new Color(140, 220, 255));
        g.fillRoundRect(bx - 2, by, w + 4, h, w, w);
        // メインのビーム
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
        g.setColor(new Color(180, 240, 255));
        g.fillRoundRect(bx, by, w, h, w, w);
        // 白い芯
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.setColor(Color.WHITE);
        int coreW = Math.max(1, w / 3);
        g.fillRoundRect((int) x - coreW / 2, by + 2, coreW, h - 4, coreW, coreW);
        g.setComposite(old);
    }
}
