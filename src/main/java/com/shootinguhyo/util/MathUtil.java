package com.shootinguhyo.util;

/**
 * MathUtil：ゲーム内でよく使う数学計算をまとめたユーティリティ。
 *
 * 【役割】
 *  - 「点Aから点Bへの角度」「点間の距離」「値を範囲内に収める」を1か所にまとめる
 *  - どこからでも使える静的メソッドにすることで、コード重複を減らす
 *
 * 【なぜutilパッケージに置くか】
 *  特定の機能(ゲームロジック)に属さない汎用処理は util にまとめておくと、
 *  別プロジェクトに持っていきやすい。再利用性を意識した構成。
 */
public class MathUtil {
    /**
     * 点(fromX,fromY)から点(toX,toY)への角度(ラジアン)を返す。
     *
     * 【atan2の使い分け】
     *  atan(y/x) は引数の符号情報が消えて2象限分しか区別できないが、
     *  atan2(y, x) はx,yの両方を見るので4象限すべて区別できる。
     *  「相対座標から方向を求めたい」場合は必ずatan2を使う。
     */
    public static double angle(double fromX, double fromY, double toX, double toY) {
        return Math.atan2(toY - fromY, toX - fromX);
    }

    /**
     * 2点間の直線距離を返す。
     *
     * 【Math.hypotを使う理由】
     *  Math.sqrt(dx*dx + dy*dy) より「桁あふれを起こしにくい」高精度な実装になっている。
     *  弾幕シューティングのように毎フレーム多数の距離計算を行う場合、精度は重要。
     */
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x2 - x1, y2 - y1);
    }

    /**
     * 値vを[min, max]の範囲に丸める。
     * 例：clamp(150, 0, 100) → 100
     *     clamp(-10, 0, 100) → 0
     *
     * プレイヤーが画面外に飛び出さないようにする時などに使う。
     */
    public static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
