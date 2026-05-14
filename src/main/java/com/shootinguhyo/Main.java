package com.shootinguhyo;

/**
 * Mainクラス：プログラムを起動する最初の入口。
 *
 * 【役割】
 *  Javaで作るすべてのアプリは「mainメソッド」から始まる。
 *  ここではゲーム用のウィンドウ(GameWindow)を作って画面に表示するだけ。
 *
 * 【なぜこの作りか】
 *  - Swingという画面ライブラリを使ってGUI(画面付き)アプリを作っている。
 *  - SwingはGUI処理を「EDT（Event Dispatch Thread）」という専用スレッドで動かすのが安全。
 *    違うスレッドから画面を操作すると表示が乱れたり例外が出る可能性があるため。
 *  - そのためSwingUtilities.invokeLater(...)でEDT上にウィンドウ生成処理を依頼している。
 */
public class Main {
    /**
     * プログラム開始時にJavaから自動で呼ばれるメソッド。
     * argsはコマンドラインから渡される引数（今回は使っていない）。
     */
    public static void main(String[] args) {
        // invokeLaterに「あとでEDT上で実行してね」と処理を渡す。
        // ラムダ式（() -> { ... }）で「実行したい処理」を書く。
        javax.swing.SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(); // ゲームウィンドウ本体を作成
            window.setVisible(true);              // 画面に表示する
        });
    }
}
