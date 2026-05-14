package com.shootinguhyo.graphics;

import java.awt.Graphics2D;

/**
 * SpriteAnimation：複数のPixelSpriteを切り替えてアニメーションさせるクラス。
 *
 * 【役割】
 *  「歩く」「攻撃する」「アイドル」など、複数フレームの絵を順番に表示する。
 *  キャラクターやボスのアニメーション化に使う想定。
 *
 * 【使い方】
 *  PixelSprite[] frames = { spriteA, spriteB, spriteC };
 *  SpriteAnimation anim = new SpriteAnimation(frames, 10); // 10フレームごとに切替
 *  // 毎ループ: anim.tick(); anim.draw(g, x, y, scale);
 *
 * 【TODO】
 *  - ループ/非ループの切替
 *  - フレームごとの表示時間を個別指定できるようにする
 *  - アニメ完了時のコールバック
 */
public class SpriteAnimation {
    private final PixelSprite[] frames;
    private final int framesPerStep;
    private int tick = 0;

    public SpriteAnimation(PixelSprite[] frames, int framesPerStep) {
        this.frames = frames;
        this.framesPerStep = Math.max(1, framesPerStep);
    }

    /** 1フレーム進める。ゲームループから毎フレーム呼ぶ想定。 */
    public void tick() {
        tick++;
    }

    /** リセット。再生し直したい時に。 */
    public void reset() {
        tick = 0;
    }

    /** 現在表示すべきスプライトを返す。 */
    public PixelSprite current() {
        if (frames.length == 0) return null;
        int idx = (tick / framesPerStep) % frames.length;
        return frames[idx];
    }

    public void draw(Graphics2D g, double cx, double cy, int scale) {
        PixelSprite s = current();
        if (s != null) s.draw(g, cx, cy, scale);
    }
}
