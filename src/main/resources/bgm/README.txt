BGM (Background Music) を .wav 形式でここに配置してください。

【必要なファイル一覧】
  title.wav         タイトル/メニュー画面
  stage1.wav        ステージ1 道中
  stage1_boss.wav   ステージ1 ボス戦
  stage2.wav        ステージ2 道中
  stage2_boss.wav   ステージ2 ボス戦
  stage3.wav
  stage3_boss.wav
  stage4.wav
  stage4_boss.wav
  stage5.wav
  stage5_boss.wav
  stage6.wav
  stage6_boss.wav
  ending.wav        エンディング
  gameover.wav      ゲームオーバー

【形式】
  - 推奨: 16bit PCM WAV / 44.1kHz / モノラル or ステレオ
  - javax.sound.sampled の Clip でループ再生されます
  - ループ点を指定したい場合は、ファイル自体をループしやすい長さで作成してください
    (Clipは末端到達でループ先頭に戻る単純ループです)

【サイズ目安】
  Clipはメモリに全部読み込むため、1曲 ~5MB を目安にしてください。
  長い曲が必要な場合はビットレートを下げるか、外部ライブラリ(JOAL等)に切り替えてください。

【ファイルが見つからない場合】
  起動時に「[BGM] file not found: /bgm/xxx.wav」とログが出て、無音で続行します。
