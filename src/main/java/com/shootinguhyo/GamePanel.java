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

    // メニュー位置
    private final String[] titleMenu = { "NEW GAME", "OPTION", "QUIT" };
    private int titleMenuIndex = 0;

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

        if (input.isJustPressed(KeyEvent.VK_UP) || input.isJustPressed(KeyEvent.VK_W)) {
            titleMenuIndex = (titleMenuIndex - 1 + titleMenu.length) % titleMenu.length;
            audio.playSe(AudioManager.Se.MENU_MOVE);
        }
        if (input.isJustPressed(KeyEvent.VK_DOWN) || input.isJustPressed(KeyEvent.VK_S)) {
            titleMenuIndex = (titleMenuIndex + 1) % titleMenu.length;
            audio.playSe(AudioManager.Se.MENU_MOVE);
        }
        if (input.isJustPressed(KeyEvent.VK_ENTER) || input.isJustPressed(KeyEvent.VK_Z)) {
            audio.playSe(AudioManager.Se.MENU_SELECT);
            switch (titleMenuIndex) {
                case 0 -> {
                    charSelectIndex = 0;
                    gameState = GameState.CHARACTER_SELECT;
                }
                case 1 -> {
                    optionIndex = 0;
                    gameState = GameState.OPTIONS;
                }
                case 2 -> System.exit(0);
            }
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
            gameState = GameState.TITLE;
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

        player.update(input);
        if (input.isJustPressed(KeyEvent.VK_X)) {
            audio.playSe(AudioManager.Se.BOMB);
        }

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

        player.update(input);
        if (input.isJustPressed(KeyEvent.VK_X)) {
            audio.playSe(AudioManager.Se.BOMB);
        }

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

    // ===================== ENTITY UPDATE HELPERS =====================

    private void updateEnemies() {
        Iterator<Enemy> ei = enemies.iterator();
        while (ei.hasNext()) {
            Enemy e = ei.next();
            e.update();
            enemyBullets.addAll(e.getAndClearNewBullets());

            if (e.isDefeated()) {
                createExplosion((int) e.x, (int) e.y, 12, new Color(180, 100, 255));
                items.add(new Item(e.x, e.y, Item.ItemType.POWER));
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
                items.add(new Item(fe.x, fe.y, Item.ItemType.POWER));
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
                    player.addPower(20);
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
        g.setColor(new Color(5, 0, 20));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawStars(g);

        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        g.setColor(new Color(255, 200, 255));
        String title = "Shooting Uhyo";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (PANEL_WIDTH - fm.stringWidth(title)) / 2, 120);

        // メニュー
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        for (int i = 0; i < titleMenu.length; i++) {
            boolean selected = i == titleMenuIndex;
            g.setColor(selected ? new Color(255, 240, 120) : new Color(180, 180, 220));
            String label = (selected ? "> " : "  ") + titleMenu[i];
            fm = g.getFontMetrics();
            g.drawString(label, (PANEL_WIDTH - fm.stringWidth(label)) / 2, 220 + i * 36);
        }

        // 操作説明
        g.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g.setColor(new Color(150, 150, 200));
        String[] controls = {
            "Up/Down: select   Enter/Z: confirm",
            "Arrow/WASD: Move  Z: Shoot  X: Bomb",
            "Shift: Focus  Esc: Pause"
        };
        int cy = 360;
        for (String ctrl : controls) {
            fm = g.getFontMetrics();
            g.drawString(ctrl, (PANEL_WIDTH - fm.stringWidth(ctrl)) / 2, cy);
            cy += 18;
        }
    }

    private void renderCharacterSelect(Graphics2D g) {
        g.setColor(new Color(5, 0, 20));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawStars(g);

        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.setColor(new Color(255, 220, 255));
        String title = "Select Character";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (PANEL_WIDTH - fm.stringWidth(title)) / 2, 50);

        List<PlayerCharacter> chars = CharacterRegistry.all();
        PlayerCharacter cur = chars.get(charSelectIndex);

        // 立ち絵(4倍)
        cur.getPortraitSprite().draw(g, PANEL_WIDTH / 2.0, PANEL_HEIGHT / 2.0 - 20, 4);

        // 名前
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        fm = g.getFontMetrics();
        g.drawString(cur.getDisplayName(),
                (PANEL_WIDTH - fm.stringWidth(cur.getDisplayName())) / 2, PANEL_HEIGHT - 130);

        // プロフィール
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(200, 200, 220));
        int yLine = PANEL_HEIGHT - 105;
        for (String line : cur.getProfile().split("\n")) {
            fm = g.getFontMetrics();
            g.drawString(line, (PANEL_WIDTH - fm.stringWidth(line)) / 2, yLine);
            yLine += 18;
        }

        // 左右の矢印プロンプト
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.setColor(new Color(255, 240, 120));
        g.drawString("<", 60, PANEL_HEIGHT / 2);
        g.drawString(">", PANEL_WIDTH - 76, PANEL_HEIGHT / 2);

        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(new Color(120, 120, 160));
        String hint = "Left/Right: select   Z: confirm   X: back";
        fm = g.getFontMetrics();
        g.drawString(hint, (PANEL_WIDTH - fm.stringWidth(hint)) / 2, PANEL_HEIGHT - 20);
    }

    private void renderOptions(Graphics2D g) {
        g.setColor(new Color(5, 0, 20));
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        drawStars(g);

        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.setColor(new Color(255, 220, 255));
        String title = "Option";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (PANEL_WIDTH - fm.stringWidth(title)) / 2, 60);

        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        int startY = 130;
        for (int i = 0; i < OPTION_LABELS.length; i++) {
            boolean selected = i == optionIndex;
            g.setColor(selected ? new Color(255, 240, 120) : new Color(180, 180, 220));
            String label = (selected ? "> " : "  ") + OPTION_LABELS[i];
            g.drawString(label, 140, startY + i * 32);
            String value = currentOptionValueText(i);
            if (value != null) {
                g.drawString(value, 320, startY + i * 32);
            }
        }

        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(new Color(120, 120, 160));
        String hint = "Up/Down: move   Left/Right: change   X: back";
        fm = g.getFontMetrics();
        g.drawString(hint, (PANEL_WIDTH - fm.stringWidth(hint)) / 2, PANEL_HEIGHT - 20);
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
