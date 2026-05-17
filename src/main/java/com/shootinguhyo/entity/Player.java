package com.shootinguhyo.entity;

import com.shootinguhyo.InputHandler;
import com.shootinguhyo.character.PlayerCharacter;
import com.shootinguhyo.effect.BombImageRegistry;
import com.shootinguhyo.entity.bullet.PlayerBullet;
import com.shootinguhyo.util.MathUtil;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Player：自機(プレイヤーが操作する機体)。
 *
 * 【役割】
 *  - キー入力を受けて移動・射撃・ボム発動を行う
 *  - 残機、ボム数、パワー、スコア、グレイズの状態を管理
 *  - 被弾時の無敵時間処理
 *
 * 【弾幕シューティングの設計ポイント】
 *  - 高速移動と低速移動(Shift)を切り替えられる。低速時は当たり判定を見える化する。
 *  - 当たり判定(HITBOX_RADIUS)は見た目より小さい4px。これが「避ける楽しさ」の根幹。
 *  - パワーアップに応じて自機弾の本数が増える(3→5→7→9→12本)。
 *  - ボムは敵弾を全消ししつつ無敵になる「最後の保険」。
 */
public class Player extends Entity {
    public static final int FIELD_WIDTH = 384;
    public static final int FIELD_HEIGHT = 448;
    private static final double DEFAULT_NORMAL_SPEED = 5.0; // 通常移動速度のデフォルト
    private static final double DEFAULT_FOCUS_SPEED = 2.5;  // Shift押下中の速度のデフォルト
    private static final double DEFAULT_HITBOX_RADIUS = 4.0; // 当たり判定の半径のデフォルト

    // 選択されたキャラ。未設定なら従来のデフォルト挙動を使う。
    private PlayerCharacter character;

    private int lives = 3;   // 残機
    private int bombs = 3;   // ボム所持数
    private double power = 0; // パワー(0-125)。小P=0.5、大P=1.5で積み上げる
    private long score = 0;  // スコア(longにすることで桁あふれを防ぐ)
    private int graze = 0;   // 弾を掠った回数

    private int shootCooldown = 0;     // 射撃クールダウン(0以下なら撃てる)
    private int invincibleFrames = 0;  // 無敵時間カウンタ
    private int bombFrames = 0;        // ボム発動中の残り時間
    private boolean bombing = false;

    /** 自機オプション(アタッチメント)の状態。キャラ実装側が位置と発射タイミングを制御する。 */
    public static final class OptionState {
        /** 描画用の絶対座標(キャラ側で毎フレーム書き換える)。 */
        public double x, y;
        /** 自前のクールダウン(キャラ側で減算/再装填する)。 */
        public int fireCooldown;
        /** trueなら描画される。 */
        public boolean active;
    }
    public final OptionState leftOption = new OptionState();
    public final OptionState rightOption = new OptionState();

    /** ホーミング弾用：最寄りの敵(GamePanel側から毎フレーム更新)。-1なら無効。 */
    private double nearestEnemyX = -1;
    private double nearestEnemyY = -1;
    public void setNearestEnemy(double tx, double ty) {
        this.nearestEnemyX = tx;
        this.nearestEnemyY = ty;
    }
    public boolean hasNearestEnemy() { return nearestEnemyY >= 0; }
    public double getNearestEnemyX() { return nearestEnemyX; }
    public double getNearestEnemyY() { return nearestEnemyY; }

    // 撃った弾はここにためて、GamePanelが回収・全体リストへ移動する設計。
    // → 自機自身がゲーム全体の弾リストを持たなくて済むので、結合度が低くなる。
    private List<PlayerBullet> newBullets = new ArrayList<>();

    public Player(double x, double y) {
        super(x, y);
    }

    public Player(double x, double y, PlayerCharacter character) {
        super(x, y);
        this.character = character;
    }

    public void setCharacter(PlayerCharacter character) { this.character = character; }
    public PlayerCharacter getCharacter() { return character; }

    private double normalSpeed() {
        return character != null ? character.getNormalSpeed() : DEFAULT_NORMAL_SPEED;
    }
    private double focusSpeed() {
        return character != null ? character.getFocusSpeed() : DEFAULT_FOCUS_SPEED;
    }
    private double hitboxRadius() {
        return character != null ? character.getHitboxRadius() : DEFAULT_HITBOX_RADIUS;
    }

    /**
     * プレイヤーの1フレーム更新。
     * 入力に応じて移動・射撃・ボムを処理する。
     */
    public void update(InputHandler input) {
        // Shiftで低速モードへ
        boolean focus = input.isDown(KeyEvent.VK_SHIFT);
        double speed = focus ? focusSpeed() : normalSpeed();

        // 移動量を計算。矢印キーとWASDの両方に対応(操作性向上のため)
        double dx = 0, dy = 0;
        if (input.isDown(KeyEvent.VK_LEFT) || input.isDown(KeyEvent.VK_A)) dx -= speed;
        if (input.isDown(KeyEvent.VK_RIGHT) || input.isDown(KeyEvent.VK_D)) dx += speed;
        if (input.isDown(KeyEvent.VK_UP) || input.isDown(KeyEvent.VK_W)) dy -= speed;
        if (input.isDown(KeyEvent.VK_DOWN) || input.isDown(KeyEvent.VK_S)) dy += speed;

        // 斜め移動の補正。x,y両方足すと√2倍の速度になってしまうので
        // 1/√2 ≒ 0.7071 を掛けて速度を一定にする
        if (dx != 0 && dy != 0) {
            dx *= 0.7071;
            dy *= 0.7071;
        }

        // 移動結果が画面外に出ないようclampでフィールド内に制限
        double hr = hitboxRadius();
        x = MathUtil.clamp(x + dx, hr, FIELD_WIDTH - hr);
        y = MathUtil.clamp(y + dy, hr, FIELD_HEIGHT - hr);

        // Zキー押しっぱなしで連射。クールダウン管理で連射速度を一定に
        if (input.isDown(KeyEvent.VK_Z) && shootCooldown <= 0) {
            shoot(focus);
            shootCooldown = 5; // 5フレーム間は次の発射不可
        }
        if (shootCooldown > 0) shootCooldown--;

        // オプション(アタッチメント)の更新は毎フレーム呼ぶ。
        // キャラ実装側で active/位置/発射タイミングを決める。
        if (character != null) {
            character.updateOptions(this, focus, newBullets);
        }

        // Xキー押下でボム発動 (押しっぱなしで連発防止のためisJustPressedを使用)
        if (input.isJustPressed(KeyEvent.VK_X) && bombs > 0 && bombFrames <= 0) {
            bombs--;
            bombFrames = 180; // ボム持続3秒(60FPS * 3)
            bombing = true;
            // ボム中は無敵 (既存の無敵時間と比較して長い方を採用)
            invincibleFrames = Math.max(invincibleFrames, 180);
        }
        if (bombFrames > 0) bombFrames--;
        else bombing = false;

        if (invincibleFrames > 0) invincibleFrames--;
    }

    /**
     * 弾を発射する。
     * フォーカス(低速)時とノーマル時で弾の出方が違うのが弾幕シューティングの定番。
     * キャラが設定されていれば、そのキャラ固有のショットを使う。
     */
    private void shoot(boolean focus) {
        if (character != null) {
            newBullets.addAll(character.createShot(x, y, power, focus));
            return;
        }
        int dmg = 10;
        if (focus) {
            // フォーカス時：弾を集中させて当てやすくする(横並び5発)
            for (int i = -2; i <= 2; i++) {
                newBullets.add(new PlayerBullet(x + i * 2, y - 10, 0, -15, dmg));
            }
        } else {
            // 通常時：パワーに応じて広がる扇状の弾
            int ways = getWayCount();
            double baseSpeed = 12.0;
            double spread = Math.toRadians(8); // 弾の広がり角度(8度)
            for (int i = 0; i < ways; i++) {
                double angle = -Math.PI / 2; // -π/2は真上(Yの正は下向きのため)
                if (ways > 1) {
                    // 中心を基準に左右対称になるよう角度を計算
                    angle += spread * (i - (ways - 1) / 2.0);
                }
                double vx = Math.cos(angle) * baseSpeed;
                double vy = Math.sin(angle) * baseSpeed;
                newBullets.add(new PlayerBullet(x, y - 5, vx, vy, dmg));
            }
            // パワーに応じて補助弾(オプション)を追加
            addSideOptions(dmg);
        }
    }

    /**
     * 側面オプション弾。パワー段階ごとに弾が増えていく。
     *
     * 【ベクトル計算】
     *  vx,vyは速度ベクトル。0.17や0.98は方位ベクトル(三角関数の値)に相当し、
     *  ほぼ真上(-Y)に少し斜めの成分を加えて広がりを表現している。
     */
    private void addSideOptions(int dmg) {
        if (power < 100) return;
        double spd = 11.0;
        if (power >= 100) {
            // 左右から斜め上
            newBullets.add(new PlayerBullet(x - 15, y - 5, -spd * 0.17, -spd * 0.98, dmg));
            newBullets.add(new PlayerBullet(x + 15, y - 5,  spd * 0.17, -spd * 0.98, dmg));
        }
        if (power >= 200) {
            // さらに外側
            newBullets.add(new PlayerBullet(x - 25, y,     -spd * 0.34, -spd * 0.94, dmg));
            newBullets.add(new PlayerBullet(x + 25, y,      spd * 0.34, -spd * 0.94, dmg));
        }
        if (power >= 300) {
            // さらに外側
            newBullets.add(new PlayerBullet(x - 35, y,     -spd * 0.50, -spd * 0.87, dmg));
            newBullets.add(new PlayerBullet(x + 35, y,      spd * 0.50, -spd * 0.87, dmg));
        }
        if (power >= 400) {
            // 最大パワー：さらに2本追加
            newBullets.add(new PlayerBullet(x - 45, y + 5, -spd * 0.64, -spd * 0.77, dmg));
            newBullets.add(new PlayerBullet(x + 45, y + 5,  spd * 0.64, -spd * 0.77, dmg));
        }
    }

    /** パワーから弾の本数を決める。早見表的なルックアップ。 */
    private int getWayCount() {
        if (power < 100) return 3;
        if (power < 200) return 5;
        if (power < 300) return 7;
        if (power < 400) return 9;
        return 12;
    }

    public boolean isInvincible() { return invincibleFrames > 0; }
    public boolean isBombing() { return bombing; }

    /** 被弾フラグ(死亡演出を1回だけ走らせるためにGamePanelから消費する)。 */
    private boolean justDied = false;
    public boolean consumeJustDied() {
        if (!justDied) return false;
        justDied = false;
        return true;
    }

    /** ボム持ち数の初期値(被弾時もここまで戻す)。 */
    public static final int BOMB_RESET_COUNT = 3;

    /**
     * 被弾処理。残機-1、ボム数を初期値に戻し、パワーを半減する。
     * 無敵フレームを長めに付与してすぐ立て続けに死なないようにしている。
     */
    public void hit() {
        if (isInvincible()) return;
        lives--;
        power = power / 2;       // ペナルティ: パワー半減
        bombs = BOMB_RESET_COUNT; // 残機ごとにボム数を初期値に戻す
        invincibleFrames = 180;   // 3秒無敵
        bombing = false;
        bombFrames = 0;
        justDied = true;
    }

    /**
     * 撃った弾のリストを取り出して内部リストを空にする。
     * GamePanel側がフレームごとに呼んで、自機弾を全体管理リストへ移す。
     */
    public List<PlayerBullet> getAndClearNewBullets() {
        List<PlayerBullet> bullets = new ArrayList<>(newBullets);
        newBullets.clear();
        return bullets;
    }

    /** パワー上限。P=125 が最大(東方EoSDライク)。 */
    public static final double POWER_MAX = 125;
    public void addPower(double amount) { power = Math.min(POWER_MAX, power + amount); }
    public void addScore(long amount) { score += amount; }
    public void addGraze(int amount) { graze += amount; }

    /** 残機を増やす(1UP用)。上限を設けておくと無限増加を防げる。 */
    public void addLife(int amount) { lives = Math.min(9, lives + amount); }

    /** ボム所持数を増やす(スコアボーナス用)。 */
    public void addBomb(int amount) { bombs = Math.min(9, bombs + amount); }

    public int getLives() { return lives; }
    public int getBombs() { return bombs; }
    public double getPower() { return power; }
    public long getScore() { return score; }
    public int getGraze() { return graze; }
    public double getHitboxRadius() { return hitboxRadius(); }
    public boolean isAlive() { return lives > 0; }

    /** Entity要件のupdate()。プレイヤーはInputHandlerが必要なので別バージョンを使う。 */
    @Override
    public void update() { /* use update(InputHandler) instead */ }

    /**
     * 自機を三角形で描画。
     *
     * 【無敵中の点滅】
     *  invincibleFrames / 5 で除算し、偶数の時だけ描画する → 5フレームごとに点滅する。
     *  ボム中は点滅させずに常に表示する(視認性確保)。
     *
     * 【Path2Dとは】
     *  自由な多角形を描くためのクラス。moveToで開始点、lineToで線を引き、
     *  closePath()で閉じた図形にする。
     */
    @Override
    public void draw(Graphics2D g) {
        if (invincibleFrames > 0 && (invincibleFrames / 5) % 2 == 0 && !bombing) return;

        // キャラが設定されていればドット絵を描画、なければ従来の三角形
        if (character != null) {
            character.getInGameSprite().draw(g, x, y, 2);
            character.drawOptions(this, g);
            return;
        }

        Path2D triangle = new Path2D.Double();
        triangle.moveTo(x, y - 14);           // 上頂点
        triangle.lineTo(x - 10, y + 10);      // 左下
        triangle.lineTo(x + 10, y + 10);      // 右下
        triangle.closePath();

        g.setColor(new Color(180, 240, 255));
        g.fill(triangle);
        g.setColor(Color.WHITE);
        g.draw(triangle);

        // 内側に小さい三角を描いてデザイン性アップ
        Path2D inner = new Path2D.Double();
        inner.moveTo(x, y - 8);
        inner.lineTo(x - 5, y + 5);
        inner.lineTo(x + 5, y + 5);
        inner.closePath();
        g.setColor(new Color(0, 200, 255));
        g.fill(inner);
    }

    /**
     * 当たり判定の赤丸を表示。低速モード時のみ呼ばれる。
     * これがあるおかげで「自分のどこに当たるか」が見えて避けやすくなる。
     */
    public void drawHitbox(Graphics2D g) {
        double hr = hitboxRadius();
        g.setColor(new Color(255, 0, 0, 180));
        g.fill(new Ellipse2D.Double(x - hr, y - hr, hr * 2, hr * 2));
        g.setColor(Color.RED);
        g.draw(new Ellipse2D.Double(x - hr, y - hr, hr * 2, hr * 2));
    }

    /** ボム持続フレーム数(発動時にセットされる値)。 */
    private static final int BOMB_TOTAL_FRAMES = 180;

    /**
     * ボム発動中の演出(東方風スペルカード)。
     * 構成:
     *  1) 強い白フラッシュ(発動直後)
     *  2) 縦方向の光線(BEAM) — 中央から外へ展開
     *  3) キャラ画像 — 中央に大きく(リムグロー付き)
     *  4) 周囲のスパーク粒子
     *  5) 終了時のフェードアウト
     * 敵・弾が見えるよう、中盤以降はキャラ画像のアルファを下げる。
     */
    public void drawBomb(Graphics2D g, int fieldWidth, int fieldHeight) {
        if (!bombing) return;

        int elapsed = BOMB_TOTAL_FRAMES - bombFrames;
        int remaining = bombFrames;
        // 終了時フェード係数(残り30フレームから線形に0へ)
        float endFade = remaining < 30 ? remaining / 30f : 1f;

        // ===== 1) 白フラッシュ =====
        float flashAlpha = Math.max(0f, 1f - elapsed / 14f);
        if (flashAlpha > 0f) {
            g.setColor(new Color(1f, 1f, 1f, flashAlpha * 0.85f));
            g.fillRect(0, 0, fieldWidth, fieldHeight);
        }

        // ===== 2) 縦方向の光線 =====
        drawBombBeams(g, fieldWidth, fieldHeight, elapsed, endFade);

        // ===== 3) キャラ画像 =====
        BufferedImage img = BombImageRegistry.imageFor(character);
        if (img != null) {
            drawBombCharacterImage(g, fieldWidth, fieldHeight, img, elapsed, remaining, endFade);
        } else {
            // 画像なし時は中央に光球フォールバック
            float a = Math.min(1.0f, remaining / 30.0f);
            g.setColor(new Color(1.0f, 1.0f, 1.0f, a * 0.4f));
            g.fillRect(0, 0, fieldWidth, fieldHeight);
        }

        // ===== 4) スパーク粒子 =====
        drawBombSparkles(g, fieldWidth, fieldHeight, elapsed, endFade);

        // ===== 5) 「SPELL CARD」風のテキスト(発動直後だけ) =====
        if (elapsed < 60) {
            float textAlpha = elapsed < 30 ? elapsed / 30f : Math.max(0f, 1f - (elapsed - 30) / 30f);
            g.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 28));
            String label = "～ BOMB! ～";
            FontMetrics fm = g.getFontMetrics();
            int tx = (fieldWidth - fm.stringWidth(label)) / 2;
            int ty = 60;
            g.setColor(new Color(0f, 0f, 0f, textAlpha * 0.7f));
            g.drawString(label, tx + 2, ty + 2);
            g.setColor(new Color(1f, 0.95f, 0.5f, textAlpha));
            g.drawString(label, tx, ty);
        }
    }

    /** 縦方向のスペルライン(複数本)。中央から外へ展開し、時間で薄くなる。 */
    private void drawBombBeams(Graphics2D g, int fieldWidth, int fieldHeight, int elapsed, float endFade) {
        Composite oldComp = g.getComposite();
        // 中央から左右に広がる縦の光線群
        int beams = 14;
        float spread = Math.min(1f, elapsed / 25f);  // 0→1で広がる
        int cx = fieldWidth / 2;
        float baseAlpha = 0.55f * endFade;
        for (int i = 0; i < beams; i++) {
            float t = (i + 1) / (float) beams;
            int offset = (int) (t * fieldWidth * 0.55f * spread);
            int width = 6 + (int) ((1 - t) * 14);
            float beamAlpha = baseAlpha * (1f - t * 0.5f);
            // 左右対称に描画
            for (int side = -1; side <= 1; side += 2) {
                int bx = cx + offset * side;
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, beamAlpha));
                // 中心ライン (明るい青白)
                g.setColor(new Color(220, 240, 255));
                g.fillRect(bx - width / 4, 0, Math.max(1, width / 2), fieldHeight);
                // 周辺のソフトな広がり
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, beamAlpha * 0.35f));
                g.setColor(new Color(140, 200, 255));
                g.fillRect(bx - width / 2, 0, width, fieldHeight);
            }
        }
        // 中央太線
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f * endFade));
        g.setColor(new Color(255, 255, 255));
        g.fillRect(cx - 2, 0, 4, fieldHeight);
        g.setComposite(oldComp);
    }

    /** 中央のキャラ画像。リムグロー＋少しの揺れを加える。 */
    private void drawBombCharacterImage(Graphics2D g, int fieldWidth, int fieldHeight,
                                        BufferedImage img, int elapsed, int remaining, float endFade) {
        // 中盤の画像アルファ
        float baseAlpha;
        if (elapsed < 30) baseAlpha = 0.95f;
        else if (elapsed < 60) baseAlpha = 0.95f - (elapsed - 30) / 30f * 0.35f; // 0.95→0.6
        else baseAlpha = 0.6f;
        float imgAlpha = baseAlpha * endFade;

        // 画像をフィールド高さの90%程度に収まるサイズに(縦長前提)
        double maxH = fieldHeight * 0.92;
        double maxW = fieldWidth * 0.95;
        double scale = Math.min(maxW / img.getWidth(), maxH / img.getHeight());
        // 発動直後だけ少し拡大→落ち着く(zoom演出)
        double zoom = 1.0 + 0.08 * Math.max(0, 1 - elapsed / 20.0);
        int drawW = (int) (img.getWidth() * scale * zoom);
        int drawH = (int) (img.getHeight() * scale * zoom);
        // 少し縦に揺らす(衝撃感)
        int shakeY = elapsed < 15 ? (int) (Math.sin(elapsed * 1.5) * 4) : 0;
        int drawX = (fieldWidth - drawW) / 2;
        int drawY = (fieldHeight - drawH) / 2 + shakeY;

        Composite oldComp = g.getComposite();

        // リムグロー(画像を少し大きく明るい色で重ねてアウター)
        for (int rim = 3; rim >= 1; rim--) {
            float rimAlpha = imgAlpha * 0.18f * rim;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rimAlpha));
            int rw = drawW + rim * 10;
            int rh = drawH + rim * 10;
            int rx = (fieldWidth - rw) / 2;
            int ry = drawY - rim * 5;
            // 明るい色で薄く塗る四角(画像形にぴったり合わせるのは難しいので近似)
            g.setColor(new Color(255, 240, 200));
            g.fillRoundRect(rx, ry, rw, rh, 18, 18);
        }

        // 本体
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, imgAlpha));
        g.drawImage(img, drawX, drawY, drawW, drawH, null);

        // 上から明るいオーバーレイ(発動直後のみ)で「光に包まれた」感
        if (elapsed < 25) {
            float overlay = (1f - elapsed / 25f) * 0.4f * endFade;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, overlay));
            g.setColor(new Color(255, 250, 220));
            g.fillRect(drawX, drawY, drawW, drawH);
        }
        g.setComposite(oldComp);
    }

    /** 周囲のスパーク粒子。経過時間でアニメーション。 */
    private void drawBombSparkles(Graphics2D g, int fieldWidth, int fieldHeight, int elapsed, float endFade) {
        Composite oldComp = g.getComposite();
        // 決定論的な疑似乱数(seedに経過を加味)で位置を生成
        java.util.Random r = new java.util.Random(91827364L);
        int count = 60;
        for (int i = 0; i < count; i++) {
            // 個々の粒子のサイクル(0..1)
            int lifeOffset = (i * 7) % 60;
            int life = (elapsed + lifeOffset) % 60;
            float t = life / 60f;

            // 出現位置(中央付近を避ける)
            float ax = r.nextFloat();
            float ay = r.nextFloat();
            float drift = r.nextFloat() * 0.5f + 0.5f;
            int px = (int) (ax * fieldWidth);
            int py = (int) (ay * fieldHeight + drift * t * 20f) % fieldHeight;

            // ライフカーブ: 0→ピーク→0
            float alphaCurve = (float) Math.sin(t * Math.PI);
            float sparkleAlpha = alphaCurve * 0.9f * endFade;
            if (sparkleAlpha <= 0.02f) continue;

            int size = 2 + (int) (alphaCurve * 3);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sparkleAlpha));
            // 中心の明るい点
            g.setColor(new Color(255, 255, 240));
            g.fillOval(px - size / 2, py - size / 2, size, size);
            // 4方向の小さなフレア(十字)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sparkleAlpha * 0.6f));
            g.setColor(new Color(200, 220, 255));
            g.fillRect(px - size, py, size * 2, 1);
            g.fillRect(px, py - size, 1, size * 2);
        }
        g.setComposite(oldComp);
    }
}
