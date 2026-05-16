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
   ```cmd
   git clone https://github.com/UhyomanCollectiveIndustries/Shooting_Uhyo
   ```
2. Navigate to the project directory:  
   ```cmd
   cd Shooting_Uhyo
   ```
3. Build the project using maven:  
   ```cmd
   mvn clean package
   ```
4. Run the game:  
   ```cmd
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

---

## ソースコード内 TODO 一覧

### 音声 (audio/)
| ファイル | TODO |
|---|---|
| `BgmPlayer.java` | 実際の音源ファイルの読み込みと再生 |
| `BgmPlayer.java` | フェードアウト処理 (`fadeOut`) |
| `BgmPlayer.java` | 音量変更の再生中への即時反映 (`setVolume`) |
| `SePlayer.java` | 効果音の実際の再生処理 |
| `AudioManager.java` | ステージ番号 → BGMファイル名マッピング |
| `AudioManager.java` | シーン別(タイトル/ボス戦/エンディング)BGM切替 |
| `SeKeys.java` | SE定数キーの整備 |

### 設定 (config/)
| ファイル | TODO |
|---|---|
| `GameOptions.java` | `save()`: Propertiesファイルへの書き出し |
| `GameOptions.java` | `load()`: Propertiesファイルからの読み込み |
| `GameOptions.java` | キーバインド変更機能 |
| `GameOptions.java` | フルスクリーン切替 |
| `GameOptions.java` | 弾密度を下げるなどのアクセシビリティ設定 |
| `Difficulty.java` | (クラスレベルTODO — 内容を確認して整備) |

### キャラクター (character/)
| ファイル | TODO |
|---|---|
| `Uhyoman.java` | 16×16ドット絵の作り込み(現在は仮) |
| `Uhyoman.java` | 立ち絵の大きなドット絵を用意(現在は仮) |
| `Uhyoman.java` | パワーに応じたホーミング弾の追加 |
| `Uhyowoman.java` | パワー満タン時のレーザー発射の実装 |
| `PlayerCharacter.java` | キャラ固有ボム演出の実装 |
| `Uhyoman.java` | キャラクターの攻撃力の調整 |

### グラフィックス (graphics/)
| ファイル | TODO |
|---|---|
| `BossSpriteSet.java` | ボス専用スプライトセットの実装 |
| `SpriteAnimation.java` | アニメーション機能の実装・活用 |
| `Sprites.java` | 仮スプライトを差分のある正式なドット絵に差し替え |

### ダイアログ (dialog/)
| ファイル | TODO |
|---|---|
| `DialogSamples.java` | ボス専用立ち絵への差し替え(現在はプレイヤー立ち絵を流用) |
| `DialogSamples.java` | ステージ別シナリオの作り込み(現在は共通テンプレート) |
| `DialogScene.java` | (クラスレベルTODO — 実装内容を整備) |

### ステージ (stage/)
| ファイル | TODO |
|---|---|
| `Stage2.java` | 中ボス出現とフェーズ管理 |
| `Stage2.java` | 専用BGM/背景設定 |
| `Stage2.java` | ステージ専用ボスの追加 |
| `Stage3.java` | 専用パターン弾を撃つシューター型雑魚の追加 |
| `Stage3.java` | 専用ボスとスペルカードの追加 |
| `Stage3.java` | 背景「夕暮れの空」の実装 |
| `Stage3.java` | より複雑なスポーンパターンの実装 |
| `Stage4.java` | 敵HPと攻撃頻度の上方修正 |
| `Stage4.java` | 中ボスフェーズの追加 |
| `Stage4.java` | 専用ボスの追加 |
| `Stage4.java` | 背景「夜の森」の実装 |
| `StageManager.java` | 各ステージの背景・BGM情報の保持 |
| `StageManager.java` | エクストラステージ(クリア後解放)の対応 |

### 画面遷移 (screen/)
| ファイル | TODO |
|---|---|
| `TitleScreen.java` | GamePanelの `renderTitle`/`updateTitle` との統合(現在は二重管理) |
| `TitleScreen.java` | BGM再生(`BgmPlayer.play("title")`)の呼び出し |
| `TitleScreen.java` | QUITでのアプリ終了処理 |
| `CharacterSelectScreen.java` | 立ち絵を大きく表示する |
| `CharacterSelectScreen.java` | キャラ説明テキストの改行対応 |
| `CharacterSelectScreen.java` | プレイ画面(GamePanel)への遷移処理の統合 |
| `StageClearScreen.java` | ボーナス計算(残機・ボム・グレイズ) |
| `StageClearScreen.java` | 次ステージへの正しい遷移処理(現在はタイトルに戻る) |
| `StageClearScreen.java` | クリア演出(勝利モーション・SE) |
| `OptionScreen.java` | キーバインド変更機能 |
| `OptionScreen.java` | 全画面切替 |
| `OptionScreen.java` | 設定の即時保存(`GameOptions.save()`) |
| `EndingScreen.java` | BGM再生(`BgmPlayer.play("ending")`)の呼び出し |
| `EndingScreen.java` | キャラ別エンディング分岐 |
| `GameOverScreen.java` | BGM再生(`BgmPlayer.play("gameover")`)の呼び出し |

### エフェクト (effect/)
| ファイル | TODO |
|---|---|
| `BackgroundAnimator.java` | バックグラウンドアニメーション実装 |
| `BossDefeatEffect.java` | ボス撃破エフェクトの拡充 |

### エンティティ (entity/)
| ファイル | TODO |
|---|---|
| `HeavyEnemy.java` | クラスのTODO確認・実装 |
| `ShooterEnemy.java` | クラスのTODO確認・実装 |
| `SniperEnemy.java` | クラスのTODO確認・実装 |
| `TankEnemy.java` | クラスのTODO確認・実装 |

### スコア (score/)
| ファイル | TODO |
|---|---|
| `ScoreBonusManager.java` | 「Extend!」テキストの画面表示と効果音(`SeKeys.EXTEND`) |
| `ScoreBonusManager.java` | ボム獲得演出と効果音の追加 |

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
