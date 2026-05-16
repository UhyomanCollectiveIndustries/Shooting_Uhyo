package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.RadialPattern;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Enemy：通常敵キャラクター。
 *
 * 【役割】
 *  - 画面上から下へ波打つようにゆっくり進入してくる雑魚キャラ
 *  - 一定周期で全方位弾(RadialPattern)を撃つ
 *  - HPが0になると倒され、アイテムとスコアをドロップ
 *
 * 【動き方の仕組み】
 *  - y を毎フレーム+0.8で下に進める(縦方向はゆっくり等速)
 *  - x = startX + sin(frame * 0.03) * 40 で左右に揺れる
 *    → サイン波を使うことで滑らかな往復運動を表現できる(ゲームでは頻出パターン)
 *
 * 【protectedの意味】
 *  サブクラスからアクセスできる。今は継承していないが拡張しやすいよう公開度をゆるめている。
 */
public class Enemy extends Entity {
    protected int hp;       // 残り体力
    protected int maxHp;    // 最大体力(HPバー表示などに使える)
    protected int score;    // 倒した時のスコア
    protected int frame = 0;     // 出現後の経過フレーム
    protected double startX;     // 横揺れの中心x座標
    protected List<EnemyBullet> newBullets = new ArrayList<>();

    // 弾幕パラメータ(後から書き換え可能。Stage1など軽め敵向け)
    protected int shootInterval = 90;   // 弾を撃つ間隔(フレーム)
    protected int shootDirections = 8;  // 全方位弾の本数

    public Enemy(double x, double y, int hp, int score) {
        super(x, y);
        // 難易度に応じてHPをスケール
        int scaledHp = Math.max(1, (int)(hp * com.shootinguhyo.config.Difficulty.current().enemyHpMul));
        this.hp = scaledHp;
        this.maxHp = scaledHp;
        this.score = score;
        this.startX = x;
    }

    /** 弾幕を軽めに設定する。Stage1など序盤ステージ向け。 */
    public Enemy withLightShot() {
        this.shootInterval = 150;
        this.shootDirections = 5;
        return this;
    }

    /** 弾幕設定をカスタマイズ。 */
    public Enemy withShot(int interval, int directions) {
        this.shootInterval = Math.max(20, interval);
        this.shootDirections = Math.max(1, directions);
        return this;
    }

    /**
     * 敵の1フレーム更新。
     * 移動と弾発射、画面外チェックを行う。
     */
    @Override
    public void update() {
        frame++;
        y += 0.8;                                       // 下方向にゆっくり進む
        x = startX + Math.sin(frame * 0.03) * 40;       // 左右にサイン波で揺れる

        // 指定した間隔で全方位弾を撃つ(間隔の半分の位置で撃つことでズレを作る)
        if (frame % shootInterval == shootInterval / 2 && hp > 0) {
            RadialPattern pattern = new RadialPattern(shootDirections, 2.0, frame * 0.1);
            newBullets.addAll(pattern.generate(x, y, EnemyBullet.BulletSize.SMALL, new Color(200, 100, 255)));
        }

        // 画面下を抜けたら消滅
        if (y > 500) active = false;
    }

    /** ダメージを受ける処理。HPが0以下になったら消滅。 */
    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) {
            hp = 0;
            active = false;
        }
    }

    /** 撃った弾を取り出し、内部リストをクリアする(Playerと同じ設計)。 */
    public List<EnemyBullet> getAndClearNewBullets() {
        List<EnemyBullet> bullets = new ArrayList<>(newBullets);
        newBullets.clear();
        return bullets;
    }

    public boolean isDefeated() { return hp <= 0; }
    public int getScore() { return score; }
    public int getHp() { return hp; }

    /**
     * 敵の見た目を菱形で描画。
     * 内側にも小さい菱形を描いて立体感を出している。
     */
    @Override
    public void draw(Graphics2D g) {
        int size = 10;
        Path2D diamond = new Path2D.Double();
        diamond.moveTo(x, y - size);      // 上
        diamond.lineTo(x + size, y);      // 右
        diamond.lineTo(x, y + size);      // 下
        diamond.lineTo(x - size, y);      // 左
        diamond.closePath();

        g.setColor(new Color(150, 50, 220));
        g.fill(diamond);
        g.setColor(new Color(200, 100, 255));
        g.draw(diamond);

        // 内側の菱形(明るい色)
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
