package com.shootinguhyo.stage;

import com.shootinguhyo.entity.Enemy;
import com.shootinguhyo.entity.FastEnemy;
import java.util.List;

public interface Stage {
    void update(int frame, List<Enemy> enemies, List<FastEnemy> fastEnemies);
    boolean isBossTime(int frame);
    boolean isComplete(int frame);
}
