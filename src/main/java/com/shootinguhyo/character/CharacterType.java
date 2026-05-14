package com.shootinguhyo.character;

/**
 * CharacterType：選択可能な自機キャラクターの識別子。
 *
 * 【役割】
 *  ゲーム全体で「今どのキャラを使っているか」を表すための列挙型。
 *  PlayerCharacter#getId() の文字列IDと対応するが、こちらは
 *  switch等で型安全に分岐したい場合に使う。
 *
 * 【既存実装との対応】
 *  UHYOMAN   -> Uhyoman
 *  UHYOWOMAN -> Uhyowoman
 *
 * 【今後の拡張】
 *  3人目以降を追加するときはここに値を増やし、CharacterRegistryに登録する。
 */
public enum CharacterType {
    UHYOMAN("uhyoman"),
    UHYOWOMAN("uhyowoman");

    private final String id;

    CharacterType(String id) {
        this.id = id;
    }

    /** PlayerCharacter#getId() と同じ文字列ID。 */
    public String getId() {
        return id;
    }

    /** 文字列IDから enum を逆引きする。見つからなければ null。 */
    public static CharacterType fromId(String id) {
        for (CharacterType t : values()) {
            if (t.id.equals(id)) return t;
        }
        return null;
    }
}
