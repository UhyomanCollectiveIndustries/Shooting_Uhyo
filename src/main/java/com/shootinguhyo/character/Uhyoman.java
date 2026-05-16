package com.shootinguhyo.character;

import com.shootinguhyo.entity.Player;
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

    /** 自機ショットの基本ダメージ。 */
    private static final int BASE_DAMAGE = 10;

    /**
     * パワー値から本数を決める。
     *   P<10 :3, P>=10:4, P>=20:5, P>=50:6, P>=125:7
     */
    private static int waysForPower(double power) {
        if (power >= 125) return 7;
        if (power >= 50)  return 6;
        if (power >= 20)  return 5;
        if (power >= 10)  return 4;
        return 3;
    }

    /**
     * 通常ショットの生成。本数はパワーで増える。
     */
    @Override
    public List<PlayerBullet> createShot(double x, double y, double power, boolean focus) {
        List<PlayerBullet> bullets = new ArrayList<>();
        int dmg = BASE_DAMAGE;
        double speed = 12.0;

        if (focus) {
            // フォーカス: 集中して直進する縦並びショット
            int ways = waysForPower(power);
            for (int i = 0; i < ways; i++) {
                double offsetX = (i - (ways - 1) / 2.0) * 3;
                bullets.add(new PlayerBullet(x + offsetX, y - 10, 0, -speed, dmg));
            }
        } else {
            // 通常: 扇状に広がる
            int ways = waysForPower(power);
            double spread = Math.toRadians(10);
            for (int i = 0; i < ways; i++) {
                double angle = -Math.PI / 2 + spread * (i - (ways - 1) / 2.0);
                bullets.add(new PlayerBullet(x, y - 5,
                        Math.cos(angle) * speed, Math.sin(angle) * speed, dmg));
            }
        }
        return bullets;
    }

    /**
     * オプション(左右のアタッチメント)の毎フレーム更新。
     * <ul>
     *   <li>P>=20 で起動。左右に固定で出る</li>
     *   <li>最寄り敵に向けてホーミング弾を自動発射(攻撃力は通常の半分)</li>
     *   <li>P>=50: 発射レート短縮</li>
     *   <li>P>=125: 発射レートさらに短縮 + 2発同時発射</li>
     * </ul>
     */
    @Override
    public void updateOptions(Player p, boolean focus, List<PlayerBullet> newBullets) {
        double power = p.getPower();
        boolean active = power >= 20;
        p.leftOption.active = active;
        p.rightOption.active = active;
        if (!active) return;

        // 位置(フォーカス時は少し内側に寄せる)
        double sideOffset = focus ? 14 : 20;
        p.leftOption.x  = p.x - sideOffset;
        p.leftOption.y  = p.y + 4;
        p.rightOption.x = p.x + sideOffset;
        p.rightOption.y = p.y + 4;

        // 発射パラメータ
        int cooldown = power >= 125 ? 6 : power >= 50 ? 10 : 15;
        int shotsPerFire = power >= 125 ? 2 : 1;
        int dmg = BASE_DAMAGE / 2; // 半分

        if (p.leftOption.fireCooldown > 0)  p.leftOption.fireCooldown--;
        if (p.rightOption.fireCooldown > 0) p.rightOption.fireCooldown--;

        fireOption(p, p.leftOption,  cooldown, shotsPerFire, dmg, newBullets);
        fireOption(p, p.rightOption, cooldown, shotsPerFire, dmg, newBullets);
    }

    private static void fireOption(Player p, Player.OptionState opt,
                                   int cooldown, int shots, int dmg,
                                   List<PlayerBullet> bullets) {
        if (opt.fireCooldown > 0) return;
        opt.fireCooldown = cooldown;

        double speed = 11.0;
        // 最寄り敵に向ける。無ければ真上
        double tx = p.hasNearestEnemy() ? p.getNearestEnemyX() : opt.x;
        double ty = p.hasNearestEnemy() ? p.getNearestEnemyY() : opt.y - 100;
        double dx = tx - opt.x;
        double dy = ty - opt.y;
        // 真横〜下向きには撃たせない(上方向限定)
        if (dy > -10) dy = -100;
        double dist = Math.max(1.0, Math.sqrt(dx * dx + dy * dy));
        double vx = dx / dist * speed;
        double vy = dy / dist * speed;

        for (int i = 0; i < shots; i++) {
            double off = (i - (shots - 1) / 2.0) * 4;
            bullets.add(new PlayerBullet(opt.x + off, opt.y, vx, vy, dmg));
        }
    }

    /** オプションを赤いコアの白丸で描画。 */
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
        g.setColor(new Color(220, 80, 110));
        g.fillOval((int) opt.x - 2, (int) opt.y - 2, 4, 4);
    }
}
