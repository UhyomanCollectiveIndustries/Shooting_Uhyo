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
 * Uhyoman：1人目のキャラ「広範囲攻撃型」のひな型。
 *
 * 【コンセプト】
 *  弾が広く扇状に広がるホーミング寄りキャラ。動きが少し速い。
 *  → 雑魚処理が得意・道中向きキャラのイメージ。
 *
 * 【TODO】
 *  - ホーミング弾の追尾ロジック実装
 *  - ボム時に「結界」を張る独自エフェクト
 *  - スプライトのアニメーション化(歩きモーション)
 */
public class Uhyoman implements PlayerCharacter {

    // ドット絵パレット (現状は仮の配色)
    private static final Map<Character, Color> PALETTE = PixelSprite.palette(
            'R', new Color(220, 60, 80),      // 赤(髪リボン等)
            'W', new Color(255, 240, 230),    // 肌の白
            'B', new Color(40, 40, 80),       // 黒髪
            'P', new Color(255, 180, 200),    // ピンク(衣装)
            'Y', new Color(255, 220, 90)      // 黄(アクセント)
    );

    // 16x16のキャラ仮ドット絵 (TODO: もっと作り込む)
    private static final String[] IN_GAME_PATTERN = {
            ".....RRRR......",
            "....RBBBBR.....",
            "...RBBBBBBR....",
            "...BBWWWWBB....",
            "..BBWWWWWWBB...",
            "..BWWWWWWWWB...",
            "...BWWWWWWB....",
            "...RBBBBBBR....",
            "..PPPPPPPPPP...",
            ".PPYPPPPPPYPP..",
            "PPPPPPPPPPPPPP.",
            "PP.PPPPPPPP.PP.",
            ".P..PPPPPP..P..",
            "....PPPPPP.....",
            ".....P..P......",
            ".....P..P......"
    };

    // 立ち絵(現状は仮で同じスプライト)。TODO: もっと大きいドット絵を用意
    private static final String[] PORTRAIT_PATTERN = IN_GAME_PATTERN;

    // 自機絵(PNG)を優先。無ければドット絵にフォールバック。
    // クラスパスのリソース解決は大文字小文字を区別するので複数候補を試す。
    private static final BufferedImage CHAR_IMAGE = ImageLoader.loadAny(
            "/character/uhyoman.png", "/character/Uhyoman.png",
            "/character/uhyoman.jpg", "/character/Uhyoman.jpg"
    );
    // 論理サイズ(scale倍で描画される基準)
    private static final int IN_GAME_LOGICAL_HEIGHT  = 24;  // 自機表示は控えめに
    private static final int PORTRAIT_LOGICAL_HEIGHT = 28;  // キャラ選択用は大きめ

    private final PixelSprite inGameSprite = CHAR_IMAGE != null
            ? new PixelSprite(CHAR_IMAGE, IN_GAME_LOGICAL_HEIGHT)
            : new PixelSprite(IN_GAME_PATTERN, PALETTE);
    private final PixelSprite portraitSprite = CHAR_IMAGE != null
            ? new PixelSprite(CHAR_IMAGE, PORTRAIT_LOGICAL_HEIGHT)
            : new PixelSprite(PORTRAIT_PATTERN, PALETTE);

    @Override
    public String getId() { return "uhyoman"; }

    @Override
    public String getDisplayName() { return "Uhyoman"; }

    @Override
    public String getProfile() {
        return "広範囲攻撃が得意な巫女タイプ。\n移動速度がやや速い。";
    }

    @Override
    public PixelSprite getInGameSprite() { return inGameSprite; }

    @Override
    public PixelSprite getPortraitSprite() { return portraitSprite; }

    @Override
    public double getNormalSpeed() { return 5.5; }

    @Override
    public double getFocusSpeed() { return 2.8; }

    @Override
    public double getHitboxRadius() { return 3.5; }

    /**
     * 通常ショットの生成。広く扇状に飛ぶ弾を作る。
     * TODO: パワーに応じてホーミング弾を追加する。
     */
    @Override
    public List<PlayerBullet> createShot(double x, double y, int power, boolean focus) {
        List<PlayerBullet> bullets = new ArrayList<>();
        int dmg = 10;
        double speed = 12.0;

        if (focus) {
            // 集中射撃(直進3発)
            for (int i = -1; i <= 1; i++) {
                bullets.add(new PlayerBullet(x + i * 3, y - 10, 0, -speed, dmg));
            }
        } else {
            // 扇状(本数はパワーで増加)
            int ways = 3 + (power / 100);
            double spread = Math.toRadians(10);
            for (int i = 0; i < ways; i++) {
                double angle = -Math.PI / 2 + spread * (i - (ways - 1) / 2.0);
                bullets.add(new PlayerBullet(x, y - 5,
                        Math.cos(angle) * speed, Math.sin(angle) * speed, dmg));
            }
        }
        return bullets;
    }
}
