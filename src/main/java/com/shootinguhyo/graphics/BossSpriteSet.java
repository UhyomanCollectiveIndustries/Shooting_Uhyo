package com.shootinguhyo.graphics;

/**
 * BossSpriteSet：ボスのアニメ用スプライト群をまとめる(ひな型)。
 *
 * 【役割】
 *  - ボスは「待機」「攻撃」「被弾」「撃破」などの状態ごとに絵が変わる想定
 *  - 各状態のスプライト配列を1つにまとめておき、Boss.javaから差し替え可能にする
 *
 * 【使い方の想定】
 *  BossSpriteSet set = new BossSpriteSet(
 *      new SpriteAnimation(Sprites.BOSS_ANIM, 20),     // idle
 *      new SpriteAnimation(Sprites.BOSS_ANIM, 10),     // attack
 *      new SpriteAnimation(Sprites.BOSS_ANIM, 4),      // hit
 *      new SpriteAnimation(Sprites.BOSS_ANIM, 6)       // defeated
 *  );
 *  // Boss側で状態に応じて current() で取り出して描画
 *
 * 【TODO】
 *  - 専用のドット絵フレーム(2〜4枚)を Sprites.java に追加
 *  - Boss.java に BossSpriteSet を持たせて状態切替で current アニメを差し替える
 */
public class BossSpriteSet {
    /** 状態の種類。 */
    public enum State { IDLE, ATTACK, HIT, DEFEATED }

    private final SpriteAnimation idle;
    private final SpriteAnimation attack;
    private final SpriteAnimation hit;
    private final SpriteAnimation defeated;

    private State current = State.IDLE;

    public BossSpriteSet(SpriteAnimation idle, SpriteAnimation attack,
                         SpriteAnimation hit, SpriteAnimation defeated) {
        this.idle = idle;
        this.attack = attack;
        this.hit = hit;
        this.defeated = defeated;
    }

    public void setState(State s) {
        if (this.current != s) {
            this.current = s;
            getCurrentAnim().reset();
        }
    }

    public State getState() { return current; }

    public SpriteAnimation getCurrentAnim() {
        return switch (current) {
            case IDLE -> idle;
            case ATTACK -> attack;
            case HIT -> hit;
            case DEFEATED -> defeated;
        };
    }

    /** 毎フレーム呼ぶ。 */
    public void tick() { getCurrentAnim().tick(); }
}
