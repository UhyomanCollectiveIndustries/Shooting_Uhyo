package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

public class Stage1 implements Stage {
    private static final int BOSS_FRAME = 900;

    @Override
    public void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies) {
        switch (frame) {
            case 120 -> spawnEnemyRow(enemies, 3);
            case 240 -> spawnFastEnemies(fastEnemies, 2);
            case 360 -> spawnEnemyDiamond(enemies, 4);
            case 480 -> spawnFastEnemies(fastEnemies, 4);
            case 600 -> spawnEnemyRow(enemies, 5);
            case 720 -> spawnFastEnemies(fastEnemies, 3);
        }
    }

    private void spawnEnemyRow(List<Enemy> enemies, int count) {
        for (int i = 0; i < count; i++) {
            double x = 60.0 + (260.0 / (count + 1)) * (i + 1);
            enemies.add(new Enemy(x, -20, 30, 200));
        }
    }

    private void spawnEnemyDiamond(List<Enemy> enemies, int count) {
        double[][] positions = {
            {192, -20}, {130, -60}, {254, -60}, {192, -100}
        };
        for (int i = 0; i < Math.min(count, positions.length); i++) {
            enemies.add(new Enemy(positions[i][0], positions[i][1], 30, 200));
        }
    }

    private void spawnFastEnemies(List<FastEnemy> fastEnemies, int count) {
        for (int i = 0; i < count; i++) {
            boolean fromLeft = i % 2 == 0;
            double x = fromLeft ? -20 : 404;
            double y = 80 + (120.0 / count) * i;
            double vx = fromLeft ? 3.0 : -3.0;
            double vy = 1.5;
            fastEnemies.add(new FastEnemy(x, y, vx, vy));
        }
    }

    @Override
    public boolean isBossTime(int frame) { return frame >= BOSS_FRAME; }

    @Override
    public boolean isComplete(int frame) { return frame > BOSS_FRAME + 300; }
}
