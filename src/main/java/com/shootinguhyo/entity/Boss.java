package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.*;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Boss：ステージ終盤に登場する強敵。
 *
 * 【役割】
 *  - 4段階のフェーズ(通常→スペル1→スペル2→スペル3→撃破)を切り替えて戦う
 *  - 各フェーズで異なる弾幕パターンを撃ち、見た目と難易度を変化させる
 *  - HPバーとスペルカード時間制限の管理
 *
 * 【弾幕シューティングにおける「スペルカード」とは】
 *  ボス専用の必殺技フェーズ。制限時間内に倒すか時間切れにすると次フェーズに進む。
 *  時間切れより前に倒すとボーナス点。本作ではスペル中は受けるダメージを80%カットして
 *  避けゲームとしての歯ごたえを保っている。
 *
 * 【フェーズ遷移の仕組み】
 *  状態(Phase)をenumで管理し、checkPhaseTransition()でHPと時間を見て次フェーズへ。
 *  transitionTo()でHP・タイマー・フレーム・弾リストを初期化する。
 */
public class Boss extends Entity {
    /** ボスの状態(フェーズ)を表す。 */
    public enum Phase { PHASE1, SPELL1, SPELL2, SPELL3, DEFEATED }

    private Phase phase = Phase.PHASE1;
    // 各フェーズで個別のHP上限を持つ(難易度バランス調整しやすい設計)
    private int phase1Hp = 5000;
    private int spell1Hp = 4000;
    private int spell2Hp = 4000;
    private int spell3Hp = 6000;
    private int currentHp;
    private int maxHp;

    private int frame = 0;       // フェーズ内の経過フレーム
    private int spellTimer = 0;  // スペルカード残り時間
    private int spellMaxTime = 0;// スペルカード制限時間(満タン値)
    private double moveDir = 1;  // 左右移動方向(1=右、-1=左)
    private double playerX, playerY;
    private List<EnemyBullet> newBullets = new ArrayList<>();

    // スパイラル弾の現在の角度。フレーム毎に増減させて回転を表現する
    private double spiralAngle1 = 0;
    private double spiralAngle2 = Math.PI; // 反対方向(逆回転)用
    private double waveAngle = 0;          // 波打ち角度
    private double radialAngle = 0;        // 全方位弾の角度

    public Boss(double x, double y) {
        super(x, y);
        // 難易度に応じて各フェーズのHPをスケール
        double hpMul = com.shootinguhyo.config.Difficulty.current().enemyHpMul;
        phase1Hp = (int)(phase1Hp * hpMul);
        spell1Hp = (int)(spell1Hp * hpMul);
        spell2Hp = (int)(spell2Hp * hpMul);
        spell3Hp = (int)(spell3Hp * hpMul);
        currentHp = phase1Hp;
        maxHp = phase1Hp;
    }

    /** プレイヤー位置を教えてもらう(自機狙い弾用)。 */
    public void setPlayerPosition(double px, double py) {
        this.playerX = px;
        this.playerY = py;
    }

    /**
     * ボスの1フレーム更新。
     * 撃破後は早期return → 何も起きない(画面表示も消える)。
     */
    @Override
    public void update() {
        if (phase == Phase.DEFEATED) return;

        frame++;
        updateMovement();        // 位置を動かす
        updatePattern();         // 弾を撃つ
        checkPhaseTransition();  // フェーズ遷移チェック
    }

    /**
     * ボスの動き。
     * 横方向は左右往復、縦方向はサイン波で揺らす。
     * x座標が画面端に近づいたら方向反転。
     */
    private void updateMovement() {
        x += moveDir * 1.5;
        if (x > 340) moveDir = -1;
        if (x < 44) moveDir = 1;
        y = 80 + Math.sin(frame * 0.02) * 20; // ±20pxで上下にゆらゆら
    }

    /** 弾発射パターンをフェーズに応じて切り替え。 */
    private void updatePattern() {
        switch (phase) {
            case PHASE1 -> updatePhase1();
            case SPELL1 -> updateSpell1();
            case SPELL2 -> updateSpell2();
            case SPELL3 -> updateSpell3();
            default -> {}
        }
        if (spellTimer > 0) spellTimer--; // タイマー経過
    }

    /** PHASE1：3秒に1回だけ全方位弾。優しめのウォームアップ。 */
    private void updatePhase1() {
        if (frame % 180 == 0) {
            RadialPattern rp = new RadialPattern(8, 3.0, radialAngle);
            newBullets.addAll(rp.generate(x, y, EnemyBullet.BulletSize.MEDIUM, new Color(255, 100, 100)));
            radialAngle += 0.2; // 次回は少し回転させる(同じ場所に来ないように)
        }
    }

    /**
     * SPELL1：左右回転する二重スパイラル弾幕。
     * spiralAngle1は+方向、spiralAngle2は-方向に回し、交差する弾幕パターンを作る。
     * 3フレームに1回、各3方向の弾を撃つので合計毎秒40発の弾密度。
     */
    private void updateSpell1() {
        spiralAngle1 += 0.08;
        spiralAngle2 -= 0.08;

        if (frame % 3 == 0) {
            double spd = 2.5;
            // 内側回転(ピンク)
            for (int i = 0; i < 3; i++) {
                double a = spiralAngle1 + Math.PI * 2 / 3 * i;
                newBullets.add(new EnemyBullet(x, y,
                        Math.cos(a) * spd, Math.sin(a) * spd,
                        EnemyBullet.BulletSize.SMALL, new Color(255, 150, 200)));
            }
            // 逆回転(水色)
            for (int i = 0; i < 3; i++) {
                double a = spiralAngle2 + Math.PI * 2 / 3 * i;
                newBullets.add(new EnemyBullet(x, y,
                        Math.cos(a) * spd, Math.sin(a) * spd,
                        EnemyBullet.BulletSize.SMALL, new Color(150, 200, 255)));
            }
        }
    }

    /** SPELL2：波状弾＋たまに自機狙いの混合。波の角度を少しずつ変えてゆらぎを出す。 */
    private void updateSpell2() {
        waveAngle += 0.05;
        if (frame % 20 == 0) {
            WavePattern wp = new WavePattern(Math.toRadians(60), 7, waveAngle);
            newBullets.addAll(wp.generate(x, y, EnemyBullet.BulletSize.SMALL, new Color(255, 150, 180)));
        }
        if (frame % 60 == 30) {
            AimedPattern ap = new AimedPattern(3, Math.toRadians(20), 3.0);
            newBullets.addAll(ap.generate(x, y, playerX, playerY,
                    EnemyBullet.BulletSize.MEDIUM, new Color(255, 200, 220)));
        }
    }

    /** SPELL3：最終フェーズ。全方位弾＋スパイラル＋自機狙いの3種類同時。 */
    private void updateSpell3() {
        radialAngle += 0.05;
        spiralAngle1 += 0.12;

        if (frame % 60 == 0) {
            // 16方向の全方位弾(中サイズ・黄色)
            RadialPattern rp = new RadialPattern(16, 3.5, radialAngle);
            newBullets.addAll(rp.generate(x, y, EnemyBullet.BulletSize.MEDIUM, new Color(255, 220, 100)));
        }
        if (frame % 4 == 0) {
            // 4方向のスパイラル(高頻度)
            double spd = 3.0;
            for (int i = 0; i < 4; i++) {
                double a = spiralAngle1 + Math.PI / 2 * i;
                newBullets.add(new EnemyBullet(x, y,
                        Math.cos(a) * spd, Math.sin(a) * spd,
                        EnemyBullet.BulletSize.SMALL, new Color(255, 255, 200)));
            }
        }
        if (frame % 90 == 45) {
            // 自機狙いの5way
            AimedPattern ap = new AimedPattern(5, Math.toRadians(15), 4.0);
            newBullets.addAll(ap.generate(x, y, playerX, playerY,
                    EnemyBullet.BulletSize.SMALL, new Color(200, 255, 200)));
        }
    }

    /**
     * フェーズ遷移をチェック。
     * HP切れ または スペル時間切れ で次に進む。
     */
    private void checkPhaseTransition() {
        if (phase == Phase.PHASE1 && currentHp <= 0) {
            transitionTo(Phase.SPELL1, 400, 20 * 60); // 20秒
        } else if (phase == Phase.SPELL1 && (currentHp <= 0 || spellTimer <= 0)) {
            transitionTo(Phase.SPELL2, 400, 20 * 60);
        } else if (phase == Phase.SPELL2 && (currentHp <= 0 || spellTimer <= 0)) {
            transitionTo(Phase.SPELL3, 600, 30 * 60); // 最後は30秒と長め
        } else if (phase == Phase.SPELL3 && (currentHp <= 0 || spellTimer <= 0)) {
            phase = Phase.DEFEATED;
            active = false;
        }
    }

    /**
     * フェーズ切替時の初期化処理。
     * HP・タイマー・経過フレーム・既存弾を全リセット。
     */
    private void transitionTo(Phase newPhase, int hp, int timer) {
        phase = newPhase;
        currentHp = hp;
        maxHp = hp;
        spellTimer = timer;
        spellMaxTime = timer;
        frame = 0;
        newBullets.clear();
    }

    /**
     * 被ダメージ処理。
     * スペルカード中はダメージを80%カット → スペルを早く終わらせにくくして避ける時間を確保。
     */
    public void takeDamage(int dmg) {
        if (phase == Phase.DEFEATED) return;
        if (phase == Phase.SPELL1 || phase == Phase.SPELL2 || phase == Phase.SPELL3) {
            dmg = (int)(dmg * 0.2);
        }
        currentHp -= dmg;
        if (currentHp < 0) currentHp = 0;
    }

    public List<EnemyBullet> getAndClearNewBullets() {
        List<EnemyBullet> bullets = new ArrayList<>(newBullets);
        newBullets.clear();
        return bullets;
    }

    public Phase getPhase() { return phase; }
    public int getCurrentHp() { return currentHp; }
    public int getMaxHp() { return maxHp; }
    public int getSpellTimer() { return spellTimer; }
    public int getSpellMaxTime() { return spellMaxTime; }
    public boolean isDefeated() { return phase == Phase.DEFEATED; }

    /**
     * スペル名を返す。switch式で各フェーズに対応する名前を取り出す。
     * 日本語はUnicodeエスケープで埋め込んでいる(ソースのエンコーディングに依存しないため)。
     */
    public String getSpellName() {
        return switch (phase) {
            case SPELL1 -> "夢符『幻想の弾幕』";
            case SPELL2 -> "花符『桜吹雪』";
            case SPELL3 -> "靈符『幻想の結界』";
            default -> "";
        };
    }

    /** スペル中の画面オーバーレイ色。透明度60で背景がうっすら染まる程度。 */
    public Color getSpellColor() {
        return switch (phase) {
            case SPELL1 -> new Color(255, 150, 200, 60);
            case SPELL2 -> new Color(255, 200, 220, 60);
            case SPELL3 -> new Color(255, 220, 100, 60);
            default -> new Color(0, 0, 0, 0);
        };
    }

    /**
     * ボスの描画。装飾図形2重 + 中央の円 + HPバー。
     * 大きく見せるために多重で描画している。
     */
    @Override
    public void draw(Graphics2D g) {
        int size = 30;
        g.setColor(new Color(180, 50, 150));
        drawDecorativeShape(g, (int)x, (int)y, size + 5);

        g.setColor(new Color(220, 80, 180));
        drawDecorativeShape(g, (int)x, (int)y, size);

        // 中央の本体(円)
        g.setColor(new Color(255, 150, 220));
        g.fill(new Ellipse2D.Double(x - 15, y - 15, 30, 30));

        g.setColor(new Color(255, 220, 240));
        g.fill(new Ellipse2D.Double(x - 8, y - 8, 16, 16));

        drawHpBar(g);
    }

    /**
     * 装飾図形(花のような形)を描く。
     * 8点のうち偶数番目を大きく、奇数番目を小さく(60%)することで凹凸のある形に。
     * frame * 0.02 で角度をずらすことで時間経過とともに回転する。
     */
    private void drawDecorativeShape(Graphics2D g, int cx, int cy, int size) {
        Path2D shape = new Path2D.Double();
        int points = 8;
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2 / points * i - Math.PI / 8 + frame * 0.02;
            double r = (i % 2 == 0) ? size : size * 0.6;
            double px = cx + Math.cos(angle) * r;
            double py = cy + Math.sin(angle) * r;
            if (i == 0) shape.moveTo(px, py);
            else shape.lineTo(px, py);
        }
        shape.closePath();
        g.fill(shape);
    }

    /**
     * HPバーを画面上部に描く。
     * 赤い背景の上に、現在HPの比率分だけ色付きバーを重ねる方式。
     * 色はフェーズによって変わり、視覚的にフェーズを把握しやすくしている。
     */
    private void drawHpBar(Graphics2D g) {
        int barW = 300, barH = 8;
        int barX = 42, barY = 14;
        g.setColor(new Color(80, 0, 0)); // 背景(暗い赤)
        g.fillRect(barX, barY, barW, barH);
        float ratio = maxHp > 0 ? (float) currentHp / maxHp : 0;
        Color barColor = switch (phase) {
            case SPELL1 -> new Color(255, 150, 200);
            case SPELL2 -> new Color(255, 200, 220);
            case SPELL3 -> new Color(255, 220, 100);
            default -> new Color(255, 80, 80);
        };
        g.setColor(barColor);
        g.fillRect(barX, barY, (int)(barW * ratio), barH);
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barW, barH); // 枠
    }
}
