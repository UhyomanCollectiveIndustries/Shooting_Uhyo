package com.shootinguhyo.entity;

import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.graphics.BossArtRegistry;
import com.shootinguhyo.pattern.*;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
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
    private int spell1Hp = 10000;
    private int spell2Hp = 15000;
    private int spell3Hp = 25000;
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

    /** ステージ番号(画像レジストリの引き当てに使う)。0なら未設定。 */
    private int stageNo = 0;
    private BufferedImage bodyImage;

    public Boss(double x, double y) {
        this(x, y, 0);
    }

    /** ステージ番号付きコンストラクタ(全身画像の引き当てに使う)。 */
    public Boss(double x, double y, int stageNo) {
        super(x, y);
        this.stageNo = stageNo;
        // 全身画像はステージ番号があれば一度だけロードを試す
        if (stageNo > 0) {
            this.bodyImage = BossArtRegistry.bodyFor(stageNo);
        }
        // 難易度に応じて各フェーズのHPをスケール
        double hpMul = com.shootinguhyo.config.Difficulty.current().enemyHpMul;
        phase1Hp = (int)(phase1Hp * hpMul);
        spell1Hp = (int)(spell1Hp * hpMul);
        spell2Hp = (int)(spell2Hp * hpMul);
        spell3Hp = (int)(spell3Hp * hpMul);
        currentHp = phase1Hp;
        maxHp = phase1Hp;
    }

    public int getStageNo() { return stageNo; }

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

    /** 現在フェーズのHP残量比率(0.0〜1.0)。スペルの段階変化(必殺技の激化)に使う。 */
    private double hpRatio() {
        return maxHp > 0 ? Math.max(0.0, (double) currentHp / maxHp) : 0.0;
    }

    /**
     * スペルカードのHP閾値段階を返す。
     *  0 = 余裕(75%超)、1 = 中盤(50%〜75%)、2 = 終盤(25%〜50%)、3 = 瀕死(25%以下)。
     * 段階が上がるほど弾幕が激しくなる(=必殺技がHPで変化する)。
     */
    private int spellStage() {
        double r = hpRatio();
        if (r > 0.75) return 0;
        if (r > 0.50) return 1;
        if (r > 0.25) return 2;
        return 3;
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
        int stage = spellStage();
        // HPが減るほど腕の本数を増やし(3→4→5→6)、回転を速める
        int arms = 3 + stage;
        spiralAngle1 += 0.08 + stage * 0.015;
        spiralAngle2 -= 0.08 + stage * 0.015;

        if (frame % 3 == 0) {
            double spd = 2.5 + stage * 0.2;
            // 内側回転(ピンク)
            for (int i = 0; i < arms; i++) {
                double a = spiralAngle1 + Math.PI * 2 / arms * i;
                newBullets.add(new EnemyBullet(x, y,
                        Math.cos(a) * spd, Math.sin(a) * spd,
                        EnemyBullet.BulletSize.SMALL, new Color(255, 150, 200)));
            }
            // 逆回転(水色)
            for (int i = 0; i < arms; i++) {
                double a = spiralAngle2 + Math.PI * 2 / arms * i;
                newBullets.add(new EnemyBullet(x, y,
                        Math.cos(a) * spd, Math.sin(a) * spd,
                        EnemyBullet.BulletSize.SMALL, new Color(150, 200, 255)));
            }
        }
        // 瀕死(25%以下)になると追い打ちの全方位弾が加わる
        if (stage >= 3 && frame % 90 == 0) {
            RadialPattern rp = new RadialPattern(12, 2.2, radialAngle);
            newBullets.addAll(rp.generate(x, y, EnemyBullet.BulletSize.SMALL, new Color(255, 120, 160)));
            radialAngle += 0.15;
        }
    }

    /** SPELL2：波状弾＋たまに自機狙いの混合。波の角度を少しずつ変えてゆらぎを出す。 */
    private void updateSpell2() {
        int stage = spellStage();
        waveAngle += 0.05;
        // HPが減るほど波の本数を増やし(7→8→9→10)、撃つ間隔を詰める
        int waveCount = 7 + stage;
        int waveInterval = Math.max(12, 20 - stage * 3);
        if (frame % waveInterval == 0) {
            WavePattern wp = new WavePattern(Math.toRadians(60), waveCount, waveAngle);
            newBullets.addAll(wp.generate(x, y, EnemyBullet.BulletSize.SMALL, new Color(255, 150, 180)));
        }
        if (frame % 60 == 30) {
            // 終盤(50%以下)は自機狙いが3way→5wayに増える
            int aimWays = stage >= 2 ? 5 : 3;
            AimedPattern ap = new AimedPattern(aimWays, Math.toRadians(20), 3.0);
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
     * スペル名を返す。
     * <p>ステージ毎に「花札の役・札の名前」をテーマにし、
     *  ステージが進むほど強い役(カス→短冊→タネ→三光→四光→五光)になる。</p>
     */
    public String getSpellName() {
        // 各ステージ × 各SPELLフェーズで個別の札名/役名を返す。
        // 役の強さの目安: 1=カス, 2=短冊系, 3=タネ系, 4=三光/特殊役, 5=四光, 6=五光・最強
        return switch (stageNo) {
            case 1 -> switch (phase) {
                case SPELL1 -> "札符『松にカス』";
                case SPELL2 -> "札符『梅の短冊』";
                case SPELL3 -> "松梅『初春の囃子』";
                default -> "";
            };
            case 2 -> switch (phase) {
                case SPELL1 -> "札符『桜に幕』";
                case SPELL2 -> "短冊『青き紫陽花』";
                case SPELL3 -> "藤鳥『時鳥の鳴音』";
                default -> "";
            };
            case 3 -> switch (phase) {
                case SPELL1 -> "種符『菖蒲の八橋』";
                case SPELL2 -> "種符『牡丹の蝶舞』";
                case SPELL3 -> "獣符『萩野の猪』";
                default -> "";
            };
            case 4 -> switch (phase) {
                case SPELL1 -> "酒符『花見で一杯』";
                case SPELL2 -> "酒符『月見で一杯』";
                case SPELL3 -> "三獣『猪鹿蝶』";
                default -> "";
            };
            case 5 -> switch (phase) {
                case SPELL1 -> "光符『三光奪取』";
                case SPELL2 -> "雨光『雨の四光』";
                case SPELL3 -> "四光『満ちる栄誉』";
                default -> "";
            };
            case 6 -> switch (phase) {
                case SPELL1 -> "光符『桐に鳳凰』";
                case SPELL2 -> "光符『雨四光・嵐の宴』";
                case SPELL3 -> "極光『五光・天上の煌』";
                default -> "";
            };
            default -> switch (phase) {
                case SPELL1 -> "札符『無銘の幻』";
                case SPELL2 -> "札符『継ぎ札』";
                case SPELL3 -> "役符『未完の役』";
                default -> "";
            };
        };
    }

    /** ステージ毎のボス名(花札の月モチーフ)。DialogSamplesから参照される。 */
    public static String bossNameFor(int stageNo) {
        return switch (stageNo) {
            case 1 -> "松鶴の番人 ハナサキ";
            case 2 -> "桜幕の歌い手 サクラギ";
            case 3 -> "牡丹の蝶舞 ボタンヒメ";
            case 4 -> "月見の酒人 ススキ";
            case 5 -> "雨四光の遣い ヤナギ";
            case 6 -> "桐鳳の冠者 キリオウ";
            default -> "???";
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
        // 装飾図形(花のような形)は常に背景として描く
        int size = 30;
        g.setColor(new Color(180, 50, 150));
        drawDecorativeShape(g, (int)x, (int)y, size + 5);

        g.setColor(new Color(220, 80, 180));
        drawDecorativeShape(g, (int)x, (int)y, size);

        if (bodyImage != null) {
            // 全身画像があればそれを中央に描画(縦80pxに収まるよう拡大縮小)
            int targetH = 96;
            int targetW = (int) Math.round((double) bodyImage.getWidth() * targetH / bodyImage.getHeight());
            int dx = (int) x - targetW / 2;
            int dy = (int) y - targetH / 2;
            g.drawImage(bodyImage, dx, dy, targetW, targetH, null);
        } else {
            // 画像が無ければ従来の円ボディにフォールバック
            g.setColor(new Color(255, 150, 220));
            g.fill(new Ellipse2D.Double(x - 15, y - 15, 30, 30));
            g.setColor(new Color(255, 220, 240));
            g.fill(new Ellipse2D.Double(x - 8, y - 8, 16, 16));
        }

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
