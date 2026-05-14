package com.shootinguhyo.character;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CharacterRegistry：選択可能キャラクターの一覧を保持するクラス。
 *
 * 【役割】
 *  - 「使えるキャラ」を1か所で管理する
 *  - キャラ選択画面はここから取得し、ゲーム本体は選ばれたPlayerCharacterを受け取る
 *
 * 【シングルトン的に使う】
 *  static のリストとして提供。インスタンスを作らずに `CharacterRegistry.all()` で取得。
 *
 * 【拡張方法】
 *  新キャラを追加したい場合は static 初期化のlistにaddするだけ。
 */
public final class CharacterRegistry {
    private CharacterRegistry() {}

    /** 全キャラのインスタンスリスト(順番がそのままキャラ選択UIの並び順)。 */
    private static final List<PlayerCharacter> ALL = new ArrayList<>();

    static {
        ALL.add(new Uhyoman());
        ALL.add(new Uhyowoman());
        // TODO: 新キャラを追加するときはここに add する
    }

    /** 全キャラの読み取り専用ビューを返す。 */
    public static List<PlayerCharacter> all() {
        return Collections.unmodifiableList(ALL);
    }

    /** 文字列IDからキャラを取得。見つからなければデフォルト(先頭)を返す。 */
    public static PlayerCharacter byId(String id) {
        for (PlayerCharacter c : ALL) {
            if (c.getId().equals(id)) return c;
        }
        return ALL.get(0);
    }

    /** CharacterType からキャラを取得。 */
    public static PlayerCharacter byType(CharacterType type) {
        return byId(type.getId());
    }

    /** デフォルト(タイトルから直接プレイした場合に使う)キャラ。 */
    public static PlayerCharacter getDefault() {
        return ALL.get(0);
    }
}
