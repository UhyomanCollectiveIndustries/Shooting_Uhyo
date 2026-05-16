package com.shootinguhyo.character;

import com.shootinguhyo.entity.Player;
import com.shootinguhyo.entity.bullet.BeamBullet;
import com.shootinguhyo.entity.bullet.PlayerBullet;
import com.shootinguhyo.graphics.ImageLoader;
import com.shootinguhyo.graphics.PixelSprite;

import java.awt.Color;
import java.awt.Graphics2D;
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

    private static final int BASE_DAMAGE = 14;

    /**
     * フォーカス時の弾数。P=10で1→3、その後拡張。
     */
    private static int focusWaysForPower(double power) {
        if (power >= 125) return 7;
        if (power >= 50)  return 5;
        if (power >= 20)  return 4;
        if (power >= 10)  return 3;
        return 1;
    }

    /** 通常時の弾数。 */
    private static int normalWaysForPower(double power) {
        if (power >= 125) return 5;
        if (power >= 50)  return 4;
        if (power >= 20)  return 3;
        if (power >= 10)  return 2;
        return 2;
    }

    /**
     * 通常ショットの生成。前方集中型。
     */
    @Override
    public List<PlayerBullet> createShot(double x, double y, double power, boolean focus) {
        List<PlayerBullet> bullets = new ArrayList<>();
        int dmg = BASE_DAMAGE;
        double speed = 14.0;

        if (focus) {
            int ways = focusWaysForPower(power);
            // 中央列は威力2倍(芯)、外側は通常
            for (int i = 0; i < ways; i++) {
                double offsetX = (i - (ways - 1) / 2.0) * 3;
                int d = (i == ways / 2 && ways % 2 == 1) ? dmg * 2 : dmg;
                bullets.add(new PlayerBullet(x + offsetX, y - 10, 0, -speed, d));
            }
        } else {
            int ways = normalWaysForPower(power);
            for (int i = 0; i < ways; i++) {
                double offsetX = (i - (ways - 1) / 2.0) * 5;
                bullets.add(new PlayerBullet(x + offsetX, y - 5, 0, -speed, dmg));
            }
        }
        return bullets;
    }

    /**
     * オプション(左右のアタッチメント)を更新。
     * <ul>
     *   <li>P>=20で起動</li>
     *   <li>フォーカス中は前方に移動</li>
     *   <li>ビーム(BeamBullet)を高頻度で発射(攻撃力は通常の半分)</li>
     *   <li>P>=50: ビーム太め、発射レート向上</li>
     *   <li>P>=125: さらに太い + 連射</li>
     * </ul>
     */
    @Override
    public void updateOptions(Player p, boolean focus, List<PlayerBullet> newBullets) {
        double power = p.getPower();
        boolean active = power >= 20;
        p.leftOption.active = active;
        p.rightOption.active = active;
        if (!active) return;

        // 位置 — フォーカス時は前方、通常時は左右
        if (focus) {
            p.leftOption.x  = p.x - 10;
            p.leftOption.y  = p.y - 18;
            p.rightOption.x = p.x + 10;
            p.rightOption.y = p.y - 18;
        } else {
            p.leftOption.x  = p.x - 22;
            p.leftOption.y  = p.y + 4;
            p.rightOption.x = p.x + 22;
            p.rightOption.y = p.y + 4;
        }

        int cooldown  = power >= 125 ? 2 : power >= 50 ? 3 : 4;
        int beamWidth = power >= 125 ? 10 : power >= 50 ? 6 : 4;
        int dmg = BASE_DAMAGE / 2;

        if (p.leftOption.fireCooldown > 0)  p.leftOption.fireCooldown--;
        if (p.rightOption.fireCooldown > 0) p.rightOption.fireCooldown--;

        fireBeam(p.leftOption,  cooldown, beamWidth, dmg, newBullets);
        fireBeam(p.rightOption, cooldown, beamWidth, dmg, newBullets);
    }

    private static void fireBeam(Player.OptionState opt, int cooldown,
                                 int beamWidth, int dmg,
                                 List<PlayerBullet> bullets) {
        if (opt.fireCooldown > 0) return;
        opt.fireCooldown = cooldown;
        // 真上に高速で飛ばす(連射でビーム風に見せる)
        bullets.add(new BeamBullet(opt.x, opt.y - 6, 0, -22, dmg, beamWidth));
    }

    /** オプションを黄色いコアの白い菱形で描画。 */
    @Override
    public void drawOptions(Player p, Graphics2D g) {
        drawOne(g, p.leftOption);
        drawOne(g, p.rightOption);
    }

    private static void drawOne(Graphics2D g, Player.OptionState opt) {
        if (!opt.active) return;
        int r = 5;
        g.setColor(new Color(255, 255, 255, 220));
        g.fillOval((int) opt.x - r, (int) opt.y - r, r * 2, r * 2);
        g.setColor(new Color(255, 220, 80));
        g.fillOval((int) opt.x - 2, (int) opt.y - 2, 4, 4);
    }
}
