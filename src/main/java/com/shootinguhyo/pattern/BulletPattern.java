package com.shootinguhyo.pattern;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import java.awt.Color;
import java.util.List;

/**
 * BulletPattern：弾幕の「型(パターン)」を表すインターフェース。
 *
 * 【役割】
 *  「ある地点(x,y)から、ある種類の弾を、ある形に並べて生成する」処理を抽象化したもの。
 *  これを実装することで、全方位弾(Radial)・自機狙い(Aimed)・スパイラル(Spiral)など
 *  さまざまな弾幕を同じ呼び出し方で使えるようになる。
 *
 * 【インターフェースを使うメリット】
 *  - 呼び出し側(BossやEnemy)はパターンの中身を知らなくてもよい
 *  - 新しい弾幕パターンを増やしても、既存のコードを変更しなくて済む(拡張に強い)
 *
 * 【設計パターンの名前】
 *  これは「Strategy(ストラテジー)パターン」と呼ばれるもの。
 *  アルゴリズム(ここでは弾の並べ方)を入れ替え可能にする設計。
 */
public interface BulletPattern {
    /**
     * 弾を生成して返す。
     * @param x,y 弾の発射元(敵やボスの位置)
     * @param size 弾のサイズ
     * @param color 弾の色
     */
    List<EnemyBullet> generate(double x, double y, EnemyBullet.BulletSize size, Color color);
}
