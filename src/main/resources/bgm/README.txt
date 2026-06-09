BGM (Background Music) をここに配置してください。
.wav と .mp3 の両方に対応しています。

【必要なファイル一覧】(拡張子は .wav または .mp3)
  title              タイトル/メニュー画面
  stage1             ステージ1 道中
  stage1_boss        ステージ1 ボス戦
  stage2             ステージ2 道中
  stage2_boss        ステージ2 ボス戦
  stage3
  stage3_boss
  stage4
  stage4_boss
  stage5
  stage5_boss
  stage6
  stage6_boss
  ending             エンディング
  gameover           ゲームオーバー

【探索順】
  BgmPlayer は同名のファイルを以下の順で探します:
    1. /bgm/{name}.wav
    2. /bgm/{name}.mp3
    3. /bgm/{name}.WAV
    4. /bgm/{name}.MP3
  最初に見つかったものを使用します。

【形式】
  - WAV: 16bit PCM / 44.1kHz / モノラル or ステレオ 推奨
  - MP3: 通常のCBR/VBR どちらも可。内部で PCM_SIGNED 16bit にデコードしてから Clip にロードします
  - ループ再生は単純なファイル末→先頭ループです(ループ点指定不可)

【サイズ目安】
  Clipはメモリ上に展開するため、デコード後で 1曲 ~10MB 程度を目安に。
  MP3 は 3〜5分の曲が ~3〜5MB だが、PCM デコード後は ~30MB 超になることもある点に注意。
  長尺BGMは MP3 で短めにループする曲を用意するのが現実的。

【ファイルが見つからない場合】
  起動時に「[BGM] file not found (.wav/.mp3): /bgm/xxx」とログが出て、
  無音で続行します(クラッシュしません)。
