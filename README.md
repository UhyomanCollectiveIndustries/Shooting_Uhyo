# README  
## UhyomanCollectiveIndustries  
### Shooting_Uhyo  

このゲームはUhyomanCollectiveIndustriesの「Shooting_Uhyo」です。  
### ゲームの説明  
このゲームは、プレイヤーがウヒョを操作して、敵を撃ち倒すシューティングゲームです。  
### 操作方法  
- 矢印キー/WASDでウヒョを移動させます。  
- zキーで弾を撃ちます。  
- xキーでボムを使用します。  
- Shiftで低速移動(当たり判定表示)。  
- ESCでポーズ。  
### ゲームの目的  
すべてのステージで敵を倒し、悪の組織「アンチうひょ組織のラスボスであるちくしょー」を倒すことです。  
### 注意事項  
- javaがインストールされている必要があります。  
- ゲームはWindows、Mac、Linuxでプレイできます。  
- ゲームのバグや不具合があった場合は、開発者に報告してください。  
### 開発者情報  
- 開発者: UhyomanCollectiveIndustries/Me1td0wn76  

### how to play & build  
1. Clone the repository:  
   ```
   git clone https://github.com/UhyomanCollectiveIndustries/Shooting_Uhyo  
    ```
2. Navigate to the project directory:  
   ```
   cd Shooting_Uhyo
    ```
3. build the project useing maven:
    ```
    mvn clean package
     ```
4. Run the game:
    ```
    java -jar target/Shooting_Uhyo.jar
     ```


## 未実装なもの  
- 背景・自機・敵・ボスのグラフィックのドット絵作り込み(現在は仮スプライト/図形描画)  
- ステージのBGMの実音源化(現在は AudioManager フックのみで音は鳴らない)  
- ステージのSEの実音源化(現在は AudioManager フックのみで音は鳴らない)  
- ボス専用立ち絵の差し替え(現在はプレイヤー立ち絵を流用)  
- ボスとの会話のステージ別シナリオ作り込み(現在は共通テンプレ)  
- エンディングのキャラ別分岐(現在は共通テキスト)  
- BossのHPバー閾値による必殺技変化  
- Bossキャラクターのアニメーション(SpriteAnimation の活用)  
- ステージ背景のパララックス強化(BackgroundLayer の複数レイヤー化)  
- セーブ/ロード(GameOptions.save/load の実装)  
- コンテニューができるようにする  

リプレイ/スコア/ミュージックルーム/エクストラは UI のみで、押下時に「未実装です」と表示するに留めています。  
## 実装済みなもの  
- 十字キー/WASDでの自機の移動  
- zキーでの弾の発射、xキーでのボム  
- Shiftでの低速移動 + 当たり判定表示  
- タイトル画面(NEW GAME / OPTION / QUIT のメニュー)  
- キャラクター選択(Uhyoman / Uhyowoman)  
- キャラクターごとの攻撃方法の違い(扇状/前方集中)  
- オプション画面(難易度・BGM音量・SE音量・ヒットボックス表示)  
- 難易度(EASY/NORMAL/HARD/LUNATIC)による敵HP・敵弾速度のスケーリング  
- 6ステージ進行(StageManager による管理)  
- ステージ別の背景オーバーレイ色(夜空/朝焼け/夕暮れ/夜の森/雷雨/宇宙)  
- ボス戦前/後の会話シーン(タイプライター演出)  
- ボスの3段スペルカード + HPバー  
- ボス撃破後の演出(BossDefeatEffect: 閃光 + 同心円波紋)  
- ステージクリア画面 / エンディング画面(スクロール) / ゲームオーバー画面  
- スコアボーナス(節目で1UP・ボム+1)  
- 敵バリエーション(通常/高速/シューター/ヘビー/スナイパー/タンク)  
- 弾幕パターン(全方位/自機狙い/波/螺旋)  
- BGM/SE フック(AudioManager) — 出力先の実音源は今後差し替え  
