package com.shootinguhyo.graphics;

import java.awt.Graphics2D;

/**
 * SpriteAnimation：複数のPixelSpriteを切り替えてアニメーションさせるクラス。
 *
 * 【役割】
 *  「歩く」「攻撃する」「アイドル」など、複数フレームの絵を順番に表示する。
 *  キャラクターやボスのアニメーション化に使う。
 *
 * 【使い方】
 *  PixelSprite[] frames = { spriteA, spriteB, spriteC };
 *  SpriteAnimation anim = new SpriteAnimation(frames, 10); // 10フレームごとに切替・ループ
 *  // 毎ループ: anim.tick(); anim.draw(g, x, y, scale);
 *
 *  // 非ループ + 完了コールバック:
 *  SpriteAnimation once = new SpriteAnimation(frames, 6, false);
 *  once.setOnComplete(() -> System.out.println("演出おわり"));
 *
 *  // フレームごとに表示時間を変える(溜め→素早く):
 *  SpriteAnimation v = new SpriteAnimation(frames, new int[]{30, 6, 6}, false);
 */
public class SpriteAnimation {
    private final PixelSprite[] frames;
    /** 各フレームの表示時間(フレーム数)。長さは frames と同じ。 */
    private final int[] durations;
    /** durations の累積(二分探索的に現在フレームを求めるため)。 */
    private final int[] cumulative;
    private final int totalDuration;

    private final boolean loop;
    private int tick = 0;
    private boolean finished = false;
    private Runnable onComplete;

    /** 全フレーム共通の表示時間でループ再生(従来互換)。 */
    public SpriteAnimation(PixelSprite[] frames, int framesPerStep) {
        this(frames, uniform(frames.length, framesPerStep), true);
    }

    /** 全フレーム共通の表示時間。ループ可否を指定。 */
    public SpriteAnimation(PixelSprite[] frames, int framesPerStep, boolean loop) {
        this(frames, uniform(frames.length, framesPerStep), loop);
    }

    /**
     * フレームごとに表示時間を個別指定する。
     * @param frames   表示するスプライト列
     * @param durations 各フレームの表示時間(フレーム数)。framesと同数であること。
     * @param loop      末尾まで再生したあと先頭へ戻すか
     */
    public SpriteAnimation(PixelSprite[] frames, int[] durations, boolean loop) {
        this.frames = frames != null ? frames : new PixelSprite[0];
        this.loop = loop;

        int n = this.frames.length;
        this.durations = new int[n];
        this.cumulative = new int[n];
        int sum = 0;
        for (int i = 0; i < n; i++) {
            int d = (durations != null && i < durations.length) ? durations[i] : 1;
            this.durations[i] = Math.max(1, d);
            sum += this.durations[i];
            this.cumulative[i] = sum;
        }
        this.totalDuration = Math.max(1, sum);
    }

    private static int[] uniform(int n, int framesPerStep) {
        int[] d = new int[Math.max(0, n)];
        java.util.Arrays.fill(d, Math.max(1, framesPerStep));
        return d;
    }

    /** 完了時(非ループで最終フレームを再生し終えた瞬間)に呼ばれる処理を設定。 */
    public void setOnComplete(Runnable onComplete) {
        this.onComplete = onComplete;
    }

    /** 1フレーム進める。ゲームループから毎フレーム呼ぶ想定。 */
    public void tick() {
        if (finished || frames.length == 0) return;
        tick++;
        if (!loop && tick >= totalDuration) {
            tick = totalDuration; // 最終フレームで停止
            finished = true;
            if (onComplete != null) onComplete.run();
        }
    }

    /** リセット。再生し直したい時に。 */
    public void reset() {
        tick = 0;
        finished = false;
    }

    /** 非ループ再生が最後まで終わったか。ループ再生では常にfalse。 */
    public boolean isFinished() {
        return finished;
    }

    /** 現在表示すべきスプライトを返す。 */
    public PixelSprite current() {
        if (frames.length == 0) return null;
        int t = loop ? (tick % totalDuration) : Math.min(tick, totalDuration - 1);
        for (int i = 0; i < cumulative.length; i++) {
            if (t < cumulative[i]) return frames[i];
        }
        return frames[frames.length - 1];
    }

    public void draw(Graphics2D g, double cx, double cy, int scale) {
        PixelSprite s = current();
        if (s != null) s.draw(g, cx, cy, scale);
    }
}
