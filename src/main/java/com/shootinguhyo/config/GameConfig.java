package com.shootinguhyo.config;

import com.shootinguhyo.character.PlayerCharacter;

/**
 * GameConfig：1プレイ分の設定をまとめるクラス。
 *
 * 【役割】
 *  プレイ開始時に「選んだキャラ」「難易度」「初期残機数」などを保持。
 *  GamePanelやステージにこれを渡せば、設定に応じたゲームが始まる。
 *
 * 【なぜGameOptionsと分けるか】
 *  - GameOptions: ゲームをまたいで残る設定(音量など)
 *  - GameConfig : 今回のプレイだけ有効な情報(選択キャラなど)
 */
public class GameConfig {
    private PlayerCharacter character;
    private Difficulty difficulty = Difficulty.NORMAL;
    private int startingLives = 3;
    private int startingBombs = 3;

    public PlayerCharacter getCharacter() { return character; }
    public void setCharacter(PlayerCharacter character) { this.character = character; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        // ゲーム本体(Enemy/EnemyBullet/Boss)から参照される静的な現在値も同期する
        Difficulty.setCurrent(difficulty);
    }

    public int getStartingLives() { return startingLives; }
    public void setStartingLives(int startingLives) { this.startingLives = startingLives; }

    public int getStartingBombs() { return startingBombs; }
    public void setStartingBombs(int startingBombs) { this.startingBombs = startingBombs; }
}
