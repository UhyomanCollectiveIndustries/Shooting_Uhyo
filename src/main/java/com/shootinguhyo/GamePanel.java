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

public class GamePanel extends JPanel implements Runnable {
    public static final int FIELD_WIDTH = 384;
    public static final int FIELD_HEIGHT = 448;
    public static final int PANEL_WIDTH = 576;
    public static final int PANEL_HEIGHT = 448;
    private static final int FPS = 60;

    private BufferedImage backBuffer;
    private Graphics2D backG;
    private Thread gameThread;
    private InputHandler input;

    private GameState gameState = GameState.TITLE;
    private GameState prevGameState = GameState.TITLE;

    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<FastEnemy> fastEnemies = new ArrayList<>();
    private List<PlayerBullet> playerBullets = new ArrayList<>();
    private List<EnemyBullet> enemyBullets = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private Boss boss;

    private Stage1 stage1;
    private int stageFrame = 0;
    private boolean bossSpawned = false;

    private HUD hud;
    private SpellCardEffect spellCardEffect;

    private double[] starX = new double[150];
    private double[] starY = new double[150];
    private double[] starSpeed = new double[150];
    private int[] starBrightness = new int[150];

    private Random rand = new Random();

    private int titleFrame = 0;
    private int endFrame = 0;

    private Boss.Phase lastBossPhase = null;

    public GamePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        input = new InputHandler();
        addKeyListener(input);
        setFocusable(true);

        backBuffer = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        backG = backBuffer.createGraphics();
        backG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        hud = new HUD();
        spellCardEffect = new SpellCardEffect();
        initStars();
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

    private void update() {
        switch (gameState) {
            case TITLE -> updateTitle();
            case PLAYING -> updatePlaying();
            case BOSS_FIGHT -> updateBossFight();
            case GAME_OVER -> updateGameOver();
            case CLEAR -> updateClear();
            case PAUSED -> updatePaused();
        }
        input.clearJustPressed();
    }

    private void updateTitle() {
        titleFrame++;
        updateStars();
        if (input.isJustPressed(KeyEvent.VK_ENTER)) {
            initGame();
            gameState = GameState.PLAYING;
        }
    }

    private void initGame() {
        player = new Player(192, 380);
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

    private void updatePlaying() {
        updateStars();
        stageFrame++;

        player.update(input);

        if (input.isJustPressed(KeyEvent.VK_ESCAPE)) {
            prevGameState = gameState;
            gameState = GameState.PAUSED;
            return;
        }

        stage1.update(stageFrame, enemies, fastEnemies);

        for (FastEnemy fe : fastEnemies) {
            fe.setPlayerPosition(player.x, player.y);
        }

        updateEnemies();
        updateBullets();
        updateItems();
        updateParticles();
        spellCardEffect.update();

        if (stage1.isBossTime(stageFrame) && !bossSpawned && enemies.isEmpty() && fastEnemies.isEmpty()) {
            bossSpawned = true;
            boss = new Boss(192, 80);
            gameState = GameState.BOSS_FIGHT;
        }

        hud.update(player.getScore());

        if (!player.isAlive()) {
            gameState = GameState.GAME_OVER;
            endFrame = 0;
        }
    }

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

            Boss.Phase currentPhase = boss.getPhase();
            if (currentPhase != lastBossPhase) {
                lastBossPhase = currentPhase;
                if (currentPhase == Boss.Phase.SPELL1 || currentPhase == Boss.Phase.SPELL2
                        || currentPhase == Boss.Phase.SPELL3) {
                    spellCardEffect.activate(boss.getSpellColor(), boss.getSpellName());
                    player.addScore(10000);
                    enemyBullets.clear();
                } else if (currentPhase == Boss.Phase.DEFEATED) {
                    spellCardEffect.deactivate();
                    player.addScore(10000);
                    enemyBullets.clear();
                    createExplosion((int) boss.x, (int) boss.y, 30, Color.YELLOW);
                }
            }

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

    private void updatePaused() {
        if (input.isJustPressed(KeyEvent.VK_ESCAPE) || input.isJustPressed(KeyEvent.VK_ENTER)) {
            gameState = prevGameState;
        }
    }

    private void updateGameOver() {
        endFrame++;
        updateStars();
        if (endFrame > 120 && input.isJustPressed(KeyEvent.VK_ENTER)) {
            gameState = GameState.TITLE;
        }
    }

    private void updateClear() {
        endFrame++;
        updateStars();
        if (endFrame > 120 && input.isJustPressed(KeyEvent.VK_ENTER)) {
            gameState = GameState.TITLE;
        }
    }

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
                    hit = true;
                    break;
                }
            }
            if (!hit) {
                for (FastEnemy fe : fastEnemies) {
                    if (fe.active && MathUtil.distance(pb.x, pb.y, fe.x, fe.y) < 8) {
                        fe.takeDamage(pb.getDamage());
                        hit = true;
                        break;
                    }
                }
            }
            if (!hit && boss != null && !boss.isDefeated()) {
                if (MathUtil.distance(pb.x, pb.y, boss.x, boss.y) < 30) {
                    boss.takeDamage(pb.getDamage());
                    player.addScore(50);
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
            case PLAYING, BOSS_FIGHT -> renderGame(g);
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
        g.drawString(title, (PANEL_WIDTH - fm.stringWidth(title)) / 2, 150);

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(new Color(200, 150, 255));
        String sub = "";
        fm = g.getFontMetrics();
        g.drawString(sub, (PANEL_WIDTH - fm.stringWidth(sub)) / 2, 185);

        if ((titleFrame / 30) % 2 == 0) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(Color.WHITE);
            String press = "Press ENTER to Start";
            fm = g.getFontMetrics();
            g.drawString(press, (PANEL_WIDTH - fm.stringWidth(press)) / 2, 280);
        }

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

    private void renderGame(Graphics2D g) {
        g.setColor(new Color(5, 0, 20));
        g.fillRect(0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        drawStars(g);

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
            if (input.isDown(KeyEvent.VK_SHIFT)) {
                player.drawHitbox(g);
            }
            player.drawBomb(g, FIELD_WIDTH, FIELD_HEIGHT);
        }

        for (Particle p : particles) if (p.active) p.draw(g);

        g.setColor(new Color(80, 60, 120));
        g.drawRect(0, 0, FIELD_WIDTH - 1, FIELD_HEIGHT - 1);

        hud.draw(g, player, boss);
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
        if (endFrame > 120) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g.setColor(Color.WHITE);
            String sub2 = "Press ENTER to return to title";
            fm = g.getFontMetrics();
            g.drawString(sub2, (FIELD_WIDTH - fm.stringWidth(sub2)) / 2, FIELD_HEIGHT / 2 + 20);
        }
    }

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
