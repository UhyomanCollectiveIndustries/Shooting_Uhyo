package com.shootinguhyo;

import javax.swing.*;

/**
 * GameWindowクラス：ゲーム用のウィンドウ枠を作るクラス。
 *
 * 【役割】
 *  Windowsなどで見る「タイトルバー＋閉じるボタン付きの窓」を作る。
 *  その中にゲーム描画領域（GamePanel）を貼り付けて、ゲームを開始する。
 *
 * 【なぜJFrameを継承しているのか】
 *  - JFrameはSwingでウィンドウを作るための「土台」になるクラス。
 *  - 継承することで「タイトル設定」「閉じる処理」などのメソッドをそのまま使える。
 */
public class GameWindow extends JFrame {

    /**
     * コンストラクタ：このクラスがnewされたときに最初に呼ばれる処理。
     * ウィンドウの設定とゲームパネルの組み込みをここで行う。
     */
    public GameWindow() {
        // ウィンドウ上部に表示するタイトル文字
        // 文字列リテラルでは日本語をそのまま書ける（ソースファイルの文字コードに依存）
        setTitle("Shooting Uhyo - 幻想の妖精");

        // ウィンドウの「×」ボタンを押したらアプリ全体を終了する設定
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // サイズ変更を禁止（ゲーム画面なので拡大縮小されない方がレイアウトが崩れない）
        setResizable(false);

        // ゲーム本体（描画と更新を担当するパネル）を作って組み込む
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        // GamePanelが希望するサイズに合わせてウィンドウサイズを自動調整
        pack();

        // ウィンドウを画面中央に表示
        setLocationRelativeTo(null);

        // ゲームのメインループを開始（別スレッドで動き始める）
        gamePanel.startGame();
    }
}
