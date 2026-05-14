package com.shootinguhyo.graphics;

import java.awt.Color;
import java.util.Map;

/**
 * Sprites：ゲーム内で使うドット絵スプライトをまとめた「絵札集」。
 *
 * 【役割】
 *  - 自機、敵、ボス、アイテム、背景パーツなどの「絵」を1か所で定義する
 *  - PNG等の画像ファイルを使わず、文字配列で絵を表現することで
 *    「コード=絵」になり、初学者でも編集しやすい
 *
 * 【ひな型なので】
 *  - サンプルのドット絵はかなり荒い。ちゃんとしたデザインは差し替え前提
 *  - 「ここを書き換えればキャラの絵が変わる」場所を示すのが目的
 *
 * 【使い方】
 *   Sprites.REIMU_IDLE.draw(g, x, y, 2); // 2倍拡大で描画
 */
public final class Sprites {

    private Sprites() {} // ユーティリティクラスなのでインスタンス化禁止

    // ====== パレット定義(色) ======
    // キャラごと、もしくは「赤系」「青系」などのテーマで色を使い分ける

    /** 自機(霊夢風)用の色パレット */
    private static final Map<Character, Color> REIMU_PALETTE = PixelSprite.palette(
            'R', new Color(220, 60, 80),    // 赤い服
            'W', new Color(255, 240, 240),  // 白い袖
            'S', new Color(255, 220, 180),  // 肌色
            'H', new Color(60, 30, 20),     // 髪
            'Y', new Color(255, 220, 100),  // 髪飾り(黄)
            'B', new Color(40, 20, 30)      // 輪郭
    );

    /** 自機(魔理沙風)用の色パレット */
    private static final Map<Character, Color> MARISA_PALETTE = PixelSprite.palette(
            'K', new Color(40, 30, 40),     // 黒い帽子・服
            'W', new Color(255, 250, 240),  // 白いエプロン
            'S', new Color(255, 220, 180),  // 肌色
            'Y', new Color(240, 220, 80),   // 金髪
            'P', new Color(120, 80, 160),   // 紫リボン
            'B', new Color(20, 10, 20)      // 輪郭
    );

    /** 敵(雑魚)用パレット */
    private static final Map<Character, Color> ENEMY_PALETTE = PixelSprite.palette(
            'P', new Color(180, 80, 220),
            'L', new Color(220, 140, 255),
            'D', new Color(80, 30, 120),
            'W', new Color(255, 255, 255)
    );

    /** 高速敵用パレット */
    private static final Map<Character, Color> FAST_ENEMY_PALETTE = PixelSprite.palette(
            'C', new Color(0, 200, 220),
            'L', new Color(150, 255, 255),
            'D', new Color(0, 80, 100)
    );

    /** ボス用パレット */
    private static final Map<Character, Color> BOSS_PALETTE = PixelSprite.palette(
            'R', new Color(255, 100, 180),
            'P', new Color(180, 50, 150),
            'W', new Color(255, 240, 250),
            'D', new Color(80, 30, 60),
            'Y', new Color(255, 220, 100),
            'S', new Color(255, 220, 180),
            'H', new Color(60, 30, 50)
    );

    // ====== 自機キャラのスプライト ======

    /**
     * 霊夢風キャラ(待機)。
     * 16x16のドット絵。
     * Bは輪郭、Hは髪、Yは髪飾り、Sは肌、Rは赤い服、Wは白い袖。
     */
    public static final PixelSprite REIMU_IDLE = new PixelSprite(new String[]{
            "....BBBB........",
            "...BYYYYB.......",
            "..BHHHHHHB......",
            "..BHSSSSHB......",
            "..BHS..SHB......",
            "...BSSSSB.......",
            "...BWRRWB.......",
            "..BWRRRRWB......",
            ".BWRRRRRRWB.....",
            "BWRRRRRRRRWB....",
            "BRRRRRRRRRRB....",
            ".BRRRRRRRRB.....",
            "..BRRRRRRB......",
            "...BRRRRB.......",
            "....BBBB........",
            "................"
    }, REIMU_PALETTE);

    /**
     * 魔理沙風キャラ(待機)。
     * 黒い帽子と金髪が目印。
     */
    public static final PixelSprite MARISA_IDLE = new PixelSprite(new String[]{
            "....KKKKKK......",
            "...KKKKKKKK.....",
            "..KKKKKKKKKK....",
            "...PPPPPPP......",
            "...KYYYYYYK.....",
            "..KYSSSSSSYK....",
            "..KYS..SSYK.....",
            "...KYSSSSYK.....",
            "...KKKKKKK......",
            "..KWKKKKWKK.....",
            ".KWWKKKKWWKK....",
            "KWWWKKKKWWWKK...",
            ".KWWKKKKWWKK....",
            "..KKKKKKKKK.....",
            "...KKKKKKK......",
            "................"
    }, MARISA_PALETTE);

    // ====== 敵キャラのスプライト ======

    /** 通常敵(妖精風)。ひし形ベース。 */
    public static final PixelSprite ENEMY_BASIC = new PixelSprite(new String[]{
            "....PP....",
            "...PLLP...",
            "..PLWWLP..",
            ".PLWWWWLP.",
            "PLWWWWWWLP",
            "PLWWWWWWLP",
            ".PLWWWWLP.",
            "..PLWWLP..",
            "...PLLP...",
            "....PP...."
    }, ENEMY_PALETTE);

    /** 高速敵。三角形ベースで速そうに見せる。 */
    public static final PixelSprite ENEMY_FAST = new PixelSprite(new String[]{
            "....CC....",
            "...CLLC...",
            "..CLLLLC..",
            ".CLLLLLLC.",
            "CLLLLLLLLC",
            ".DCCCCCCD.",
            "..DCCCCD..",
            "...DCCD...",
            "....DD....",
            ".........."
    }, FAST_ENEMY_PALETTE);

    // ====== ボススプライト ======

    /**
     * ボス(待機)。32x32。
     * 大きな帽子と装飾を持つキャラ風の図形。
     */
    public static final PixelSprite BOSS_IDLE = new PixelSprite(new String[]{
            "..........DDDDDDDD..............",
            ".........DPPPPPPPPD.............",
            "........DPPPPPPPPPPD............",
            ".......DPRRRRRRRRRRPD...........",
            "......DRRRRRRRRRRRRRRD..........",
            ".......DDDDDDDDDDDDDDD..........",
            "........DHHHHHHHHHHHD...........",
            ".......DHHHHHHHHHHHHHD..........",
            "......DHSSSSSSSSSSSHHD..........",
            ".....DHSSWWSSSSWWSSSHHD.........",
            ".....DHSSWWSSSSWWSSSHHD.........",
            ".....DHSSSSSSSSSSSSSHHD.........",
            "......DHSSSRRRRRRSSSHD..........",
            ".......DHSSSSSSSSSSHD...........",
            "........DHHHHHHHHHHD............",
            "........DRRRRRRRRRRD............",
            "......DDRRRWWWWWWRRRDD..........",
            ".....DRRRWWWWWWWWWWRRRD.........",
            "....DRRRWWWWWWWWWWWWRRRD........",
            "...DRRRWWWWWWWWWWWWWWRRRD.......",
            "..DRRRWWWWWYYYYYYWWWWWRRRD......",
            "..DRRWWWWWYYYYYYYYWWWWWRRD......",
            "..DRRWWWWWYYYYYYYYWWWWWRRD......",
            "..DRRWWWWWWYYYYYYWWWWWWRRD......",
            "..DRRWWWWWWWWWWWWWWWWWWRRD......",
            "...DRRWWWWWWWWWWWWWWWWRRD.......",
            "....DRRWWWWWWWWWWWWWWRRD........",
            ".....DRRWWWWWWWWWWWWRRD.........",
            "......DRRWWWWWWWWWWRRD..........",
            ".......DRRRRRRRRRRRRD...........",
            "........DDDDDDDDDDDD............",
            "................................"
    }, BOSS_PALETTE);

    // ====== アニメーションパターン(ひな型) ======

    /**
     * 霊夢の歩行(2フレーム交互)。
     * 本格的にやるなら REIMU_WALK_LEFT, REIMU_WALK_RIGHT も追加する。
     * TODO: ちゃんと差分のあるスプライトに差し替える
     */
    public static final PixelSprite[] REIMU_WALK = { REIMU_IDLE, REIMU_IDLE };

    /** 魔理沙の歩行(同上ひな型) */
    public static final PixelSprite[] MARISA_WALK = { MARISA_IDLE, MARISA_IDLE };

    /** ボスのアイドル(同上ひな型) */
    public static final PixelSprite[] BOSS_ANIM = { BOSS_IDLE, BOSS_IDLE };
}
