package com.shootinguhyo;

import com.shootinguhyo.audio.AudioManager;
import com.shootinguhyo.character.CharacterRegistry;
import com.shootinguhyo.character.PlayerCharacter;
import com.shootinguhyo.config.Difficulty;
import com.shootinguhyo.config.GameConfig;
import com.shootinguhyo.config.GameOptions;
import com.shootinguhyo.dialog.DialogSamples;
import com.shootinguhyo.dialog.DialogScene;
import com.shootinguhyo.effect.BossDefeatEffect;
import com.shootinguhyo.effect.Particle;
import com.shootinguhyo.effect.SpellCardEffect;
import com.shootinguhyo.entity.*;
import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.entity.bullet.PlayerBullet;
import com.shootinguhyo.render.HUD;
import com.shootinguhyo.score.ScoreBonusManager;
import com.shootinguhyo.stage.Stage;
import com.shootinguhyo.stage.StageManager;
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
 *  - すべての状態(タイトル/キャラ選択/オプション/会話/プレイ/ボス戦/エンディング)を管理
 *
 * 【画面遷移の全体図】
 *   TITLE (NEW GAME / OPTION / QUIT)
 *     ├ NEW GAME → CHARACTER_SELECT → PLAYING(stage1)
 *     │   └ ボス出現条件 → DIALOG(プリ) → BOSS_FIGHT
 *     │       └ 撃破 → BOSS_DEFEAT(演出) → DIALOG(ポスト) → STAGE_CLEAR
 *     │           ├ 次ステージあり → PLAYING(stageN)
 *     │           └ 最終クリア → ENDING → TITLE
 *     ├ OPTION → OPTIONS → TITLE
 *     └ QUIT (System.exit)
 *
 * 【スレッドモデル】
 *  別スレッド(gameThread)でメインループを回し、毎フレーム update→repaint を呼ぶ。
 *  描画はSwingのEDTで処理される。
 */
public class GamePanel extends JPanel implements Runnable {
    // プレイフィールド + 右側HUD のサイズ
    public static final int FIELD_WIDTH = 384;
    public static final int FIELD_HEIGHT = 448;
    public static final int PANEL_WIDTH = 576;
    public static final int PANEL_HEIGHT = 448;
    private static final int FPS = 60;

    // バックバッファ(ダブルバッファリング)
    private BufferedImage backBuffer;
    private Graphics2D backG;
    private Thread gameThread;
    private InputHandler input;

    // 状態管理
    private GameState gameState = GameState.TITLE;
    private GameState prevGameState = GameState.TITLE;

    // ゲーム中オブジェクト
    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<FastEnemy> fastEnemies = new ArrayList<>();
    private List<PlayerBullet> playerBullets = new ArrayList<>();
    private List<EnemyBullet> enemyBullets = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private Boss boss;

    // ステージ進行
    private Stage currentStage;          // 現在のステージインスタンス
    private int stageFrame = 0;          // ステージ開始からの経過フレーム
    private boolean bossSpawned = false; // 今のステージでボスを出したか

    // ゲーム共通サービス(ひな型からのコネクト)
    private GameOptions options;
    private GameConfig config;
    private AudioManager audio;
    private StageManager stageManager;
    private ScoreBonusManager scoreBonus;
    private BossDefeatEffect bossDefeatEffect;

    private HUD hud;
    private SpellCardEffect spellCardEffect;

    // 会話シーン(プリ/ポスト)
    private DialogScene dialogScene;
    private boolean dialogIsPreBoss = false;

    // 背景の星
    private double[] starX = new double[150];
    private double[] starY = new double[150];
    private double[] starSpeed = new double[150];
    private int[] starBrightness = new int[150];

    private Random rand = new Random();

    private int endFrame = 0;

    private Boss.Phase lastBossPhase = null;

    // ボム(スペルカード)ダメージ管理
    private boolean prevBombing = false;          // 前フレームのbombing状態(立ち上がり検出用)
    private int bombDamageTickFrame = 0;          // ボム継続中のダメージ間隔カウンタ
    private static final int BOMB_BURST_DAMAGE_ENEMY = 600;  // 発動瞬間の全敵への一撃
    private static final int BOMB_BURST_DAMAGE_BOSS  = 300;  // 発動瞬間のボスへの一撃
    private static final int BOMB_TICK_DAMAGE_ENEMY  = 80;   // ボム持続中の連続ダメージ(敵)
    private static final int BOMB_TICK_DAMAGE_BOSS   = 35;   // ボム持続中の連続ダメージ(ボス)
    private static final int BOMB_TICK_INTERVAL      = 8;    // ティック間隔(フレーム)

    // メニュー位置
    private final String[] titleMenu = {
            "Start", "Extra Start", "Practice Start", "Replay",
            "Score", "Music Room", "Option", "Quit"
    };
    private int titleMenuIndex = 0;
    /** 未実装メニューを押したときの一時告知タイマ(描画用)。 */
    private int titleNoticeFrame = 0;
    private String titleNoticeText = "";

    // モード選択
    private int modeSelectIndex = 1; // 初期はNormal
    private static final String[] MODE_DESCRIPTIONS = {
            "STGが苦手な方向けです  (全6面)",
            "おおよそほとんどの人向けです  (全6面)",
            "アーケードSTG並の難易度です  (全6面)",
            "ちょっとおかしい人向け難易度です  (全6面)"
    };

    private int charSelectIndex = 0;

    private static final String[] OPTION_LABELS = {
            "Difficulty", "BGM Volume", "SE Volume", "Show Hitbox", "Back"
    };
    private int optionIndex = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);

        input = new InputHandler();
        addKeyListener(input);
        setFocusable(true);

        backBuffer = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        backG = backBuffer.createGraphics();
        backG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 共通サービスの初期化
        options = new GameOptions();
        config = new GameConfig();
        audio = new AudioManager(options);
        stageManager = new StageManager();
        scoreBonus = new ScoreBonusManager();
        bossDefeatEffect = new BossDefeatEffect();

        hud = new HUD();
        spellCardEffect = new SpellCardEffect();
        initStars();

        audio.playTitleBgm();
    }

    private void initStars() {
        for (int i = 0; i < starX.length; i++) {
            starX[i] = rand.nextInt(FIELD_WIDTH);
            starY[i] = rand.nextInt(FIELD_HEIGHT);
            starSpeed[i] = rand.nextDouble() * 1.5 + 0.3;
            starBrightness[i] = rand.nextInt(180) + 60;
        }
    }

    private void updateStars() {
        for (int i = 0; i < starY.length; i++) {
            starY[i] += starSpeed[i];
            if (starY[i] > FIELD_HEIGHT) {
                starY[i] = 0;
                starX[i] = rand.nextInt(FIELD_WIDTH);
            }
        }
    }

    private void drawStars(Graphics2D g) {
        for (int i = 0; i < starX.length; i++) {
            int b = starBrightness[i];
            g.setColor(new Color(b, b, b));
            int size = starSpeed[i] > 1.2 ? 2 : 1;
            g.fillRect((int) starX[i], (int) starY[i], size, size);
        }
    }

    public void startGame() {
        gameThread = new Thread(this);
        gameThread.setDaemon(true);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long nsPerFrame = 1_000_000_000L / FPS;

        while (true) {
            long now = System.nanoTime();
            long elapsed = now - lastTime;
            if (elapsed >= nsPerFrame) {
                lastTime = now;
                update();
                repaint();
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /** 1フレームの状態更新。状態ごとに別メソッドにディスパッチ。 */
    private void update() {
        switch (gameState) {
            case TITLE -> updateTitle();
            case MODE_SELECT -> updateModeSelect();
            case CHARACTER_SELECT -> updateCharacterSelect();
            case OPTIONS -> updateOptions();
            case DIALOG -> updateDialog();
            case PLAYING -> updatePlaying();
            case BOSS_FIGHT -> updateBossFight();
            case BOSS_DEFEAT -> updateBossDefeat();
            case STAGE_CLEAR -> updateStageClear();
            case ENDING -> updateEnding();
            case GAME_OVER -> updateGameOver();
            case CLEAR -> updateClear();
            case PAUSED -> updatePaused();
        }
        input.clearJustPressed();
    }

    // ===================== TITLE =====================

    private void updateTitle() {
        updateStars();
        if (titleNoticeFrame > 0) titleNoticeFrame--;

        if (input.isJustPressed(KeyEvent.VK_UP) || input.isJustPressed(KeyEvent.VK_W)) {
            titleMenuIndex = (titleMenuIndex - 1 + titleMenu.length) % titleMenu.length;
            audio.playSe(AudioManager.Se.MENU_MOVE);
        }
        if (input.isJustPressed(KeyEvent.VK_DOWN) || input.isJustPressed(KeyEvent.VK_S)) {
            titleMenuIndex = (titleMenuIndex + 1) % titleMenu.length;
            audio.playSe(AudioManager.Se.MENU_MOVE);
        }
        if (input.isJustPressed(KeyEvent.VK_ENTER) || input.isJustPressed(KeyEvent.VK_Z)) {
            switch (titleMenuIndex) {
                case 0 -> {
                    // Start: モード選択へ
                    audio.playSe(AudioManager.Se.MENU_SELECT);
                    modeSelectIndex = options.getDifficulty().ordinal();
                    gameState = GameState.MODE_SELECT;
                }
                case 6 -> {
                    // Option
                    audio.playSe(AudioManager.Se.MENU_SELECT);
                    optionIndex = 0;
                    gameState = GameState.OPTIONS;
                }
                case 7 -> {
                    audio.playSe(AudioManager.Se.MENU_SELECT);
                    System.exit(0);
                }
                default -> {
                    // 未実装メニュー
                    audio.playSe(AudioManager.Se.MENU_MOVE);
                    titleNoticeText = "「" + titleMenu[titleMenuIndex] + "」は未実装です";
                    titleNoticeFrame = 120;
                }
            }
        }
    }

    // ===================== MODE_SELECT =====================

    private void updateModeSelect() {
        updateStars();
        Difficulty[] modes = Difficulty.values();

        if (input.isJustPressed(KeyEvent.VK_UP) || input.isJustPressed(KeyEvent.VK_W)) {
            modeSelectIndex = (modeSelectIndex - 1 + modes.length) % modes.length;
            audio.playSe(AudioManager.Se.MENU_MOVE);
        }
        if (input.isJustPressed(KeyEvent.VK_DOWN) || input.isJustPressed(KeyEvent.VK_S)) {
            modeSelectIndex = (modeSelectIndex + 1) % modes.length;
            audio.playSe(AudioManager.Se.MENU_MOVE);
        }
        if (input.isJustPressed(KeyEvent.VK_ENTER) || input.isJustPressed(KeyEvent.VK_Z)) {
            audio.playSe(AudioManager.Se.MENU_SELECT);
            options.setDifficulty(modes[modeSelectIndex]);
            charSelectIndex = 0;
            gameState = GameState.CHARACTER_SELECT;
        }
        if (input.isJustPressed(KeyEvent.VK_ESCAPE) || input.isJustPressed(KeyEvent.VK_X)) {
            audio.playSe(AudioManager.Se.MENU_SELECT);
            gameState = GameState.TITLE;
        }
    }

    // ===================== CHARACTER_SELECT =====================

    private void updateCharacterSelect() {
        updateStars();
        List<PlayerCharacter> chars = CharacterRegistry.all();

        if (input.isJustPressed(KeyEvent.VK_LEFT) || input.isJustPressed(KeyEvent.VK_A)) {
            charSelectIndex = (charSelectIndex - 1 + chars.size()) % chars.size();
            audio.playSe(AudioManager.Se.MENU_MOVE);
        }
        if (input.isJustPressed(KeyEvent.VK_RIGHT) || input.isJustPressed(KeyEvent.VK_D)) {
            charSelectIndex = (charSelectIndex + 1) % chars.size();
            audio.playSe(AudioManager.Se.MENU_MOVE);
        }
        if (input.isJustPressed(KeyEvent.VK_ENTER) || input.isJustPressed(KeyEvent.VK_Z)) {
            audio.playSe(AudioManager.Se.MENU_SELECT);
            config.setCharacter(chars.get(charSelectIndex));
            config.setDifficulty(options.getDifficulty()); // 難易度反映
            startNewRun();
        }
        if (input.isJustPressed(KeyEvent.VK_ESCAPE) || input.isJustPressed(KeyEvent.VK_X)) {
            audio.playSe(AudioManager.Se.MENU_SELECT);
            gameState = GameState.MODE_SELECT;
        }
    }

    /** 新規プレイ開始：プレイヤー初期化＆ステージ1を開始。 */
    private void startNewRun() {
        stageManager.reset();
        scoreBonus.reset();
        initGameForCurrentStage(true);
        audio.playBgmForStage(stageManager.getCurrentStage());
        gameState = GameState.PLAYING;
    }

    /**
     * 現在のステージ用にゲーム状態を初期化する。
     * @param newRun true=新規プレイ(残機リセット)、false=次ステージ持ち越し
     */
    private void initGameForCurrentStage(boolean newRun) {
        if (newRun || player == null) {
            player = new Player(192, 380, config.getCharacter());
        } else {
            // 残機等を持ち越して、位置だけ戻す
            player.x = 192;
            player.y = 380;
        }
        enemies.clear();
        fastEnemies.clear();
        playerBullets.clear();
        enemyBullets.clear();
        items.clear();
        particles.clear();
        boss = null;
        currentStage = stageManager.createCurrentStage();
        stageFrame = 0;
        bossSpawned = false;
        lastBossPhase = null;
        spellCardEffect.deactivate();
        bossDefeatEffect.update();
        endFrame = 0;
    }

    // ===================== OPTIONS =====================

    private void updateOptions() {
        updateStars();
        if (input.isJustPressed(KeyEvent.VK_UP) || input.isJustPressed(KeyEvent.VK_W)) {
            optionIndex = (optionIndex - 1 + OPTION_LABELS.length) % OPTION_LABELS.length;
            audio.playSe(AudioManager.Se.MENU_MOVE);
        }
        if (input.isJustPressed(KeyEvent.VK_DOWN) || input.isJustPressed(KeyEvent.VK_S)) {
            optionIndex = (optionIndex + 1) % OPTION_LABELS.length;
            audio.playSe(AudioManager.Se.MENU_MOVE);
        }
        if (input.isJustPressed(KeyEvent.VK_LEFT) || input.isJustPressed(KeyEvent.VK_A)) {
            changeOption(-1);
        }
        if (input.isJustPressed(KeyEvent.VK_RIGHT) || input.isJustPressed(KeyEvent.VK_D)) {
            changeOption(1);
        }
        if (input.isJustPressed(KeyEvent.VK_ENTER) || input.isJustPressed(KeyEvent.VK_Z)) {
            if (optionIndex == OPTION_LABELS.length - 1) {
                backToTitle();
            }
        }
        if (input.isJustPressed(KeyEvent.VK_ESCAPE) || input.isJustPressed(KeyEvent.VK_X)) {
            backToTitle();
        }
    }

    private void changeOption(int dir) {
        switch (optionIndex) {
            case 0 -> {
                Difficulty[] all = Difficulty.values();
                int cur = options.getDifficulty().ordinal();
                int newIdx = (cur + dir + all.length) % all.length;
                options.setDifficulty(all[newIdx]);
                audio.playSe(AudioManager.Se.MENU_MOVE);
            }
            case 1 -> { options.setBgmVolume(options.getBgmVolume() + dir * 5); audio.syncVolume(); }
            case 2 -> { options.setSeVolume(options.getSeVolume() + dir * 5); audio.syncVolume(); }
            case 3 -> options.setShowHitbox(!options.isShowHitbox());
        }
    }

    private void backToTitle() {
        audio.playSe(AudioManager.Se.MENU_SELECT);
        options.save();
        gameState = GameState.TITLE;
    }

    // ===================== DIALOG =====================

    private void startDialog(boolean isPreBoss) {
        PlayerCharacter pc = config.getCharacter() != null
                ? config.getCharacter()
                : CharacterRegistry.getDefault();
        int stageNo = stageManager.getCurrentStage();
        dialogScene = new DialogScene(
                isPreBoss ? DialogSamples.preBoss(stageNo, pc)
                          : DialogSamples.postBoss(stageNo, pc));
        dialogIsPreBoss = isPreBoss;
        gameState = GameState.DIALOG;
    }

    private void updateDialog() {
        updateStars();
        if (dialogScene == null) {
            finishDialog();
            return;
        }
        dialogScene.update(input);
        if (dialogScene.isFinished()) {
            finishDialog();
        }
    }

    private void finishDialog() {
        if (dialogIsPreBoss) {
            // プリボス → ボス出現
            boss = new Boss(192, 80);
            audio.playBossBgm(stageManager.getCurrentStage());
            audio.playSe(AudioManager.Se.SPELL_DECLARE);
            gameState = GameState.BOSS_FIGHT;
        } else {
            // ポストボス → ステージクリア画面
            gameState = GameState.STAGE_CLEAR;
            endFrame = 0;
        }
    }

    // ===================== PLAYING =====================

    private void updatePlaying() {
        updateStars();
        stageFrame++;

        updateNearestTarget();
        player.update(input);
        if (input.isJustPressed(KeyEvent.VK_X)) {
            audio.playSe(AudioManager.Se.BOMB);
        }

        applyBombDamage();

        if (input.isJustPressed(KeyEvent.VK_ESCAPE)) {
            prevGameState = gameState;
            gameState = GameState.PAUSED;
            return;
        }

        currentStage.update(stageFrame, enemies, fastEnemies);

        for (FastEnemy fe : fastEnemies) {
            fe.setPlayerPosition(player.x, player.y);
        }

        updateEnemies();
        updateBullets();
        updateItems();
        updateParticles();
        spellCardEffect.update();

        // 雑魚を片付けてからボス出現タイミングへ
        if (currentStage.isBossTime(stageFrame) && !bossSpawned
                && enemies.isEmpty() && fastEnemies.isEmpty()) {
            bossSpawned = true;
            // プリボス会話に入る
            startDialog(true);
            return;
        }

        hud.update(player.getScore());
        scoreBonus.checkAndApply(player); // スコアボーナス監視

        if (!player.isAlive()) {
            audio.playSe(AudioManager.Se.PLAYER_HIT);
            audio.fadeOutBgm(30);
            audio.playGameOverBgm();
            gameState = GameState.GAME_OVER;
            endFrame = 0;
        }
    }

    // ===================== BOSS_FIGHT =====================

    private void updateBossFight() {
        updateStars();
        stageFrame++;

        updateNearestTarget();
        player.update(input);
        if (input.isJustPressed(KeyEvent.VK_X)) {
            audio.playSe(AudioManager.Se.BOMB);
        }

        applyBombDamage();

        if (input.isJustPressed(KeyEvent.VK_ESCAPE)) {
            prevGameState = gameState;
            gameState = GameState.PAUSED;
            return;
        }

        if (boss != null) {
            boss.setPlayerPosition(player.x, player.y);
            boss.update();

            Boss.Phase currentPhase = boss.getPhase();
            if (currentPhase != lastBossPhase) {
                lastBossPhase = currentPhase;
                if (currentPhase == Boss.Phase.SPELL1 || currentPhase == Boss.Phase.SPELL2
                        || currentPhase == Boss.Phase.SPELL3) {
                    spellCardEffect.activate(boss.getSpellColor(), boss.getSpellName());
                    audio.playSe(AudioManager.Se.SPELL_DECLARE);
                    player.addScore(10000);
                    enemyBullets.clear();
                } else if (currentPhase == Boss.Phase.DEFEATED) {
                    spellCardEffect.deactivate();
                    audio.playSe(AudioManager.Se.SPELL_GET);
                    player.addScore(50000);
                    enemyBullets.clear();
                    createExplosion((int) boss.x, (int) boss.y, 40, Color.YELLOW);
                    bossDefeatEffect.activate(boss.x, boss.y);
                    audio.fadeOutBgm(30);
                    gameState = GameState.BOSS_DEFEAT;
                    return;
                }
            }

            List<EnemyBullet> bossBullets = boss.getAndClearNewBullets();
            enemyBullets.addAll(bossBullets);
        }

        updateBullets();
        updateItems();
        updateParticles();
        spellCardEffect.update();

        hud.update(player.getScore());
        scoreBonus.checkAndApply(player);

        if (!player.isAlive()) {
            audio.fadeOutBgm(30);
            audio.playGameOverBgm();
            gameState = GameState.GAME_OVER;
            endFrame = 0;
        }
    }

    // ===================== BOSS_DEFEAT =====================

    private void updateBossDefeat() {
        updateStars();
        bossDefeatEffect.update();
        updateBullets();      // 残っている自弾の処理
        updateItems();
        updateParticles();

        if (!bossDefeatEffect.isActive()) {
            // ポストボス会話へ
            startDialog(false);
        }
    }

    // ===================== STAGE_CLEAR =====================

    private void updateStageClear() {
        endFrame++;
        updateStars();
        if (endFrame > 120 && input.isJustPressed(KeyEvent.VK_ENTER)) {
            audio.playSe(AudioManager.Se.MENU_SELECT);
            // 次ステージへ。最終ステージならエンディングへ。
            if (!stageManager.advance()) {
                gameState = GameState.ENDING;
                audio.playEndingBgm();
                endFrame = 0;
            } else {
                initGameForCurrentStage(false);
                audio.playBgmForStage(stageManager.getCurrentStage());
                gameState = GameState.PLAYING;
            }
        }
    }

    // ===================== ENDING =====================

    private void updateEnding() {
        endFrame++;
        updateStars();
        // 600フレーム以上経過後はEnterでタイトルへ戻れる
        if (endFrame > 600 && input.isJustPressed(KeyEvent.VK_ENTER)) {
            audio.playSe(AudioManager.Se.MENU_SELECT);
            audio.playTitleBgm();
            gameState = GameState.TITLE;
        }
    }

    private void updatePaused() {
        if (input.isJustPressed(KeyEvent.VK_ESCAPE) || input.isJustPressed(KeyEvent.VK_ENTER)) {
            gameState = prevGameState;
        }
    }

    private void updateGameOver() {
        endFrame++;
        updateStars();
        if (endFrame > 120 && input.isJustPressed(KeyEvent.VK_ENTER)) {
            audio.playTitleBgm();
            gameState = GameState.TITLE;
        }
    }

    /** 旧CLEAR状態(互換)。タイトルへ戻すだけ。 */
    private void updateClear() {
        endFrame++;
        updateStars();
        if (endFrame > 120 && input.isJustPressed(KeyEvent.VK_ENTER)) {
            audio.playTitleBgm();
            gameState = GameState.TITLE;
        }
    }

    // ===================== NEAREST TARGET =====================

    /**
     * 最寄りの敵(雑魚/Fast/ボス)を計算してPlayerに伝える。
     * 自機オプションのホーミング弾の目標として使われる。
     */
    private void updateNearestTarget() {
        if (player == null) return;
        double bestDist = Double.MAX_VALUE;
        double tx = -1, ty = -1;
        for (Enemy e : enemies) {
            if (!e.active || e.isDefeated()) continue;
            double d = MathUtil.distance(player.x, player.y, e.x, e.y);
            if (d < bestDist) { bestDist = d; tx = e.x; ty = e.y; }
        }
        for (FastEnemy fe : fastEnemies) {
            if (!fe.active || fe.isDefeated()) continue;
            double d = MathUtil.distance(player.x, player.y, fe.x, fe.y);
            if (d < bestDist) { bestDist = d; tx = fe.x; ty = fe.y; }
        }
        if (boss != null && !boss.isDefeated()) {
            double d = MathUtil.distance(player.x, player.y, boss.x, boss.y);
            if (d < bestDist) { bestDist = d; tx = boss.x; ty = boss.y; }
        }
        player.setNearestEnemy(tx, ty);
    }

    // ===================== BOMB DAMAGE =====================

    /**
     * ボム(スペルカード)発動中、画面上の敵とボスにダメージを与える。
     * - 発動の瞬間(立ち上がり)に強い一撃を全体に
     * - 持続中は一定間隔で継続ダメージ
     * 既存の弾消し(updateBullets内のplayer.isBombing()判定)はそのまま動作する。
     */
    private void applyBombDamage() {
        boolean bombing = player != null && player.isBombing();

        // 立ち上がり：発動の瞬間
        if (bombing && !prevBombing) {
            // 画面上の全雑魚に一撃(画面上の雑魚はだいたい倒せる威力)
            for (Enemy e : enemies) {
                if (e.active && !e.isDefeated()) {
                    e.takeDamage(BOMB_BURST_DAMAGE_ENEMY);
                }
            }
            for (FastEnemy fe : fastEnemies) {
                if (fe.active && !fe.isDefeated()) {
                    fe.takeDamage(BOMB_BURST_DAMAGE_ENEMY);
                }
            }
            // ボスにも一撃
            if (boss != null && !boss.isDefeated()) {
                boss.takeDamage(BOMB_BURST_DAMAGE_BOSS);
            }
            bombDamageTickFrame = 0;
        }

        // 継続ダメージ
        if (bombing) {
            bombDamageTickFrame++;
            if (bombDamageTickFrame >= BOMB_TICK_INTERVAL) {
                bombDamageTickFrame = 0;
                for (Enemy e : enemies) {
                    if (e.active && !e.isDefeated()) {
                        e.takeDamage(BOMB_TICK_DAMAGE_ENEMY);
                    }
                }
                for (FastEnemy fe : fastEnemies) {
                    if (fe.active && !fe.isDefeated()) {
                        fe.takeDamage(BOMB_TICK_DAMAGE_ENEMY);
                    }
                }
                if (boss != null && !boss.isDefeated()) {
                    boss.takeDamage(BOMB_TICK_DAMAGE_BOSS);
                }
            }
        } else {
            bombDamageTickFrame = 0;
        }

        prevBombing = bombing;
    }

    // ===================== ENTITY UPDATE HELPERS =====================

    private void updateEnemies() {
        Iterator<Enemy> ei = enemies.iterator();
        while (ei.hasNext()) {
            Enemy e = ei.next();
            e.update();
            enemyBullets.addAll(e.getAndClearNewBullets());

            if (e.isDefeated()) {
                createExplosion((int) e.x, (int) e.y, 12, new Color(180, 100, 255));
                // 12%の確率で大P(価値+3)、それ以外は通常P(価値+1)
                boolean bigPower = rand.nextInt(100) < 12;
                items.add(new Item(e.x, e.y, Item.ItemType.POWER, bigPower));
                items.add(new Item(e.x + 8, e.y, Item.ItemType.POINT));
                player.addScore(e.getScore());
                audio.playSe(AudioManager.Se.EXPLOSION);
            }

            if (!e.active) ei.remove();
        }

        Iterator<FastEnemy> fi = fastEnemies.iterator();
        while (fi.hasNext()) {
            FastEnemy fe = fi.next();
            fe.update();
            enemyBullets.addAll(fe.getAndClearNewBullets());

            if (fe.isDefeated()) {
                createExplosion((int) fe.x, (int) fe.y, 10, new Color(0, 200, 200));
                // FastEnemyは小さいので大Pの確率も低め(8%)
                boolean bigPower = rand.nextInt(100) < 8;
                items.add(new Item(fe.x, fe.y, Item.ItemType.POWER, bigPower));
                player.addScore(fe.getScore());
                audio.playSe(AudioManager.Se.EXPLOSION);
            }

            if (!fe.active) fi.remove();
        }
    }

    private void updateBullets() {
        playerBullets.addAll(player.getAndClearNewBullets());

        Iterator<PlayerBullet> pbi = playerBullets.iterator();
        while (pbi.hasNext()) {
            PlayerBullet pb = pbi.next();
            pb.update();
            if (!pb.active) { pbi.remove(); continue; }

            boolean hit = false;
            for (Enemy e : enemies) {
                if (e.active && MathUtil.distance(pb.x, pb.y, e.x, e.y) < 12) {
                    e.takeDamage(pb.getDamage());
                    audio.playSe(AudioManager.Se.ENEMY_HIT);
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                for (FastEnemy fe : fastEnemies) {
                    if (fe.active && MathUtil.distance(pb.x, pb.y, fe.x, fe.y) < 8) {
                        fe.takeDamage(pb.getDamage());
                        audio.playSe(AudioManager.Se.ENEMY_HIT);
                        hit = true;
                        break;
                    }
                }
            }
            if (!hit && boss != null && !boss.isDefeated()) {
                if (MathUtil.distance(pb.x, pb.y, boss.x, boss.y) < 30) {
                    boss.takeDamage(pb.getDamage());
                    player.addScore(50);
                    audio.playSe(AudioManager.Se.ENEMY_HIT);
                    hit = true;
                }
            }
            if (hit) pbi.remove();
        }

        Iterator<EnemyBullet> ebi = enemyBullets.iterator();
        while (ebi.hasNext()) {
            EnemyBullet eb = ebi.next();
            eb.update();
            if (!eb.active) { ebi.remove(); continue; }

            if (player.isBombing()) { ebi.remove(); continue; }

            double dist = MathUtil.distance(eb.x, eb.y, player.x, player.y);
            if (dist < 16 && dist > player.getHitboxRadius() + eb.getRadius()) {
                player.addGraze(1);
                player.addScore(100);
            }

            if (!player.isInvincible() && dist < player.getHitboxRadius() + eb.getRadius()) {
                player.hit();
                ebi.remove();
                audio.playSe(AudioManager.Se.PLAYER_HIT);
                createExplosion((int) player.x, (int) player.y, 15, Color.WHITE);
            }
        }
    }

    private void updateItems() {
        Iterator<Item> ii = items.iterator();
        while (ii.hasNext()) {
            Item item = ii.next();
            item.update();
            if (!item.active) { ii.remove(); continue; }

            if (MathUtil.distance(item.x, item.y, player.x, player.y) < player.getHitboxRadius() + item.getRadius()) {
                if (item.getType() == Item.ItemType.POWER) {
                    // 通常P=+1、大P=+3。Stage1で簡単に満タンにならないよう控えめに。
                    player.addPower(item.isBig() ? 3 : 1);
                } else {
                    player.addScore(100);
                }
                audio.playSe(AudioManager.Se.ITEM);
                ii.remove();
            }
        }
    }

    private void updateParticles() {
        Iterator<Particle> pi = particles.iterator();
        while (pi.hasNext()) {
            Particle p = pi.next();
            p.update();
            if (!p.active) pi.remove();
        }
    }

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

    // ===================== PAINT / RENDER =====================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        render();
        g.drawImage(backBuffer, 0, 0, null);
    }

    private void render() {
        Graphics2D g = backG;
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        switch (gameState) {
            case TITLE -> renderTitle(g);
            case MODE_SELECT -> renderModeSelect(g);
            case CHARACTER_SELECT -> renderCharacterSelect(g);
            case OPTIONS -> renderOptions(g);
            case DIALOG -> { renderGame(g); if (dialogScene != null) dialogScene.draw(g, FIELD_WIDTH, FIELD_HEIGHT); }
            case PLAYING, BOSS_FIGHT -> renderGame(g);
            case BOSS_DEFEAT -> { renderGame(g); bossDefeatEffect.draw(g, FIELD_WIDTH, FIELD_HEIGHT); }
            case STAGE_CLEAR -> { renderGame(g); renderStageClear(g); }
            case ENDING -> renderEnding(g);
            case PAUSED -> { renderGame(g); renderPause(g); }
            case GAME_OVER -> { renderGame(g); renderGameOver(g); }
            case CLEAR -> { renderGame(g); renderClear(g); }
        }
    }

    private void renderTitle(Graphics2D g) {
        // 背景
        g.setColor(new Color(18, 4, 28));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        // 赤い放射状の柔らかい光(中央寄り)
        java.awt.RadialGradientPaint rp = new java.awt.RadialGradientPaint(
                PANEL_WIDTH / 2f, PANEL_HEIGHT / 2f + 40, 320f,
                new float[]{0f, 1f},
                new Color[]{new Color(120, 20, 40, 110), new Color(0, 0, 0, 0)});
        java.awt.Paint oldPaint = g.getPaint();
        g.setPaint(rp);
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.setPaint(oldPaint);
        drawStars(g);

        // 左側：縦書きの和風タイトル
        String[] vert = { "U", "H", "Y", "O" };
        g.setFont(new Font("Serif", Font.BOLD, 38));
        int tx = 56;
        int ty = 70;
        // タイトル背景の縦帯
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(tx - 14, ty - 36, 56, vert.length * 50 + 24);
        g.setColor(new Color(200, 60, 80));
        g.drawRect(tx - 14, ty - 36, 56, vert.length * 50 + 24);
        for (int i = 0; i < vert.length; i++) {
            // 影
            g.setColor(new Color(60, 0, 0));
            g.drawString(vert[i], tx + 2, ty + i * 50 + 2);
            // 本体（赤系）
            g.setColor(new Color(235, 90, 110));
            g.drawString(vert[i], tx, ty + i * 50);
        }
        // 副題(横書き)
        g.setFont(new Font("Serif", Font.ITALIC, 14));
        g.setColor(new Color(230, 200, 220));
        g.drawString("~ 打倒ちくしょう ~", 30, ty + vert.length * 50 + 30);

        // 中央：タイトル絵(PNG)があれば優先表示、無ければデフォルトキャラを表示
        // クラスパスは大文字小文字を区別する。複数のファイル名候補を試す。
        java.awt.image.BufferedImage titleArt = com.shootinguhyo.graphics.ImageLoader.loadAny(
                "/title/title.png",        "/title/Title.png",
                "/title/title_screen.png", "/title/Title_screen.png",
                "/title/titlescreen.png",  "/title/TitleScreen.png",
                "/title/title.jpg",        "/title/title_screen.jpg"
        );
        if (titleArt != null) {
            // パネル中央寄りに、メニューと被らないサイズで配置
            int maxW = PANEL_WIDTH - 230 - 130; // 左タイトル帯と右メニューを避ける
            int maxH = PANEL_HEIGHT - 100;
            double scale = Math.min(
                    (double) maxW / titleArt.getWidth(),
                    (double) maxH / titleArt.getHeight()
            );
            int drawW = (int) (titleArt.getWidth() * scale);
            int drawH = (int) (titleArt.getHeight() * scale);
            int drawX = 130 + (maxW - drawW) / 2;
            int drawY = 60 + (maxH - drawH) / 2;
            // 縁を少し暗く落として馴染ませる(影付き)
            g.setColor(new Color(0, 0, 0, 80));
            g.fillRoundRect(drawX - 4, drawY - 4, drawW + 8, drawH + 8, 8, 8);
            g.drawImage(titleArt, drawX, drawY, drawW, drawH, null);
        } else {
            PlayerCharacter portraitChar = CharacterRegistry.getDefault();
            portraitChar.getPortraitSprite().draw(g, PANEL_WIDTH / 2.0 - 30, PANEL_HEIGHT / 2.0 + 10, 10);
        }

        // 右側：縦メニュー
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        int mx = PANEL_WIDTH - 200;
        int my = 90;
        int spacing = 32;
        for (int i = 0; i < titleMenu.length; i++) {
            boolean selected = i == titleMenuIndex;
            if (selected) {
                // 選択中のハイライト帯
                g.setColor(new Color(255, 220, 120, 80));
                g.fillRect(mx - 12, my + i * spacing - 18, 180, 26);
                g.setColor(new Color(255, 240, 120));
                g.drawString("▶ " + titleMenu[i], mx, my + i * spacing);
            } else {
                g.setColor(new Color(200, 200, 230));
                g.drawString("  " + titleMenu[i], mx, my + i * spacing);
            }
        }

        // 未実装メニューを押したときの一時告知
        if (titleNoticeFrame > 0) {
            int alpha = Math.min(255, titleNoticeFrame * 4);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.setColor(new Color(255, 220, 120, alpha));
            FontMetrics fm = g.getFontMetrics();
            int nx = (PANEL_WIDTH - fm.stringWidth(titleNoticeText)) / 2;
            g.setColor(new Color(0, 0, 0, Math.min(180, alpha)));
            g.fillRect(nx - 8, PANEL_HEIGHT - 78, fm.stringWidth(titleNoticeText) + 16, 24);
            g.setColor(new Color(255, 220, 120, alpha));
            g.drawString(titleNoticeText, nx, PANEL_HEIGHT - 60);
        }

        // フッタ：著作権風
        g.setFont(new Font("Serif", Font.PLAIN, 11));
        g.setColor(new Color(180, 180, 200));
        String copyright = "(C) 2026  Shooting Uhyo Project";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(copyright, (PANEL_WIDTH - fm.stringWidth(copyright)) / 2, PANEL_HEIGHT - 18);
    }

    // ===================== MODE_SELECT RENDER =====================

    private void renderModeSelect(Graphics2D g) {
        g.setColor(new Color(15, 3, 24));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawStars(g);
        // 赤い放射ライト
        java.awt.RadialGradientPaint rp = new java.awt.RadialGradientPaint(
                PANEL_WIDTH / 2f, PANEL_HEIGHT / 2f, 280f,
                new float[]{0f, 1f},
                new Color[]{new Color(140, 30, 60, 120), new Color(0, 0, 0, 0)});
        java.awt.Paint oldPaint = g.getPaint();
        g.setPaint(rp);
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.setPaint(oldPaint);

        // ヘッダ
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.setColor(new Color(255, 230, 240));
        String header = "モードを選択してね";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(header, (PANEL_WIDTH - fm.stringWidth(header)) / 2, 50);
        // 下線
        g.setColor(new Color(200, 80, 100));
        g.drawLine(PANEL_WIDTH / 2 - 130, 60, PANEL_WIDTH / 2 + 130, 60);

        Difficulty[] modes = Difficulty.values();
        // 各モードの色合い
        Color[] modeColors = {
                new Color(120, 230, 120),  // EASY - 緑
                new Color(120, 200, 255),  // NORMAL - 青
                new Color(255, 150, 120),  // HARD - 橙
                new Color(220, 120, 220)   // LUNATIC - 紫 これいる？ｗ
        };

        int baseY = 110;
        int rowH = 70;
        for (int i = 0; i < modes.length; i++) {
            boolean selected = (i == modeSelectIndex);
            int y = baseY + i * rowH;

            if (selected) {
                // ハイライト枠
                g.setColor(new Color(255, 230, 120, 60));
                g.fillRect(120, y - 24, PANEL_WIDTH - 240, 60);
                g.setColor(new Color(255, 230, 120));
                g.drawRect(120, y - 24, PANEL_WIDTH - 240, 60);
            }

            // モード名
            g.setFont(new Font("SansSerif", Font.BOLD, selected ? 26 : 22));
            g.setColor(selected ? modeColors[i] : modeColors[i].darker());
            g.drawString(modes[i].displayName, 150, y);

            // 説明
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g.setColor(selected ? new Color(255, 255, 255) : new Color(160, 160, 180));
            g.drawString(MODE_DESCRIPTIONS[i], 150, y + 22);
        }

        // フッタ操作説明
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(new Color(150, 150, 180));
        String hint = "Up/Down: 選択   Z/Enter: 決定   X/Esc: 戻る";
        fm = g.getFontMetrics();
        g.drawString(hint, (PANEL_WIDTH - fm.stringWidth(hint)) / 2, PANEL_HEIGHT - 18);
    }

    private void renderCharacterSelect(Graphics2D g) {
        g.setColor(new Color(12, 4, 22));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawStars(g);
        // 赤系の柔らかな放射
        java.awt.RadialGradientPaint rp = new java.awt.RadialGradientPaint(
                PANEL_WIDTH * 0.7f, PANEL_HEIGHT * 0.5f, 260f,
                new float[]{0f, 1f},
                new Color[]{new Color(150, 30, 60, 110), new Color(0, 0, 0, 0)});
        java.awt.Paint oldPaint = g.getPaint();
        g.setPaint(rp);
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.setPaint(oldPaint);

        // 上部見出し
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.setColor(new Color(255, 230, 240));
        String title = "プレイヤーを選択してね";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (PANEL_WIDTH - fm.stringWidth(title)) / 2, 36);
        g.setColor(new Color(200, 80, 100));
        g.drawLine(PANEL_WIDTH / 2 - 150, 46, PANEL_WIDTH / 2 + 150, 46);

        List<PlayerCharacter> chars = CharacterRegistry.all();
        PlayerCharacter cur = chars.get(charSelectIndex);

        // 左側：現在選択中モード(難易度)
        int leftX = 30;
        int modeY = 110;
        Color modeColor = switch (options.getDifficulty()) {
            case EASY    -> new Color(120, 230, 120);
            case NORMAL  -> new Color(120, 200, 255);
            case HARD    -> new Color(255, 150, 120);
            case LUNATIC -> new Color(220, 120, 220);
        };
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.setColor(modeColor);
        g.drawString(options.getDifficulty().displayName, leftX, modeY);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(200, 200, 220));
        g.drawString(MODE_DESCRIPTIONS[options.getDifficulty().ordinal()], leftX, modeY + 18);

        // 中央：立ち絵を大きく
        cur.getPortraitSprite().draw(g, PANEL_WIDTH / 2.0 - 20, PANEL_HEIGHT / 2.0 - 30, 8);

        // 右側：名前・プロフィール・ステータス
        int rightX = PANEL_WIDTH - 230;
        int infoY = 130;

        // 紹介ラベル(キャラ別)
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setColor(new Color(255, 230, 120));
        String[] subTitles = characterSubtitle(cur);
        g.drawString(subTitles[0], rightX, infoY);
        g.drawString(subTitles[1], rightX, infoY + 16);

        // キャラ名
        g.setFont(new Font("Serif", Font.BOLD, 26));
        g.setColor(new Color(255, 80, 100));
        g.drawString(cur.getDisplayName(), rightX, infoY + 50);
        g.setColor(new Color(255, 80, 100));
        g.drawLine(rightX, infoY + 56, rightX + 180, infoY + 56);

        // ステータス(星表記)
        int[] stats = characterStars(cur);  // [移動速度, 攻撃範囲, 攻撃力]
        String[] statLabels = {"移動速度", "攻撃範囲", "攻撃力"};
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        for (int i = 0; i < statLabels.length; i++) {
            int sy = infoY + 80 + i * 22;
            g.setColor(new Color(220, 220, 240));
            g.drawString(statLabels[i], rightX, sy);
            // 星を描画(★が満、☆が未)
            int starsBase = rightX + 80;
            for (int s = 0; s < 5; s++) {
                if (s < stats[i]) {
                    g.setColor(new Color(255, 220, 100));
                    g.drawString("★", starsBase + s * 14, sy);
                } else {
                    g.setColor(new Color(80, 80, 110));
                    g.drawString("☆", starsBase + s * 14, sy);
                }
            }
        }

        // プロフィール
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(180, 180, 210));
        int profY = infoY + 170;
        for (String line : cur.getProfile().split("\n")) {
            g.drawString(line, rightX, profY);
            profY += 14;
        }

        // 左右選択用の矢印 (キャラが2人以上のとき)
        if (chars.size() > 1) {
            int blink = (int) (Math.sin(System.currentTimeMillis() / 200.0) * 60 + 195);
            g.setFont(new Font("SansSerif", Font.BOLD, 28));
            g.setColor(new Color(255, 220, 120, Math.max(80, Math.min(255, blink))));
            g.drawString("◀", PANEL_WIDTH / 2 - 130, PANEL_HEIGHT / 2 + 10);
            g.drawString("▶", PANEL_WIDTH / 2 + 110, PANEL_HEIGHT / 2 + 10);
        }

        // フッタ操作説明
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(new Color(150, 150, 180));
        String hint = "←→: 選択   Z/Enter: 決定   X/Esc: 戻る";
        fm = g.getFontMetrics();
        g.drawString(hint, (PANEL_WIDTH - fm.stringWidth(hint)) / 2, PANEL_HEIGHT - 18);
    }

    /** キャラごとに紹介サブタイトル(2行)を返す。 */
    private String[] characterSubtitle(PlayerCharacter c) {
        return switch (c.getId()) {
            case "uhyoman"   -> new String[]{"Uhyoの使い手", "(広範囲攻撃型)"};
            case "uhyowoman" -> new String[]{"Uhyo過ぎる魔術師", "(集中攻撃型)"};
            default          -> new String[]{c.getDisplayName(), ""};
        };
    }

    /** キャラごとのスター値 [移動速度, 攻撃範囲, 攻撃力] を返す(1〜5)。 */
    private int[] characterStars(PlayerCharacter c) {
        // 移動速度はgetNormalSpeedから簡易換算(4.0〜6.0想定)
        int speed = (int) Math.max(1, Math.min(5, Math.round(c.getNormalSpeed() - 1.0)));
        return switch (c.getId()) {
            case "uhyoman"   -> new int[]{speed, 4, 3};
            case "uhyowoman" -> new int[]{Math.max(1, speed - 1), 2, 5};
            default          -> new int[]{speed, 3, 3};
        };
    }

    private void renderOptions(Graphics2D g) {
        g.setColor(new Color(15, 4, 26));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawStars(g);
        // 中央の柔らかいライト
        java.awt.RadialGradientPaint rp = new java.awt.RadialGradientPaint(
                PANEL_WIDTH / 2f, PANEL_HEIGHT / 2f, 300f,
                new float[]{0f, 1f},
                new Color[]{new Color(140, 30, 60, 100), new Color(0, 0, 0, 0)});
        java.awt.Paint oldPaint = g.getPaint();
        g.setPaint(rp);
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.setPaint(oldPaint);

        // ヘッダ
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.setColor(new Color(255, 230, 240));
        String title = "Option";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (PANEL_WIDTH - fm.stringWidth(title)) / 2, 50);
        g.setColor(new Color(200, 80, 100));
        g.drawLine(PANEL_WIDTH / 2 - 90, 60, PANEL_WIDTH / 2 + 90, 60);

        // パネル枠(中央)
        int panelX = 90;
        int panelY = 90;
        int panelW = PANEL_WIDTH - 180;
        int panelH = PANEL_HEIGHT - 150;
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(panelX, panelY, panelW, panelH);
        g.setColor(new Color(140, 80, 120));
        g.drawRect(panelX, panelY, panelW, panelH);

        // 項目
        int startY = panelY + 40;
        int rowH = 36;
        for (int i = 0; i < OPTION_LABELS.length; i++) {
            boolean selected = i == optionIndex;
            int y = startY + i * rowH;
            if (selected) {
                g.setColor(new Color(255, 220, 120, 60));
                g.fillRect(panelX + 16, y - 20, panelW - 32, 28);
            }
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.setColor(selected ? new Color(255, 240, 120) : new Color(200, 200, 230));
            g.drawString((selected ? "▶ " : "  ") + OPTION_LABELS[i], panelX + 28, y);

            String value = currentOptionValueText(i);
            if (value != null) {
                g.setFont(new Font("Monospaced", Font.BOLD, 15));
                g.setColor(selected ? new Color(255, 255, 200) : new Color(170, 170, 210));
                // 右寄せ
                int vw = g.getFontMetrics().stringWidth(value);
                g.drawString(value, panelX + panelW - vw - 28, y);
            }
        }

        // フッタ操作説明
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(new Color(150, 150, 180));
        String hint = "↑↓: 項目移動   ←→: 値変更   Z/Enter: 決定   X/Esc: 戻る";
        fm = g.getFontMetrics();
        g.drawString(hint, (PANEL_WIDTH - fm.stringWidth(hint)) / 2, PANEL_HEIGHT - 18);
    }

    private String currentOptionValueText(int i) {
        return switch (i) {
            case 0 -> "< " + options.getDifficulty().displayName + " >";
            case 1 -> "< " + options.getBgmVolume() + " >";
            case 2 -> "< " + options.getSeVolume() + " >";
            case 3 -> "< " + (options.isShowHitbox() ? "ON" : "OFF") + " >";
            default -> null;
        };
    }

    private void renderGame(Graphics2D g) {
        g.setColor(new Color(5, 0, 20));
        g.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        drawStars(g);

        // ステージ別の背景オーバーレイ
        if (currentStage != null) {
            Color tint = currentStage.backgroundTint();
            if (tint != null && tint.getAlpha() > 0) {
                g.setColor(tint);
                g.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
            }
        }

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
            if (input.isDown(KeyEvent.VK_SHIFT) || options.isShowHitbox()) {
                player.drawHitbox(g);
            }
            player.drawBomb(g, FIELD_WIDTH, FIELD_HEIGHT);
        }

        for (Particle p : particles) if (p.active) p.draw(g);

        g.setColor(new Color(80, 60, 120));
        g.drawRect(0, 0, FIELD_WIDTH - 1, FIELD_HEIGHT - 1);

        // 右側HUD
        hud.draw(g, player, boss);

        // ステージ番号表示
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        g.setColor(new Color(255, 220, 100));
        g.drawString("STAGE " + stageManager.getCurrentStage() + "/" + StageManager.TOTAL_STAGES,
                FIELD_WIDTH + 12, FIELD_HEIGHT - 20);
    }

    private void renderPause(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
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

    private void renderGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        g.setFont(new Font("SansSerif", Font.BOLD, 32));
        g.setColor(new Color(255, 80, 80));
        String msg = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (FIELD_WIDTH - fm.stringWidth(msg)) / 2, FIELD_HEIGHT / 2 - 20);

        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(Color.WHITE);
        String s = "Score: " + (player != null ? player.getScore() : 0);
        fm = g.getFontMetrics();
        g.drawString(s, (FIELD_WIDTH - fm.stringWidth(s)) / 2, FIELD_HEIGHT / 2 + 10);

        if (endFrame > 120) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(Color.WHITE);
            String sub2 = "Press ENTER to return to title";
            fm = g.getFontMetrics();
            g.drawString(sub2, (FIELD_WIDTH - fm.stringWidth(sub2)) / 2, FIELD_HEIGHT / 2 + 40);
        }
    }

    private void renderStageClear(Graphics2D g) {
        g.setColor(new Color(0, 0, 30, 200));
        g.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);

        int stageNo = stageManager.getCurrentStage();
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.setColor(new Color(255, 220, 100));
        String msg = "STAGE " + stageNo + " CLEAR!";
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
            String hint = stageNo >= StageManager.TOTAL_STAGES
                    ? "Press ENTER for ending"
                    : "Press ENTER to continue";
            fm = g.getFontMetrics();
            g.drawString(hint, (FIELD_WIDTH - fm.stringWidth(hint)) / 2, FIELD_HEIGHT / 2 + 40);
        }
    }

    private void renderEnding(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

        String[] lines = {
                "～ ENDING ～",
                "",
                "全6ステージを踏破した。",
                "悪の組織アンチうひょは",
                "幻想郷から去っていった…",
                "",
                "(あなたのエンディング演出をここに書く)",
                "",
                "Thanks for playing!"
        };

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(new Color(220, 220, 255));
        int baseY = PANEL_HEIGHT + 20 - endFrame / 3;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            FontMetrics fm = g.getFontMetrics();
            int x = (PANEL_WIDTH - fm.stringWidth(line)) / 2;
            int yy = baseY + i * 28;
            if (yy > -20 && yy < PANEL_HEIGHT + 20) {
                g.drawString(line, x, yy);
            }
        }

        if (endFrame > 600) {
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            g.setColor(new Color(255, 220, 120));
            String s = "Final Score: " + (player != null ? player.getScore() : 0);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(s, (PANEL_WIDTH - fm.stringWidth(s)) / 2, PANEL_HEIGHT / 2 + 60);

            g.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g.setColor(new Color(200, 200, 220));
            String hint = "Press ENTER to return to title";
            fm = g.getFontMetrics();
            g.drawString(hint, (PANEL_WIDTH - fm.stringWidth(hint)) / 2, PANEL_HEIGHT - 30);
        }
    }

    /** 互換用(旧CLEAR状態)。 */
    private void renderClear(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.setColor(new Color(255, 220, 100));
        String msg = "CLEAR!";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (FIELD_WIDTH - fm.stringWidth(msg)) / 2, FIELD_HEIGHT / 2);
    }
}
