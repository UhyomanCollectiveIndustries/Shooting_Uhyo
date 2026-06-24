package com.shootinguhyo.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * ParallaxBackground：複数の {@link BackgroundLayer} を奥→手前に重ねて
 * パララックス(視差)スクロールを実現するクラス。
 *
 * <p>奥のレイヤーは遅く小さく暗く、手前のレイヤーは速く大きく明るくすることで、
 *  平面的だった星流れに奥行きが生まれる。</p>
 *
 * <p>{@link #forStage(int, int, int)} でステージのテーマ色に合わせた
 *  3層構成を生成できる(夜空/朝焼け/夕暮れ/夜の森/雷雨/宇宙)。</p>
 */
public class ParallaxBackground {

    /** 奥(index 0)から手前へ並ぶレイヤー群。この順で描画する。 */
    private final List<BackgroundLayer> layers = new ArrayList<>();

    public ParallaxBackground() {}

    /** レイヤーを奥→手前の順で追加する。 */
    public ParallaxBackground add(BackgroundLayer layer) {
        layers.add(layer);
        return this;
    }

    public void update() {
        for (BackgroundLayer l : layers) l.update();
    }

    /** 奥のレイヤーから順に描画する(後から描いたものが手前)。 */
    public void draw(Graphics2D g) {
        for (BackgroundLayer l : layers) l.draw(g);
    }

    public void reset() {
        for (BackgroundLayer l : layers) l.reset();
    }

    /**
     * タイトル/メニュー用の汎用的な3層星空。
     */
    public static ParallaxBackground starfield(int w, int h) {
        return new ParallaxBackground()
                // 奥: 遅くて小さく暗い星(数多め)
                .add(new BackgroundLayer(w, h, 70, 0.2, 0.5, 1, 50, 110, null))
                // 中: 中速・中サイズ
                .add(new BackgroundLayer(w, h, 45, 0.6, 1.1, 1, 110, 190, null))
                // 手前: 速くて大きく明るい星(数少なめ)
                .add(new BackgroundLayer(w, h, 25, 1.2, 2.0, 2, 190, 255, null));
    }

    /**
     * ステージ番号に応じたテーマ色の3層背景を生成する。
     * 背景オーバーレイ色(Stage#backgroundTint)と調和する粒色を選ぶ。
     */
    public static ParallaxBackground forStage(int stageNo, int w, int h) {
        // [far, mid, near] のテーマ色
        Color far, mid, near;
        switch (stageNo) {
            case 1 -> { // 夜空
                far = new Color(120, 130, 200); mid = new Color(180, 190, 230); near = new Color(230, 235, 255);
            }
            case 2 -> { // 朝焼け
                far = new Color(200, 150, 130); mid = new Color(240, 190, 150); near = new Color(255, 230, 190);
            }
            case 3 -> { // 夕暮れ
                far = new Color(180, 110, 130); mid = new Color(230, 150, 120); near = new Color(255, 200, 140);
            }
            case 4 -> { // 夜の森
                far = new Color(90, 140, 110); mid = new Color(140, 190, 140); near = new Color(200, 240, 190);
            }
            case 5 -> { // 雷雨
                far = new Color(110, 120, 150); mid = new Color(170, 180, 210); near = new Color(220, 230, 255);
            }
            case 6 -> { // 宇宙＋オーロラ
                far = new Color(130, 100, 190); mid = new Color(120, 200, 210); near = new Color(220, 200, 255);
            }
            default -> {
                return starfield(w, h);
            }
        }
        return new ParallaxBackground()
                .add(new BackgroundLayer(w, h, 70, 0.2, 0.5, 1, 40, 100, far))
                .add(new BackgroundLayer(w, h, 45, 0.6, 1.1, 1, 90, 170, mid))
                .add(new BackgroundLayer(w, h, 22, 1.2, 2.0, 2, 160, 240, near));
    }
}
