package com.shootinguhyo;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * InputHandler：キーボード入力を受け取って、ゲーム側から「今このキーが押されてる？」を
 * 問い合わせできるようにするクラス。
 *
 * 【役割】
 *  Swingでは、キーを押すと「キーが押された」というイベントが発生する。
 *  ただし、ゲームでは「今このキー押しっぱなしか？」をフレームごとに何度も確認したい。
 *  イベント発生のタイミングに頼ると不便なので、状態（押されているキー集合）として持っておく。
 *
 * 【なぜKeyAdapterを継承するか】
 *  - KeyAdapterはキー入力イベントを受け取るための便利な雛形クラス。
 *  - 必要なメソッド(keyPressed/keyReleased)だけオーバーライドすれば良い。
 *
 * 【pressedKeys と justPressedKeys を使い分ける理由】
 *  - pressedKeys     : 「今押されているか？」 …連射(押しっぱなし)判定に使う(ショット等)
 *  - justPressedKeys : 「このフレームで初めて押されたか？」 …単発判定(ボム、ポーズ切替等)
 *  → 押しっぱなしで連発したくないアクションを区別するため2種類用意している。
 */
public class InputHandler extends KeyAdapter {
    // Setを使うのは「同じキーを重複登録せず」「含むかどうか」を高速に確認できるため
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Set<Integer> justPressedKeys = new HashSet<>();

    /** 指定キーが今押されているか（押しっぱなし含む） */
    public boolean isDown(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    /** 指定キーが「このフレームで初めて押されたか」 */
    public boolean isJustPressed(int keyCode) {
        return justPressedKeys.contains(keyCode);
    }

    /**
     * 「初めて押された」情報は1フレームだけ有効。
     * 毎フレームの終わりに呼び出してクリアする必要がある。
     */
    public void clearJustPressed() {
        justPressedKeys.clear();
    }

    /**
     * キーが押された瞬間にSwingから呼ばれるメソッド。
     * すでに押されていない＝今このフレームで初めて押された、と判定できる。
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (!pressedKeys.contains(code)) {
            justPressedKeys.add(code);
        }
        pressedKeys.add(code);
    }

    /** キーが離されたらpressedKeysから削除する。 */
    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }
}
