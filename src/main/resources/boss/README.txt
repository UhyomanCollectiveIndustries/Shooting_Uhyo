ボス用の画像をここに配置してください。
クラスパスは大文字小文字を区別するので、推奨ファイル名は全て小文字です。

【ファイル名フォーマット】
  顔アイコン(会話用)  : stage{N}_face.png   例: stage1_face.png 〜 stage6_face.png
  全身画像(ゲーム中)   : stage{N}_body.png   例: stage1_body.png 〜 stage6_body.png

  ※ 大文字始まり(Stage1_face.png)、JPG拡張子(stage1_face.jpg) も読み込み候補。
  ※ ファイルが無いと、従来のドット絵/抽象図形にフォールバックします。

【表示サイズの目安】
  顔アイコン: 会話画面で 24px の論理高さ × scale倍 で描画される。
              元画像は 256x256 〜 512x512 程度の正方形〜縦長が綺麗に見える。
  全身画像  : ゲーム中のボス位置に縦96pxに収まるよう拡大縮小。
              元画像は 256x384 などの縦長で良い。透明背景PNG推奨。

【検出順】
  BossArtRegistry.java の candidates(...) を参照。
  stageN_(face|body) → StageN_(face|body) → bossN_(face|body) → BossN_(face|body)
  各々.png/.jpg などの拡張子を順に試して最初に見つかったものを使う。
