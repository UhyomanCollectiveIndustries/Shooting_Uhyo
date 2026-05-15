package com.shootinguhyo.entity;

import com.shootinguhyo.InputHandler;
import com.shootinguhyo.character.PlayerCharacter;
import com.shootinguhyo.entity.bullet.PlayerBullet;
import com.shootinguhyo.util.MathUtil;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
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
    private int power = 0;   // パワー(0-400)。100,200,300,400で弾数が変わる
    private long score = 0;  // スコア(longにすることで桁あふれを防ぐ)
    private int graze = 0;   // 弾を掠った回数

    private int shootCooldown = 0;     // 射撃クールダウン(0以下なら撃てる)
    private int invincibleFrames = 0;  // 無敵時間カウンタ
    private int bombFrames = 0;        // ボム発動中の残り時間
    private boolean bombing = false;

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

    /**
     * 被弾処理。残機-1とパワー半減。
     * 無敵フレームを長めに付与してすぐ立て続けに死なないようにしている。
     */
    public void hit() {
        if (isInvincible()) return;
        lives--;
        power = power / 2;     // ペナルティ：パワー半減
        invincibleFrames = 180; // 3秒無敵
        bombing = false;
        bombFrames = 0;
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

    public void addPower(int amount) { power = Math.min(400, power + amount); } // 上限400で頭打ち
    public void addScore(long amount) { score += amount; }
    public void addGraze(int amount) { graze += amount; }

    /** 残機を増やす(1UP用)。上限を設けておくと無限増加を防げる。 */
    public void addLife(int amount) { lives = Math.min(9, lives + amount); }

    /** ボム所持数を増やす(スコアボーナス用)。 */
    public void addBomb(int amount) { bombs = Math.min(9, bombs + amount); }

    public int getLives() { return lives; }
    public int getBombs() { return bombs; }
    public int getPower() { return power; }
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

    /**
     * ボム発動中の白いフラッシュ演出。
     * ボム残り時間に応じてアルファ(透明度)を変えてフェードアウトさせる。
     */
    public void drawBomb(Graphics2D g, int fieldWidth, int fieldHeight) {
        if (!bombing) return;
        float alpha = Math.min(1.0f, bombFrames / 30.0f);
        g.setColor(new Color(1.0f, 1.0f, 1.0f, alpha * 0.5f));
        g.fillRect(0, 0, fieldWidth, fieldHeight);
    }
}
