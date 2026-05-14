package com.shootinguhyo.entity;

import java.awt.Graphics2D;

/**
 * Entity：ゲーム内の「画面上に存在するもの全て」の共通土台。
 *
 * 【役割】
 *  自機・敵・弾・アイテムなど、画面に存在するものは「位置(x,y)」と「動く」「描く」が共通。
 *  この共通部分をまとめておくことで、コードを再利用できるし、似た処理がバラバラにならない。
 *
 * 【なぜabstract(抽象)クラスにするのか】
 *  Entityは「中身を定義していないメソッド(update/draw)」を持つので、そのままではnewできない。
 *  各サブクラス(Player, Enemy, …)が必ずupdate/drawを定義する義務がある、という設計の意思表示。
 *
 * 【activeフラグの意味】
 *  「もう消滅していい」フラグ。画面外に出たり倒されたら false にする。
 *  GamePanel側ではこのフラグを見て、リストから取り除く。
 */
public abstract class Entity {
    public double x, y;          // 中心座標。doubleにしているのは滑らかな移動のため
    public boolean active = true;

    public Entity(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** 1フレーム分の動きを進める。サブクラスで実装必須。 */
    public abstract void update();

    /** 自分の絵を描く。サブクラスで実装必須。 */
    public abstract void draw(Graphics2D g);
}
