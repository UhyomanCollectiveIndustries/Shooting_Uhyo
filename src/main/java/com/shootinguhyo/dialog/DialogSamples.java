package com.shootinguhyo.dialog;

import com.shootinguhyo.character.CharacterRegistry;
import com.shootinguhyo.character.PlayerCharacter;
import com.shootinguhyo.graphics.PixelSprite;

import java.util.Arrays;
import java.util.List;

/**
 * DialogSamples：ボス戦前後の会話シーンのサンプル定義集。
 *
 * 【役割】
 *  各ステージのボス戦突入前・撃破後の会話をまとめて持っておく。
 *  外部ファイル(JSON等)から読み込む形に置き換えやすいよう、
 *  ここでは「Javaコードに直書き」のひな型。
 *
 * 【使い方】
 *  DialogScene scene = new DialogScene(DialogSamples.preBossStage1(character));
 *  // GamePanel側で scene.update() / scene.draw() を呼ぶ
 *
 * 【TODO】
 *  - ステージ2〜6のボス会話を追加
 *  - 各ボス専用の立ち絵スプライトを用意し、ここから参照
 *  - シナリオライターが編集しやすいよう外部ファイル化(JSON/YAML)
 */
public final class DialogSamples {
    private DialogSamples() {}

    /** ステージ1のボス戦前会話(サンプル)。 */
    public static List<DialogLine> preBossStage1(PlayerCharacter playerChar) {
        PixelSprite playerPortrait = playerChar != null
                ? playerChar.getPortraitSprite()
                : CharacterRegistry.getDefault().getPortraitSprite();

        // ボスの立ち絵がまだ専用に無いので、暫定で同じスプライトを使う(TODO)
        PixelSprite bossPortrait = playerPortrait;

        return Arrays.asList(
                new DialogLine(playerChar.getDisplayName(),
                        "やっと出てきたわね…！",
                        playerPortrait, DialogLine.Side.LEFT),
                new DialogLine("???",
                        "あら、迷い込んだ人間さんかしら？\nここから先には行かせないわよ。",
                        bossPortrait, DialogLine.Side.RIGHT),
                new DialogLine(playerChar.getDisplayName(),
                        "悪いけど、用があるの。\nどいてくれない？",
                        playerPortrait, DialogLine.Side.LEFT),
                new DialogLine("???",
                        "ふふ、面白いわね。\nなら、力ずくでどうぞ。",
                        bossPortrait, DialogLine.Side.RIGHT)
        );
    }

    /** ステージ1のボス撃破後会話(サンプル)。 */
    public static List<DialogLine> postBossStage1(PlayerCharacter playerChar) {
        PixelSprite playerPortrait = playerChar.getPortraitSprite();
        PixelSprite bossPortrait = playerPortrait; // TODO: ボス専用立ち絵に差し替え

        return Arrays.asList(
                new DialogLine("???",
                        "強いのね…\n通っていいわ。",
                        bossPortrait, DialogLine.Side.RIGHT),
                new DialogLine(playerChar.getDisplayName(),
                        "ありがとう。\n先を急ぐわ。",
                        playerPortrait, DialogLine.Side.LEFT)
        );
    }

    /**
     * ステージ番号からプリボス会話を返す汎用関数。
     * ステージごとの個別シナリオは今後この中で分岐させる(現状は仮テキスト)。
     */
    public static List<DialogLine> preBoss(int stageNo, PlayerCharacter playerChar) {
        if (stageNo == 1) return preBossStage1(playerChar);

        PixelSprite playerPortrait = playerChar.getPortraitSprite();
        PixelSprite bossPortrait = playerPortrait;
        String bossName = bossNameFor(stageNo);

        return Arrays.asList(
                new DialogLine(playerChar.getDisplayName(),
                        "ここがステージ" + stageNo + "か…！",
                        playerPortrait, DialogLine.Side.LEFT),
                new DialogLine(bossName,
                        "よくぞここまで来た。\nしかしここは通さん。",
                        bossPortrait, DialogLine.Side.RIGHT),
                new DialogLine(playerChar.getDisplayName(),
                        "悪いけど、押し通らせてもらう！",
                        playerPortrait, DialogLine.Side.LEFT)
        );
    }

    /**
     * ステージ番号からポストボス会話を返す汎用関数。
     * 最終ステージ(6)は次のエンディングに繋がる流れに。
     */
    public static List<DialogLine> postBoss(int stageNo, PlayerCharacter playerChar) {
        if (stageNo == 1) return postBossStage1(playerChar);

        PixelSprite playerPortrait = playerChar.getPortraitSprite();
        PixelSprite bossPortrait = playerPortrait;
        String bossName = bossNameFor(stageNo);

        if (stageNo >= 6) {
            return Arrays.asList(
                    new DialogLine(bossName,
                            "ここまでとは…\nこの宇宙の運命、預けたぞ…",
                            bossPortrait, DialogLine.Side.RIGHT),
                    new DialogLine(playerChar.getDisplayName(),
                            "ようやく終わった…\n平和が戻ったのね。",
                            playerPortrait, DialogLine.Side.LEFT)
            );
        }

        return Arrays.asList(
                new DialogLine(bossName,
                        "見事…通るがいい。",
                        bossPortrait, DialogLine.Side.RIGHT),
                new DialogLine(playerChar.getDisplayName(),
                        "ありがと、先を急ぐわ。",
                        playerPortrait, DialogLine.Side.LEFT)
        );
    }

    /** 仮のボス名(差し替え予定)。 */
    private static String bossNameFor(int stageNo) {
        return switch (stageNo) {
            case 1 -> "ステージ1ボス";
            case 2 -> "ステージ2ボス";
            case 3 -> "ステージ3ボス";
            case 4 -> "ステージ4ボス";
            case 5 -> "ステージ5ボス";
            case 6 -> "ラスボス『ちくしょー』";
            default -> "???";
        };
    }
}
