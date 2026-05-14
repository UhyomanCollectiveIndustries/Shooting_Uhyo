package com.shootinguhyo;

import com.shootinguhyo.effect.Particle;
import com.shootinguhyo.effect.SpellCardEffect;
import com.shootinguhyo.entity.*;
import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.entity.bullet.PlayerBullet;
import com.shootinguhyo.render.HUD;
import com.shootinguhyo.stage.Stage1;
import com.shootinguhyo.util.MathUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * GamePanel：ゲームの「心臓部」となるクラス。
 *
 * 【役割】
 *  - 画面に絵を描く場所（JPanelを継承）
 *  - ゲームループ（毎フレームの更新と描画を繰り返す処理）を回す
 *  - プレイヤー、敵、弾、アイテム、エフェクト等すべてを管理する
 *
 * 【ゲーム作成の全体像（このクラスで使っている関数）】
 *  ・startGame() … ゲームスレッドを起動する
 *  ・run()       … メインループ。1秒に60回(60FPS)、update()→repaint()を呼ぶ
 *  ・update()    … 「動かす」処理。状態(GameState)に応じて分岐
 *  ・render()    … 「描く」処理。バックバッファに絵を描いて画面に転写
 *
 * 【なぜJPanelを継承するか】
 *  JPanelはSwingで自由に絵を描ける四角形領域のクラス。
 *  paintComponent()をオーバーライドすると好きな絵を描ける。
 *
 * 【なぜRunnableを実装するか】
 *  ゲームループを別スレッドで動かすため。Threadのコンストラクタにこのオブジェクトを渡すと
 *  run()メソッドが新しいスレッドで動き始める。
 *
 * 【ダブルバッファリング】
 *  画面に直接描くとチカチカ(ちらつき)する。そこで「裏紙(backBuffer)」に絵を描き終えてから
 *  一気に画面に貼り付ける方式を使い、なめらかな描画を実現している。
 */
public class GamePanel extends JPanel implements Runnable {
    // 弾幕シューティングの「プレイフィールド(弾が飛ぶ場所)」の幅と高さ
    public static final int FIELD_WIDTH = 384;
    public static final int FIELD_HEIGHT = 448;
    // フィールド + 右側のHUD(スコア表示エリア)を含めたパネル全体のサイズ
    public static final int PANEL_WIDTH = 576;
    public static final int PANEL_HEIGHT = 448;
    // FPS(Frames Per Second)：1秒間に何回更新・描画するか
    private static final int FPS = 60;

    // バックバッファ：絵を一時的に描いておく裏紙
    private BufferedImage backBuffer;
    private Graphics2D backG;       // 裏紙へ絵を描くためのペン
    private Thread gameThread;      // ゲームループを動かすスレッド
    private InputHandler input;     // キーボード入力を扱うクラス

    // 今のゲーム状態(タイトル中、プレイ中など)を保持。状態によって動作が変わる
    private GameState gameState = GameState.TITLE;
    private GameState prevGameState = GameState.TITLE; // ポーズから復帰するときの状態保存用

    // ゲーム中に登場するオブジェクトたち
    private Player player;                                          // 自機
    private List<Enemy> enemies = new ArrayList<>();                // 通常敵
    private List<FastEnemy> fastEnemies = new ArrayList<>();        // 高速敵
    private List<PlayerBullet> playerBullets = new ArrayList<>();   // 自機弾
    private List<EnemyBullet> enemyBullets = new ArrayList<>();     // 敵弾
    private List<Item> items = new ArrayList<>();                   // アイテム
    private List<Particle> particles = new ArrayList<>();           // パーティクル(爆発演出)
    private Boss boss;                                              // ボス

    // ステージ進行管理
    private Stage1 stage1;          // ステージ1のオブジェクト
    private int stageFrame = 0;     // ステージ開始からの経過フレーム数
    private boolean bossSpawned = false; // ボスを出したかどうか

    private HUD hud;                          // 画面右側のスコア表示
    private SpellCardEffect spellCardEffect;  // スペルカード(ボスの必殺技)演出

    // 背景の星 (奥行きを感じさせるための演出)
    private double[] starX = new double[150];
    private double[] starY = new double[150];
    private double[] starSpeed = new double[150];
    private int[] starBrightness = new int[150];

    private Random rand = new Random(); // 乱数生成器

    private int titleFrame = 0; // タイトル画面表示用のフレームカウンタ(点滅などに使う)
    private int endFrame = 0;   // ゲームオーバー/クリア画面用のフレームカウンタ

    private Boss.Phase lastBossPhase = null; // 前フレームのボス段階(変化検知用)

    /**
     * コンストラクタ：パネルの初期設定を行う。
     */
    public GamePanel() {
        // JPanelのサイズを指定。pack()で利用される
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);

        // キー入力を受け取れるように準備
        input = new InputHandler();
        addKeyListener(input);
        setFocusable(true); // フォーカスを持てないとキー入力を受け取れない

        // 裏紙を作成。TYPE_INT_RGBは1ピクセル24ビット(RGB)で表現するモード
        backBuffer = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        backG = backBuffer.createGraphics();
        // アンチエイリアシング有効化(図形の輪郭をなめらかに描く)
        backG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        hud = new HUD();
        spellCardEffect = new SpellCardEffect();
        initStars(); // 星の初期位置をランダム配置
    }

    /** 背景の星の初期位置・速度・明るさをランダムに設定。 */
    private void initStars() {
        for (int i = 0; i < starX.length; i++) {
            starX[i] = rand.nextInt(FIELD_WIDTH);
            starY[i] = rand.nextInt(FIELD_HEIGHT);
            starSpeed[i] = rand.nextDouble() * 1.5 + 0.3; // 速度に幅をつけて奥行き表現
            starBrightness[i] = rand.nextInt(180) + 60;
        }
    }

    /** 星を下方向に流す。画面下端を越えたら上に戻して再利用。 */
    private void updateStars() {
        for (int i = 0; i < starY.length; i++) {
            starY[i] += starSpeed[i];
            if (starY[i] > FIELD_HEIGHT) {
                starY[i] = 0;
                starX[i] = rand.nextInt(FIELD_WIDTH);
            }
        }
    }

    /** 星を描画。速い星は2x2、遅い星は1x1ピクセルにして遠近感を出す。 */
    private void drawStars(Graphics2D g) {
        for (int i = 0; i < starX.length; i++) {
            int b = starBrightness[i];
            g.setColor(new Color(b, b, b));
            int size = starSpeed[i] > 1.2 ? 2 : 1;
            g.fillRect((int) starX[i], (int) starY[i], size, size);
        }
    }

    /**
     * ゲームスレッドを開始する。
     * setDaemon(true)にしているのは、メインウィンドウが閉じたら一緒に終了させるため。
     */
    public void startGame() {
        gameThread = new Thread(this);
        gameThread.setDaemon(true);
        gameThread.start();
    }

    /**
     * メインループ。Threadから自動で呼ばれる。
     *
     * 【処理内容】
     *  常にwhile(true)で回り続け、前回更新からの経過時間を見ながら
     *  1/60秒に1回update()とrepaint()を呼ぶ「固定フレームレート」を実現。
     *
     * 【なぜThread.sleep(1)？】
     *  ただループするとCPUを100%食い続けて他のアプリが遅くなる。
     *  少し眠らせて余計な処理を減らす一般的なテクニック。
     */
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long nsPerFrame = 1_000_000_000L / FPS; // 1フレーム分のナノ秒

        while (true) {
            long now = System.nanoTime();
            long elapsed = now - lastTime;
            if (elapsed >= nsPerFrame) {
                lastTime = now;
                update();    // 状態を1フレーム分進める
                repaint();   // 画面再描画を依頼(paintComponentが呼ばれる)
            } else {
                try {
                    Thread.sleep(1); // CPU負荷低減
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * 1フレーム分の状態更新。
     * 状態(GameState)ごとに別々の更新メソッドを呼ぶ「状態パターン」風の実装。
     */
    private void update() {
        switch (gameState) {
            case TITLE -> updateTitle();
            case PLAYING -> updatePlaying();
            case BOSS_FIGHT -> updateBossFight();
            case GAME_OVER -> updateGameOver();
            case CLEAR -> updateClear();
            case PAUSED -> updatePaused();
        }
        input.clearJustPressed(); // 「初めて押された」情報はフレーム末でクリア
    }

    /** タイトル画面：Enterで開始。 */
    private void updateTitle() {
        titleFrame++;
        updateStars();
        if (input.isJustPressed(KeyEvent.VK_ENTER)) {
            initGame();
            gameState = GameState.PLAYING;
        }
    }

    /** ゲームを最初の状態にリセット。タイトル→プレイ移行時に呼ぶ。 */
    private void initGame() {
        player = new Player(192, 380); // 画面下中央あたりに配置
        enemies.clear();
        fastEnemies.clear();
        playerBullets.clear();
        enemyBullets.clear();
        items.clear();
        particles.clear();
        boss = null;
        stage1 = new Stage1();
        stageFrame = 0;
        bossSpawned = false;
        lastBossPhase = null;
        spellCardEffect.deactivate();
        endFrame = 0;
    }

    /**
     * 通常プレイ中の更新。
     * 自機・敵・弾・アイテム・パーティクルすべてを動かす。
     */
    private void updatePlaying() {
        updateStars();
        stageFrame++;

        player.update(input); // 自機の移動・射撃処理

        // Escでポーズ
        if (input.isJustPressed(KeyEvent.VK_ESCAPE)) {
            prevGameState = gameState;
            gameState = GameState.PAUSED;
            return;
        }

        // ステージ進行(タイミングに応じて敵を出現させる)
        stage1.update(stageFrame, enemies, fastEnemies);

        // 高速敵にプレイヤー位置を教える(自機狙い弾用)
        for (FastEnemy fe : fastEnemies) {
            fe.setPlayerPosition(player.x, player.y);
        }

        updateEnemies();      // 敵の動きと撃破判定
        updateBullets();      // 弾の動きと当たり判定
        updateItems();        // アイテム取得処理
        updateParticles();    // 爆発エフェクト
        spellCardEffect.update();

        // ボス出現条件：ステージ終盤かつ雑魚敵がいない
        if (stage1.isBossTime(stageFrame) && !bossSpawned && enemies.isEmpty() && fastEnemies.isEmpty()) {
            bossSpawned = true;
            boss = new Boss(192, 80);
            gameState = GameState.BOSS_FIGHT;
        }

        hud.update(player.getScore());

        // 残機0でゲームオーバーへ
        if (!player.isAlive()) {
            gameState = GameState.GAME_OVER;
            endFrame = 0;
        }
    }

    /**
     * ボス戦中の更新。
     * フェーズ移行を検出してスペルカード演出やボーナス加算を行うのが特徴。
     */
    private void updateBossFight() {
        updateStars();
        stageFrame++;

        player.update(input);

        if (input.isJustPressed(KeyEvent.VK_ESCAPE)) {
            prevGameState = gameState;
            gameState = GameState.PAUSED;
            return;
        }

        if (boss != null) {
            boss.setPlayerPosition(player.x, player.y);
            boss.update();

            // フェーズが変わった瞬間を検知 → 演出と特典を発動
            Boss.Phase currentPhase = boss.getPhase();
            if (currentPhase != lastBossPhase) {
                lastBossPhase = currentPhase;
                if (currentPhase == Boss.Phase.SPELL1 || currentPhase == Boss.Phase.SPELL2
                        || currentPhase == Boss.Phase.SPELL3) {
                    // スペルカードに入ったタイミングで画面演出を起動
                    spellCardEffect.activate(boss.getSpellColor(), boss.getSpellName());
                    player.addScore(10000);
                    enemyBullets.clear(); // 直前の弾をクリア(被弾防止＆視認性UP)
                } else if (currentPhase == Boss.Phase.DEFEATED) {
                    spellCardEffect.deactivate();
                    player.addScore(10000);
                    enemyBullets.clear();
                    createExplosion((int) boss.x, (int) boss.y, 30, Color.YELLOW);
                }
            }

            // ボスが撃った弾を全体の敵弾リストに合流
            List<EnemyBullet> bossBullets = boss.getAndClearNewBullets();
            enemyBullets.addAll(bossBullets);

            if (boss.isDefeated()) {
                gameState = GameState.CLEAR;
                endFrame = 0;
                return;
            }
        }

        updateBullets();
        updateItems();
        updateParticles();
        spellCardEffect.update();

        hud.update(player.getScore());

        if (!player.isAlive()) {
            gameState = GameState.GAME_OVER;
            endFrame = 0;
        }
    }

    /** ポーズ中：EscかEnterで復帰。 */
    private void updatePaused() {
        if (input.isJustPressed(KeyEvent.VK_ESCAPE) || input.isJustPressed(KeyEvent.VK_ENTER)) {
            gameState = prevGameState;
        }
    }

    /**
     * ゲームオーバー画面：120フレーム経過後にEnterでタイトルへ。
     * 待ち時間を設けるのは、誤入力で即タイトルに戻るのを防ぐため。
     */
    private void updateGameOver() {
        endFrame++;
        updateStars();
        if (endFrame > 120 && input.isJustPressed(KeyEvent.VK_ENTER)) {
            gameState = GameState.TITLE;
        }
    }

    /** クリア画面：同上。 */
    private void updateClear() {
        endFrame++;
        updateStars();
        if (endFrame > 120 && input.isJustPressed(KeyEvent.VK_ENTER)) {
            gameState = GameState.TITLE;
        }
    }

    /**
     * 敵の更新と撃破時の処理。
     * Iteratorを使うのは、リストをループ中に安全に要素を削除するため。
     * 通常のfor文だとremove時にConcurrentModificationExceptionが出てしまう。
     */
    private void updateEnemies() {
        Iterator<Enemy> ei = enemies.iterator();
        while (ei.hasNext()) {
            Enemy e = ei.next();
            e.update();
            enemyBullets.addAll(e.getAndClearNewBullets()); // 敵が撃った弾を取り込む

            if (e.isDefeated()) {
                // 撃破演出 + アイテムドロップ
                createExplosion((int) e.x, (int) e.y, 12, new Color(180, 100, 255));
                items.add(new Item(e.x, e.y, Item.ItemType.POWER));
                items.add(new Item(e.x + 8, e.y, Item.ItemType.POINT));
                player.addScore(e.getScore());
            }

            // 画面外に出る等でactive=falseになった敵を除去
            if (!e.active) ei.remove();
        }

        // 高速敵も同様の処理
        Iterator<FastEnemy> fi = fastEnemies.iterator();
        while (fi.hasNext()) {
            FastEnemy fe = fi.next();
            fe.update();
            enemyBullets.addAll(fe.getAndClearNewBullets());

            if (fe.isDefeated()) {
                createExplosion((int) fe.x, (int) fe.y, 10, new Color(0, 200, 200));
                items.add(new Item(fe.x, fe.y, Item.ItemType.POWER));
                player.addScore(fe.getScore());
            }

            if (!fe.active) fi.remove();
        }
    }

    /**
     * 自機弾・敵弾の移動と当たり判定。
     *
     * 【当たり判定の考え方】
     *  - 円同士の距離を求めて、半径の合計より小さければ「当たった」と判定する
     *  - 弾幕シューティングでは自機の当たり判定を非常に小さくして「避ける楽しさ」を出す
     *  - 「グレイズ」：弾が掠った時(当たってないけど近い)にボーナスを与える設計
     */
    private void updateBullets() {
        // 自機が新しく撃った弾を全体リストに合流
        playerBullets.addAll(player.getAndClearNewBullets());

        Iterator<PlayerBullet> pbi = playerBullets.iterator();
        while (pbi.hasNext()) {
            PlayerBullet pb = pbi.next();
            pb.update();
            if (!pb.active) { pbi.remove(); continue; }

            boolean hit = false;
            // 通常敵への当たり判定
            for (Enemy e : enemies) {
                if (e.active && MathUtil.distance(pb.x, pb.y, e.x, e.y) < 12) {
                    e.takeDamage(pb.getDamage());
                    hit = true;
                    break;
                }
            }
            // 高速敵への当たり判定
            if (!hit) {
                for (FastEnemy fe : fastEnemies) {
                    if (fe.active && MathUtil.distance(pb.x, pb.y, fe.x, fe.y) < 8) {
                        fe.takeDamage(pb.getDamage());
                        hit = true;
                        break;
                    }
                }
            }
            // ボスへの当たり判定
            if (!hit && boss != null && !boss.isDefeated()) {
                if (MathUtil.distance(pb.x, pb.y, boss.x, boss.y) < 30) {
                    boss.takeDamage(pb.getDamage());
                    player.addScore(50); // 当てるだけでボーナス
                    hit = true;
                }
            }
            if (hit) pbi.remove(); // 当たった弾は消える
        }

        // 敵弾と自機の当たり判定
        Iterator<EnemyBullet> ebi = enemyBullets.iterator();
        while (ebi.hasNext()) {
            EnemyBullet eb = ebi.next();
            eb.update();
            if (!eb.active) { ebi.remove(); continue; }

            // ボム中は弾を消去(無敵+全消し)
            if (player.isBombing()) { ebi.remove(); continue; }

            double dist = MathUtil.distance(eb.x, eb.y, player.x, player.y);
            // グレイズ：当たってないけど16px以内に来た弾はカスり扱い
            if (dist < 16 && dist > player.getHitboxRadius() + eb.getRadius()) {
                player.addGraze(1);
                player.addScore(100);
            }

            // 実際の被弾判定
            if (!player.isInvincible() && dist < player.getHitboxRadius() + eb.getRadius()) {
                player.hit();
                ebi.remove();
                createExplosion((int) player.x, (int) player.y, 15, Color.WHITE);
            }
        }
    }

    /** アイテムの更新と取得判定。アイテムは自機に当たると効果発動する。 */
    private void updateItems() {
        Iterator<Item> ii = items.iterator();
        while (ii.hasNext()) {
            Item item = ii.next();
            item.update();
            if (!item.active) { ii.remove(); continue; }

            if (MathUtil.distance(item.x, item.y, player.x, player.y) < player.getHitboxRadius() + item.getRadius()) {
                if (item.getType() == Item.ItemType.POWER) {
                    player.addPower(20); // パワーアップ：弾の本数増加
                } else {
                    player.addScore(100); // 得点アイテム
                }
                ii.remove();
            }
        }
    }

    /** パーティクル(爆発の破片)の更新。寿命が来たものはリストから削除。 */
    private void updateParticles() {
        Iterator<Particle> pi = particles.iterator();
        while (pi.hasNext()) {
            Particle p = pi.next();
            p.update();
            if (!p.active) pi.remove();
        }
    }

    /**
     * 爆発エフェクトを生成。
     * 指定座標の周りにcount個のパーティクルを撒く。
     *
     * 【色のランダム化】
     *  ベース色±30の範囲で色を揺らすことで、火花のような豊かな見栄えに。
     *  Math.max/Math.minで0-255の範囲に収まるようにクランプ(範囲制限)している。
     */
    private void createExplosion(int cx, int cy, int count, Color color) {
        for (int i = 0; i < count; i++) {
            Color c = new Color(
                    Math.max(0, Math.min(255, color.getRed() + rand.nextInt(60) - 30)),
                    Math.max(0, Math.min(255, color.getGreen() + rand.nextInt(60) - 30)),
                    Math.max(0, Math.min(255, color.getBlue() + rand.nextInt(60) - 30))
            );
            particles.add(new Particle(cx, cy, c));
        }
    }

    /**
     * paintComponentはSwingが画面を再描画する必要があるときに自動で呼ばれる。
     * repaint()を呼ぶと、内部的にこのメソッドが呼ばれる。
     *
     * 【ダブルバッファ転写】
     *  render()でbackBufferに絵を完成させてから、一気にgへ転写する。
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        render();
        g.drawImage(backBuffer, 0, 0, null);
    }

    /** バックバッファに今のフレームの絵を描く。状態によって描くものが違う。 */
    private void render() {
        Graphics2D g = backG;

        // 一旦黒で塗りつぶしてから描き始める(前フレームの絵を消す)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        switch (gameState) {
            case TITLE -> renderTitle(g);
            case PLAYING, BOSS_FIGHT -> renderGame(g);
            case PAUSED -> { renderGame(g); renderPause(g); }
            case GAME_OVER -> { renderGame(g); renderGameOver(g); }
            case CLEAR -> { renderGame(g); renderClear(g); }
        }
    }

    /** タイトル画面を描く。ENTER誘導文字は点滅させてアピール。 */
    private void renderTitle(Graphics2D g) {
        g.setColor(new Color(5, 0, 20));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawStars(g);

        // タイトル文字
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.setColor(new Color(255, 200, 255));
        String title = "Shooting Uhyo";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (PANEL_WIDTH - fm.stringWidth(title)) / 2, 150);

        // サブタイトル(現在は空)
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(new Color(200, 150, 255));
        String sub = "";
        fm = g.getFontMetrics();
        g.drawString(sub, (PANEL_WIDTH - fm.stringWidth(sub)) / 2, 185);

        // 30フレーム単位で点滅 (フレーム数を30で割って偶数か奇数かで判定)
        if ((titleFrame / 30) % 2 == 0) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(Color.WHITE);
            String press = "Press ENTER to Start";
            fm = g.getFontMetrics();
            g.drawString(press, (PANEL_WIDTH - fm.stringWidth(press)) / 2, 280);
        }

        // 操作説明
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(new Color(150, 150, 200));
        String[] controls = {
            "Arrow/WASD: Move", "Z: Shoot  X: Bomb",
            "Shift: Focus  Esc: Pause"
        };
        int cy = 330;
        for (String ctrl : controls) {
            fm = g.getFontMetrics();
            g.drawString(ctrl, (PANEL_WIDTH - fm.stringWidth(ctrl)) / 2, cy);
            cy += 18;
        }
    }

    /**
     * ゲーム本編の描画。
     *
     * 【描画順の意味】
     *  下に描いたものほど上に重なる(後勝ち)。
     *  そのためアイテム→敵→ボス→敵弾→自機弾→自機…の順に重ねている。
     *  特に「敵弾より自機弾を上」「自機より敵弾を下」にすることで見やすさを確保。
     */
    private void renderGame(Graphics2D g) {
        // プレイフィールド背景
        g.setColor(new Color(5, 0, 20));
        g.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        drawStars(g);

        // スペルカード(必殺技)発動中の色オーバーレイ
        if (spellCardEffect.isActive()) {
            spellCardEffect.draw(g, FIELD_WIDTH, FIELD_HEIGHT);
        }

        for (Item item : items) item.draw(g);

        for (Enemy e : enemies) if (e.active) e.draw(g);
        for (FastEnemy fe : fastEnemies) if (fe.active) fe.draw(g);

        if (boss != null && !boss.isDefeated()) boss.draw(g);

        for (EnemyBullet eb : enemyBullets) if (eb.active) eb.draw(g);

        for (PlayerBullet pb : playerBullets) if (pb.active) pb.draw(g);

        if (player != null) {
            player.draw(g);
            // 低速移動(Shift押下中)のとき当たり判定を表示
            if (input.isDown(KeyEvent.VK_SHIFT)) {
                player.drawHitbox(g);
            }
            player.drawBomb(g, FIELD_WIDTH, FIELD_HEIGHT);
        }

        // パーティクルは最後に描いて一番上に被せる
        for (Particle p : particles) if (p.active) p.draw(g);

        // フィールドの枠線
        g.setColor(new Color(80, 60, 120));
        g.drawRect(0, 0, FIELD_WIDTH - 1, FIELD_HEIGHT - 1);

        // 右側の情報表示
        hud.draw(g, player, boss);
    }

    /** ポーズ画面：半透明黒で薄暗くしてからPAUSED文字を出す。 */
    private void renderPause(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150)); // 第4引数はアルファ(透明度)
        g.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        String msg = "PAUSED";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (FIELD_WIDTH - fm.stringWidth(msg)) / 2, FIELD_HEIGHT / 2);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        String sub2 = "Press ESC or ENTER to resume";
        fm = g.getFontMetrics();
        g.drawString(sub2, (FIELD_WIDTH - fm.stringWidth(sub2)) / 2, FIELD_HEIGHT / 2 + 30);
    }

    /** ゲームオーバー画面。 */
    private void renderGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        g.setFont(new Font("SansSerif", Font.BOLD, 32));
        g.setColor(new Color(255, 80, 80));
        String msg = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (FIELD_WIDTH - fm.stringWidth(msg)) / 2, FIELD_HEIGHT / 2 - 20);
        if (endFrame > 120) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(Color.WHITE);
            String sub2 = "Press ENTER to return to title";
            fm = g.getFontMetrics();
            g.drawString(sub2, (FIELD_WIDTH - fm.stringWidth(sub2)) / 2, FIELD_HEIGHT / 2 + 20);
        }
    }

    /** クリア画面。スコアも一緒に表示。 */
    private void renderClear(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.setColor(new Color(255, 220, 100));
        String msg = "STAGE CLEAR!";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (FIELD_WIDTH - fm.stringWidth(msg)) / 2, FIELD_HEIGHT / 2 - 30);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(Color.WHITE);
        String score = "Score: " + player.getScore();
        fm = g.getFontMetrics();
        g.drawString(score, (FIELD_WIDTH - fm.stringWidth(score)) / 2, FIELD_HEIGHT / 2 + 10);
        if (endFrame > 120) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(new Color(200, 200, 255));
            String sub2 = "Press ENTER to return to title";
            fm = g.getFontMetrics();
            g.drawString(sub2, (FIELD_WIDTH - fm.stringWidth(sub2)) / 2, FIELD_HEIGHT / 2 + 40);
        }
    }
}
