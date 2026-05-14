package com.shootinguhyo.stage;

/**
 * StageManager：6ステージの進行を管理する。
 *
 * 【役割】
 *  - 現在のステージ番号を覚える
 *  - クリアごとに次のステージへ進める
 *  - 番号からStageインスタンスを生成して返す
 *
 * 【ひな型のため】
 *  - GamePanel側がStage1だけ直接持っている現状は変えていない
 *  - 将来、GamePanelから本クラス経由でステージ進行を管理する想定
 *
 * 【TODO】
 *  - 各ステージの背景・BGM情報の保持
 *  - エクストラステージ(クリア後解放)の対応
 */
public class StageManager {

    /** ステージ総数。 */
    public static final int TOTAL_STAGES = 6;

    private int currentStage = 1;

    public int getCurrentStage() { return currentStage; }

    /** 次のステージへ進める。最終ステージなら true を返さない(=エンディングへ)。 */
    public boolean advance() {
        if (currentStage >= TOTAL_STAGES) return false;
        currentStage++;
        return true;
    }

    public void reset() {
        currentStage = 1;
    }

    /** 現在のステージ番号に対応する Stage インスタンスを生成する。 */
    public Stage createCurrentStage() {
        return createStage(currentStage);
    }

    /** 番号からステージを生成。1〜6以外は Stage1 にフォールバック。 */
    public static Stage createStage(int number) {
        return switch (number) {
            case 1 -> new Stage1();
            case 2 -> new Stage2();
            case 3 -> new Stage3();
            case 4 -> new Stage4();
            case 5 -> new Stage5();
            case 6 -> new Stage6();
            default -> new Stage1();
        };
    }
}
