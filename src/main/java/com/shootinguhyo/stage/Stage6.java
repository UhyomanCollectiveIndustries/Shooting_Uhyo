package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import com.shootinguhyo.entity.bullet.EnemyBullet;
import com.shootinguhyo.pattern.ExpandingRingPattern;
import com.shootinguhyo.pattern.SpiralPattern;
import com.shootinguhyo.pattern.SwitchbackPattern;
import com.shootinguhyo.pattern.WinderPattern;

import java.awt.Color;
import java.util.List;

/**
 * Stage6：最終ステージのひな型。約3分。
 * 雑魚は控えめ、決戦への前奏として徐々に密度UP。
 */
public class Stage6 implements Stage {
    private static final int BOSS_FRAME = 10800;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        // 序盤(0〜2400) — しっとり目: 単発の中央エンcounter(スパイラル)
        if (frame > 0 && frame < 2400 && frame % 400 == 0) {
            Enemy mid = new Enemy(192, -20, 400, 500);
            mid.withPattern(new SpiralPattern(0.20, 6, 2.0))
               .withInterval(18);
            enemies.add(mid);
        }
        // 中盤(2400〜7200) — 全種混合
        if (frame >= 2400 && frame < 7200 && (frame - 2400) % 300 == 0) {
            int phase = ((frame - 2400) / 300) % 4;
            switch (phase) {
                case 0 -> { // ワインダー
                    Enemy w = new Enemy(192, -20, 400, 500);
                    w.withPattern(WinderPattern.downward(50, 2.8),
                            new Color(120, 220, 255), EnemyBullet.BulletSize.SMALL)
                     .withInterval(30);
                    enemies.add(w);
                }
                case 1 -> { // 切り返し
                    Enemy s = new Enemy(192, -20, 400, 500);
                    s.withPattern(SwitchbackPattern.standard(2.7),
                            new Color(255, 220, 100), EnemyBullet.BulletSize.SMALL)
                     .withInterval(22);
                    enemies.add(s);
                }
                case 2 -> { // 拡大円(青CW＋赤CCW)
                    Enemy r = new Enemy(192, -20, 450, 600);
                    r.withPattern(ExpandingRingPattern.standard(), null, EnemyBullet.BulletSize.MEDIUM)
                     .withInterval(28);
                    enemies.add(r);
                }
                case 3 -> { // スパイラル
                    Enemy sp = new Enemy(192, -20, 400, 500);
                    sp.withPattern(new SpiralPattern(0.22, 8, 2.2))
                      .withInterval(15);
                    enemies.add(sp);
                }
            }
            fastEnemies.add(new FastEnemy(-20, 80, 3.5, 1.5));
            fastEnemies.add(new FastEnemy(404, 80, -3.5, 1.5));
        }
        // 終盤(7200〜) — 同時多発: 左ワインダー＋中央拡大円＋右切り返し
        if (frame >= 7200 && frame < BOSS_FRAME && (frame - 7200) % 220 == 0) {
            Enemy left = new Enemy(90, -20, 400, 500);
            left.withPattern(WinderPattern.downward(50, 2.8))
                .withInterval(36);
            enemies.add(left);

            Enemy center = new Enemy(192, -20, 450, 600);
            center.withPattern(ExpandingRingPattern.standard(), null, EnemyBullet.BulletSize.MEDIUM)
                  .withInterval(32);
            enemies.add(center);

            Enemy right = new Enemy(294, -20, 400, 500);
            right.withPattern(SwitchbackPattern.standard(2.8))
                 .withInterval(26);
            enemies.add(right);

            fastEnemies.add(new FastEnemy(-20, 80, 3.5, 1.5));
            fastEnemies.add(new FastEnemy(404, 80, -3.5, 1.5));
        }
    }

    @Override
    public int midBossFrame() { return 5400; }

    @Override
    public boolean isBossTime(int frame) { return frame >= BOSS_FRAME; }

    @Override
    public boolean isComplete(int frame) { return frame > BOSS_FRAME + 600; }

    /** 宇宙風の濃紫色(ラスボス感)。 */
    @Override
    public java.awt.Color backgroundTint() { return new java.awt.Color(30, 0, 60, 100); }
}
