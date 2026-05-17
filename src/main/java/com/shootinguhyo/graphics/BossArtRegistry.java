package com.shootinguhyo.graphics;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * BossArtRegistry：ステージボスの「フェイスアイコン」と「全身画像」を保持するレジストリ。
 *
 * <p>クラスパスのリソースから1回だけロードしてキャッシュする。
 *  ファイルが見つからない場合は null を返し、呼出側で従来描画にフォールバックする。</p>
 *
 * <p>配置パス例:
 *  <pre>
 *  src/main/resources/boss/stage1_face.png   (会話用の顔アイコン)
 *  src/main/resources/boss/stage1_body.png   (ゲーム中の全身画像)
 *  </pre>
 *  大文字小文字違い・JPG拡張子なども複数候補で試す。</p>
 */
public final class BossArtRegistry {
    private static final Map<Integer, BufferedImage> FACE_CACHE = new HashMap<>();
    private static final Map<Integer, BufferedImage> BODY_CACHE = new HashMap<>();
    private static final Map<Integer, Boolean> FACE_TRIED = new HashMap<>();
    private static final Map<Integer, Boolean> BODY_TRIED = new HashMap<>();

    private BossArtRegistry() {}

    /** ステージ番号に対応する顔アイコンを取得。無ければnull。 */
    public static BufferedImage faceFor(int stageNo) {
        if (FACE_CACHE.containsKey(stageNo)) return FACE_CACHE.get(stageNo);
        if (Boolean.TRUE.equals(FACE_TRIED.get(stageNo))) return null;
        FACE_TRIED.put(stageNo, Boolean.TRUE);

        BufferedImage img = ImageLoader.loadAny(candidates(stageNo, "face"));
        if (img != null) FACE_CACHE.put(stageNo, img);
        return img;
    }

    /** ステージ番号に対応する全身画像を取得。無ければnull。 */
    public static BufferedImage bodyFor(int stageNo) {
        if (BODY_CACHE.containsKey(stageNo)) return BODY_CACHE.get(stageNo);
        if (Boolean.TRUE.equals(BODY_TRIED.get(stageNo))) return null;
        BODY_TRIED.put(stageNo, Boolean.TRUE);

        BufferedImage img = ImageLoader.loadAny(candidates(stageNo, "body"));
        if (img != null) BODY_CACHE.put(stageNo, img);
        return img;
    }

    /** 大文字小文字違い・拡張子違いを含めた候補パスを生成。 */
    private static String[] candidates(int stageNo, String suffix) {
        // 例: stage1_face / Stage1_face / boss1_face など
        String[] bases = {
                "stage" + stageNo + "_" + suffix,
                "Stage" + stageNo + "_" + suffix,
                "boss" + stageNo + "_" + suffix,
                "Boss" + stageNo + "_" + suffix,
                "stage" + stageNo + "-" + suffix,
                "boss" + stageNo
        };
        String[] exts = { ".png", ".PNG", ".jpg", ".JPG", ".jpeg" };
        String[] result = new String[bases.length * exts.length];
        int i = 0;
        for (String b : bases) {
            for (String e : exts) {
                result[i++] = "/boss/" + b + e;
            }
        }
        return result;
    }
}
