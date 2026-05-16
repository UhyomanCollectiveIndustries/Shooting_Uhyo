package com.shootinguhyo.character;

import com.shootinguhyo.entity.bullet.PlayerBullet;
import com.shootinguhyo.graphics.ImageLoader;
import com.shootinguhyo.graphics.PixelSprite;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Uhyowoman：2人目のキャラ「火力集中型」のひな型。
 *
 * 【コンセプト】
 *  弾が真上に集中するパワー型。動きは遅めだが攻撃力が高い。
 *  → 単体ボス戦やHP高い敵に強いキャラのイメージ。
 *
 * 【TODO】
 *  - レーザー(貫通弾)の実装
 *  - ボム時に星形の高威力エフェクト
 *  - 動作アニメーション
 */
public class Uhyowoman implements PlayerCharacter {

    private static final Map<Character, Color> PALETTE = PixelSprite.palette(
            'Y', new Color(255, 220, 90),     // 金髪
            'W', new Color(255, 240, 230),    // 肌の白
            'B', new Color(30, 30, 50),       // 黒(衣装/帽子)
            'K', new Color(60, 60, 100),      // 黒(影)
            'O', new Color(255, 140, 50),     // オレンジ(星アクセント)
            'P', new Color(180, 130, 220)     // 紫(衣装)
    );

    // 16x16のキャラ仮ドット絵
    private static final String[] IN_GAME_PATTERN = {
            "...BBBBBBBBBB..",
            "..BBKKKKKKKKBB.",
            "..BBBBBBBBBBBB.",
            "....YYYYYY.....",
            "...YYYYYYYY....",
            "...YWWWWWWY....",
            "...YWWWWWWY....",
            "....YWWWWY.....",
            "...PPPPPPPP....",
            "..PPOPPPPOPP...",
            ".PPPPPPPPPPPP..",
            ".PP.PPPPPP.PP..",
            "..P..PPPP..P...",
            "....PPPPPP.....",
            "....P....P.....",
            "....P....P....."
    };

    private static final String[] PORTRAIT_PATTERN = IN_GAME_PATTERN;

    // 自機絵(PNG)を優先。無ければドット絵にフォールバック。
    // クラスパスのリソース解決は大文字小文字を区別するので複数候補を試す。
    private static final BufferedImage CHAR_IMAGE = ImageLoader.loadAny(
            "/character/uhyowoman.png", "/character/Uhyowoman.png",
            "/character/uhyowoman.jpg", "/character/Uhyowoman.jpg"
    );
    private static final int IN_GAME_LOGICAL_HEIGHT  = 24;
    private static final int PORTRAIT_LOGICAL_HEIGHT = 28;

    private final PixelSprite inGameSprite = CHAR_IMAGE != null
            ? new PixelSprite(CHAR_IMAGE, IN_GAME_LOGICAL_HEIGHT)
            : new PixelSprite(IN_GAME_PATTERN, PALETTE);
    private final PixelSprite portraitSprite = CHAR_IMAGE != null
            ? new PixelSprite(CHAR_IMAGE, PORTRAIT_LOGICAL_HEIGHT)
            : new PixelSprite(PORTRAIT_PATTERN, PALETTE);

    @Override
    public String getId() { return "uhyowoman"; }

    @Override
    public String getDisplayName() { return "Uhyowoman"; }

    @Override
    public String getProfile() {
        return "前方集中砲火型。\n攻撃力が高く、ボス戦が得意。";
    }

    @Override
    public PixelSprite getInGameSprite() { return inGameSprite; }

    @Override
    public PixelSprite getPortraitSprite() { return portraitSprite; }

    @Override
    public double getNormalSpeed() { return 4.5; }

    @Override
    public double getFocusSpeed() { return 2.2; }

    @Override
    public double getHitboxRadius() { return 4.5; }

    /**
     * 通常ショットの生成。前方に集中する高威力弾。
     * TODO: パワー満タン時にレーザーを撃つようにする。
     */
    @Override
    public List<PlayerBullet> createShot(double x, double y, int power, boolean focus) {
        List<PlayerBullet> bullets = new ArrayList<>();
        int dmg = 14; // 威力高め
        double speed = 14.0;

        if (focus) {
            // フォーカス時:より高威力な単発太線
            bullets.add(new PlayerBullet(x, y - 10, 0, -speed, dmg * 2));
        } else {
            // 上方向中心の数本(本数はパワーで増加)
            int ways = 2 + (power / 100);
            for (int i = 0; i < ways; i++) {
                double offsetX = (i - (ways - 1) / 2.0) * 4;
                bullets.add(new PlayerBullet(x + offsetX, y - 5, 0, -speed, dmg));
            }
        }
        return bullets;
    }
}
