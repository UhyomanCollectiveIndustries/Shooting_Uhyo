package com.shootinguhyo.dialog;

import com.shootinguhyo.graphics.PixelSprite;

/**
 * DialogLine：会話1つ分のセリフ。
 *
 * 【役割】
 *  「誰が」「何を」喋ったかを表す最小単位。
 *  会話シーンはDialogLineの配列として表現される。
 */
public class DialogLine {
    public enum Side { LEFT, RIGHT } // 立ち絵を画面のどちらに出すか

    private final String speakerName;
    private final String text;
    private final PixelSprite portrait;
    private final Side side;

    public DialogLine(String speakerName, String text, PixelSprite portrait, Side side) {
        this.speakerName = speakerName;
        this.text = text;
        this.portrait = portrait;
        this.side = side;
    }

    public String getSpeakerName() { return speakerName; }
    public String getText() { return text; }
    public PixelSprite getPortrait() { return portrait; }
    public Side getSide() { return side; }
}
