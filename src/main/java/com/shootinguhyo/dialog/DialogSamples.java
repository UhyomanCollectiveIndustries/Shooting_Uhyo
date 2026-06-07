package com.shootinguhyo.dialog;

import com.shootinguhyo.character.CharacterRegistry;
import com.shootinguhyo.character.PlayerCharacter;
import com.shootinguhyo.entity.Boss;
import com.shootinguhyo.graphics.BossArtRegistry;
import com.shootinguhyo.graphics.PixelSprite;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DialogSamples：ボス戦前後の会話シーンのサンプル定義集。
 *
 * <p>花札の月モチーフでボスを設定:
 *  <ul>
 *    <li>Stage1: 松鶴の番人 ハナサキ</li>
 *    <li>Stage2: 桜幕の歌い手 サクラギ</li>
 *    <li>Stage3: 牡丹の蝶舞 ボタンヒメ</li>
 *    <li>Stage4: 月見の酒人 ススキ</li>
 *    <li>Stage5: 雨四光の遣い ヤナギ</li>
 *    <li>Stage6: 桐鳳の冠者 キリオウ</li>
 *  </ul>
 * </p>
 */
public final class DialogSamples {
    private DialogSamples() {}

    /** 顔アイコンの論理サイズ(PixelSprite.scale倍率を掛けた表示サイズを決める基準)。 */
    private static final int FACE_LOGICAL_HEIGHT = 24;

    /** ボスのフェイスアイコンPixelSpriteをキャッシュ付きで取得。 */
    private static final Map<Integer, PixelSprite> FACE_SPRITE_CACHE = new HashMap<>();
    private static PixelSprite bossFaceSprite(int stageNo) {
        if (FACE_SPRITE_CACHE.containsKey(stageNo)) return FACE_SPRITE_CACHE.get(stageNo);
        BufferedImage img = BossArtRegistry.faceFor(stageNo);
        PixelSprite sprite = img != null ? new PixelSprite(img, FACE_LOGICAL_HEIGHT) : null;
        FACE_SPRITE_CACHE.put(stageNo, sprite);
        return sprite;
    }

    /** 顔アイコンが用意されていなければプレイヤーの立ち絵を流用するフォールバック。 */
    private static PixelSprite bossPortraitFor(int stageNo, PixelSprite fallback) {
        PixelSprite face = bossFaceSprite(stageNo);
        return face != null ? face : fallback;
    }

    /** 短縮ヘルパ: 左/右セリフを連続で作る。 */
    private static DialogLine left (String name, String text, PixelSprite p) {
        return new DialogLine(name, text, p, DialogLine.Side.LEFT);
    }
    private static DialogLine right(String name, String text, PixelSprite p) {
        return new DialogLine(name, text, p, DialogLine.Side.RIGHT);
    }

    // ===================== STAGE 1 =====================

    public static List<DialogLine> preBossStage1(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar != null
                ? playerChar.getPortraitSprite()
                : CharacterRegistry.getDefault().getPortraitSprite();
        PixelSprite bp = bossPortraitFor(1, pl);
        String name = Boss.bossNameFor(1);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                left (who, "やっと出てきたわね…！", pl),
                right(name, "あら、迷い込んだ人間さん？\nここから先には行かせないわよ。", bp),
                left (who, "悪いけど、用があるの。\nどいてくれない？", pl),
                right(name, "ふふ、面白いわね。\n札一枚で軽く挨拶しましょうか。", bp)
        );
    }

    public static List<DialogLine> postBossStage1(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar.getPortraitSprite();
        PixelSprite bp = bossPortraitFor(1, pl);
        String name = Boss.bossNameFor(1);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                right(name, "強いのね…\nこんな最初の役じゃ私には勝てなかったか。", bp),
                left (who, "ありがとう。\n先を急がせてもらうわ。", pl)
        );
    }

    // ===================== STAGE 2 =====================

    public static List<DialogLine> preBossStage2(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar.getPortraitSprite();
        PixelSprite bp = bossPortraitFor(2, pl);
        String name = Boss.bossNameFor(2);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                left (who, "桜が散っている…\n誰かの仕業？", pl),
                right(name, "ようこそ、桜の幕の中へ。\n私の青き短冊が、貴方を彩るわ。", bp),
                left (who, "札遊びなら他所でやって。", pl),
                right(name, "あら、つれないのね。\nなら、一勝負いきましょうか。", bp)
        );
    }

    public static List<DialogLine> postBossStage2(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar.getPortraitSprite();
        PixelSprite bp = bossPortraitFor(2, pl);
        String name = Boss.bossNameFor(2);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                right(name, "見事…\n短冊では足止めできなかったか。", bp),
                left (who, "もっと強い手札がいるところに、\n用があるみたいね。", pl)
        );
    }

    // ===================== STAGE 3 =====================

    public static List<DialogLine> preBossStage3(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar.getPortraitSprite();
        PixelSprite bp = bossPortraitFor(3, pl);
        String name = Boss.bossNameFor(3);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                left (who, "…牡丹の香り。\nそろそろお出ましかしら。", pl),
                right(name, "鹿も猪も蝶も、\n揃って貴女を歓迎しているわ。", bp),
                left (who, "歓迎の弾幕は遠慮しておく。", pl),
                right(name, "あら残念。\nじゃあ、種札の宴をご一緒に。", bp)
        );
    }

    public static List<DialogLine> postBossStage3(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar.getPortraitSprite();
        PixelSprite bp = bossPortraitFor(3, pl);
        String name = Boss.bossNameFor(3);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                right(name, "種札では足りなかったかぁ…\n上の方々はもっと強いわよ。", bp),
                left (who, "上等、まとめてかかってきなさい。", pl)
        );
    }

    // ===================== STAGE 4 =====================

    public static List<DialogLine> preBossStage4(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar.getPortraitSprite();
        PixelSprite bp = bossPortraitFor(4, pl);
        String name = Boss.bossNameFor(4);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                left (who, "夜になったわね。\n月が綺麗…でも誰かいる。", pl),
                right(name, "花見で一杯、月見で一杯。\n貴女もどう？", bp),
                left (who, "弾幕を呑むのはお断り。", pl),
                right(name, "ふふ、酔わせてあげるわ。\nさあ、宴の始まりよ。", bp)
        );
    }

    public static List<DialogLine> postBossStage4(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar.getPortraitSprite();
        PixelSprite bp = bossPortraitFor(4, pl);
        String name = Boss.bossNameFor(4);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                right(name, "猪鹿蝶も、酒の役も効かないとはね…\nこの先には四光・五光の遣いがいるよ。", bp),
                left (who, "教えてくれて感謝するわ。\n気を引き締めるわね。", pl)
        );
    }

    // ===================== STAGE 5 =====================

    public static List<DialogLine> preBossStage5(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar.getPortraitSprite();
        PixelSprite bp = bossPortraitFor(5, pl);
        String name = Boss.bossNameFor(5);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                left (who, "雨が降ってきた…\n札の音が聞こえる。", pl),
                right(name, "雨に濡れた光は、太陽より眩いの。\n四光、お見せしましょうか？", bp),
                left (who, "そんな光、弾幕には要らない。", pl),
                right(name, "あなた、本当に怖いもの知らずね。\nなら、容赦はしないわよ。", bp)
        );
    }

    public static List<DialogLine> postBossStage5(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar.getPortraitSprite();
        PixelSprite bp = bossPortraitFor(5, pl);
        String name = Boss.bossNameFor(5);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                right(name, "四光すら届かないなんて…\n最後の役を打つ者は、私の比じゃないわよ。", bp),
                left (who, "じゃあ、最後の決着をつけてくる。", pl)
        );
    }

    // ===================== STAGE 6 (最終) =====================

    public static List<DialogLine> preBossStage6(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar.getPortraitSprite();
        PixelSprite bp = bossPortraitFor(6, pl);
        String name = Boss.bossNameFor(6);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                left (who, "ここが最深部…\n気配が違うわ。", pl),
                right(name, "よく辿り着いた、外の世界の者よ。\n札を語る前に名乗ろう、私が桐鳳の冠者。", bp),
                left (who, "悪いけど、その「五光」って役…\nここで止めさせてもらう。", pl),
                right(name, "ふっ、笑止。\nこの世の頂きを、見せてやろう。", bp)
        );
    }

    public static List<DialogLine> postBossStage6(PlayerCharacter playerChar) {
        PixelSprite pl = playerChar.getPortraitSprite();
        PixelSprite bp = bossPortraitFor(6, pl);
        String name = Boss.bossNameFor(6);
        String who = playerChar.getDisplayName();
        return Arrays.asList(
                right(name, "五光が…崩されるとは。\nお前さん、いい札を持っているな…", bp),
                left (who, "札じゃないわ。\nここまで連れてきてくれた、皆のおかげ。", pl),
                right(name, "ふっ、見事だ。\n夜明けの空を、お前に預けよう…", bp)
        );
    }

    // ===================== ディスパッチ =====================

    public static List<DialogLine> preBoss(int stageNo, PlayerCharacter playerChar) {
        return switch (stageNo) {
            case 1 -> preBossStage1(playerChar);
            case 2 -> preBossStage2(playerChar);
            case 3 -> preBossStage3(playerChar);
            case 4 -> preBossStage4(playerChar);
            case 5 -> preBossStage5(playerChar);
            case 6 -> preBossStage6(playerChar);
            default -> preBossStage1(playerChar);
        };
    }

    public static List<DialogLine> postBoss(int stageNo, PlayerCharacter playerChar) {
        return switch (stageNo) {
            case 1 -> postBossStage1(playerChar);
            case 2 -> postBossStage2(playerChar);
            case 3 -> postBossStage3(playerChar);
            case 4 -> postBossStage4(playerChar);
            case 5 -> postBossStage5(playerChar);
            case 6 -> postBossStage6(playerChar);
            default -> postBossStage1(playerChar);
        };
    }
}
