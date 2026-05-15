package com.shootinguhyo.config;

/**
 * Difficulty：ゲーム全体の難易度。
 *
 * 【役割】
 *  弾の速さや発射密度、敵HPなどを難易度ごとに変えるための識別子。
 *  各enum値が「倍率」を持っているので、敵や弾幕パターンがこの値を見て調整する想定。
 *
 * 【倍率の使い方】
 *  例: enemyBullet.vx *= difficulty.bulletSpeedMul;
 *      enemy.hp *= difficulty.enemyHpMul;
 *
 * 【TODO】
 *  - LUNATIC(極悪)難易度の追加
 *  - エクストラ(EXTRA)ステージ用の難易度
 */
public enum Difficulty {
    EASY  ("Easy",   0.7, 0.6, 0.7),
    NORMAL("Normal", 1.0, 1.0, 1.0),
    HARD  ("Hard",   1.3, 1.3, 1.2),
    LUNATIC("Lunatic", 1.6, 1.6, 1.4);

    public final String displayName;
    public final double bulletSpeedMul; // 敵弾の速さ倍率
    public final double bulletDensityMul; // 弾密度倍率(発射本数や頻度)
    public final double enemyHpMul;     // 敵HP倍率

    Difficulty(String displayName, double bulletSpeedMul, double bulletDensityMul, double enemyHpMul) {
        this.displayName = displayName;
        this.bulletSpeedMul = bulletSpeedMul;
        this.bulletDensityMul = bulletDensityMul;
        this.enemyHpMul = enemyHpMul;
    }

    /**
     * 現在の難易度。EnemyBullet/Enemy/Boss等が参照して挙動を調整する。
     * GameConfig.setDifficulty() から同期される。
     */
    private static Difficulty current = NORMAL;

    public static Difficulty current() { return current; }
    public static void setCurrent(Difficulty d) { current = (d == null ? NORMAL : d); }
}
