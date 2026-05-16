package com.shootinguhyo.effect;

import com.shootinguhyo.character.PlayerCharacter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * BombImageRegistry：ボム発動時に画面に重ねるキャラ別の写真画像を保持する。
 *
 * 【役割】
 *  キャラクターID -> ボム画像 のマップ。クラスパスから1度だけロードし、
 *  以後は使い回す(毎フレーム読み直さない)。
 *
 * 【配置場所】
 *  src/main/resources/bomb/uhyowoman.jpg  (IMG_4417を改名 — メガネ＋紙コップ)
 *  src/main/resources/bomb/uhyoman.jpg    (IMG_4721を改名 — ラーメン)
 *  ※ファイルが無くてもクラッシュしない(nullを返す)。
 */
public final class BombImageRegistry {

    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private static final Map<String, Boolean> TRIED = new HashMap<>();

    /**
     * キャラID毎のクラスパスリソースのマッピング。
     * IMG_xxxx.jpg のままでも見つけるよう複数候補を試す。
     */
    private static final Map<String, String[]> RESOURCE_PATHS = new HashMap<>();
    static {
        RESOURCE_PATHS.put("uhyowoman", new String[]{
                "/bomb/uhyowoman.jpg", "/bomb/uhyowoman.png",
                "/bomb/IMG_4417.jpg", "/bomb/IMG_4417.JPG"
        });
        RESOURCE_PATHS.put("uhyoman", new String[]{
                "/bomb/uhyoman.jpg", "/bomb/uhyoman.png",
                "/bomb/IMG_4721.jpg", "/bomb/IMG_4721.JPG"
        });
    }

    private BombImageRegistry() {}

    /** キャラに対応するボム画像を返す。リソースが無ければnull。 */
    public static BufferedImage imageFor(PlayerCharacter character) {
        if (character == null) return null;
        return imageFor(character.getId());
    }

    public static BufferedImage imageFor(String characterId) {
        if (characterId == null) return null;
        if (CACHE.containsKey(characterId)) return CACHE.get(characterId);
        if (Boolean.TRUE.equals(TRIED.get(characterId))) return null;
        TRIED.put(characterId, Boolean.TRUE);

        String[] paths = RESOURCE_PATHS.get(characterId);
        if (paths == null) return null;
        for (String p : paths) {
            try (InputStream in = BombImageRegistry.class.getResourceAsStream(p)) {
                if (in == null) continue;
                BufferedImage img = ImageIO.read(in);
                if (img != null) {
                    CACHE.put(characterId, img);
                    return img;
                }
            } catch (Exception ignored) {
                // 次の候補へ
            }
        }
        return null;
    }
}
