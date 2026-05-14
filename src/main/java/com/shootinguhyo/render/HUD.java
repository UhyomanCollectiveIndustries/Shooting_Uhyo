package com.shootinguhyo.render;

import com.shootinguhyo.entity.Player;
import com.shootinguhyo.entity.Boss;

import java.awt.*;

/**
 * HUD：Head-Up Display(ヘッドアップディスプレイ)
 *
 * 【役割】
 *  画面右側に「スコア」「ハイスコア」「残機」「ボム数」「パワー」「グレイズ数」を表示する。
 *  ボス戦中はスペルカードの残り時間も表示。
 *
 * 【なぜ専用クラスにするのか】
 *  GamePanelにすべて書くと長くなりすぎるため、UI関連だけ切り出してメンテしやすくする。
 *  「描画責務」を分割する設計の典型例。
 */
public class HUD {
    private static final int HUD_X = 384;     // HUDの左端x(フィールド幅と一致)
    private static final int HUD_WIDTH = 192; // HUDの幅
    private long hiScore = 0;

    /** 現在スコアより高ければハイスコアを更新する。 */
    public void update(long score) {
        if (score > hiScore) hiScore = score;
    }

    /**
     * HUD全体を描画。
     *
     * 【表示順序の意図】
     *  ・スコア類(現状/最高)を最上部に
     *  ・残機・ボム・パワー・グレイズを続けて並べる
     *  ・スペルタイマーは特別枠としてさらに下に表示
     *
     * 【記号の意味】
     *  ハート(♥)で残機、ダイヤ(◆)でボム数を表現 → 一目で分かりやすい。
     */
    public void draw(Graphics2D g, Player player, Boss boss) {
        // HUD背景パネル
        g.setColor(new Color(10, 5, 30));
        g.fillRect(HUD_X, 0, HUD_WIDTH, 448);
        // フィールドとの境界線
        g.setColor(new Color(100, 80, 150));
        g.drawLine(HUD_X, 0, HUD_X, 448);

        int x = HUD_X + 12;
        int y = 30;

        g.setFont(new Font("Monospaced", Font.PLAIN, 11));

        // SCORE：9桁0埋め(%09d)で表示すると桁が揃って格好良い
        g.setColor(new Color(200, 180, 255));
        g.drawString("SCORE", x, y);
        g.setColor(Color.WHITE);
        g.drawString(String.format("%09d", player.getScore()), x, y + 16);

        y += 40;
        g.setColor(new Color(200, 180, 255));
        g.drawString("HI-SCORE", x, y);
        g.setColor(Color.WHITE);
        g.drawString(String.format("%09d", hiScore), x, y + 16);

        // PLAYER：残機数だけハートマークを繰り返す
        y += 40;
        g.setColor(new Color(200, 180, 255));
        g.drawString("PLAYER", x, y);
        g.setColor(new Color(255, 100, 100));
        StringBuilder lives = new StringBuilder();
        for (int i = 0; i < player.getLives(); i++) lives.append("♥");
        g.drawString(lives.toString(), x, y + 16);

        // BOMB：ダイヤマークを繰り返し
        y += 40;
        g.setColor(new Color(200, 180, 255));
        g.drawString("BOMB", x, y);
        g.setColor(new Color(100, 200, 255));
        StringBuilder bombs = new StringBuilder();
        for (int i = 0; i < player.getBombs(); i++) bombs.append("◆");
        g.drawString(bombs.toString(), x, y + 16);

        // POWER：0〜400を0.00〜4.00として小数点表記に。見栄え目的の見せ方
        y += 40;
        g.setColor(new Color(200, 180, 255));
        g.drawString("POWER", x, y);
        g.setColor(Color.WHITE);
        g.drawString(String.format("%.2f", player.getPower() / 100.0), x, y + 16);

        // GRAZE：弾を掠った回数
        y += 40;
        g.setColor(new Color(200, 180, 255));
        g.drawString("GRAZE", x, y);
        g.setColor(Color.WHITE);
        g.drawString(String.format("%04d", player.getGraze()), x, y + 16);

        // スペルカード残り時間(ボス戦中の特別表示)
        if (boss != null && !boss.isDefeated()) {
            Boss.Phase phase = boss.getPhase();
            if (phase == Boss.Phase.SPELL1 || phase == Boss.Phase.SPELL2 || phase == Boss.Phase.SPELL3) {
                y += 50;
                int timerSec = boss.getSpellTimer() / 60; // フレーム→秒変換
                g.setColor(new Color(255, 220, 100));
                g.drawString("TIME", x, y);
                // 残り10秒未満は赤色で警告
                g.setColor(timerSec < 10 ? Color.RED : Color.WHITE);
                g.setFont(new Font("Monospaced", Font.BOLD, 18));
                g.drawString(String.format("%02d", timerSec), x, y + 22);
            }
        }
    }
}
