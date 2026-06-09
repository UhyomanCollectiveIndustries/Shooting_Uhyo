SE (Sound Effect) をここに配置してください。
.wav と .mp3 の両方に対応しています。

【必要なファイル一覧】(拡張子は .wav または .mp3)
  shoot              自機弾の発射音
  enemy_hit          敵に弾が当たった音
  explosion          敵撃破時の爆発音
  player_hit         自機被弾音
  bomb               ボム発動音
  item               アイテム取得音
  spell_declare      スペルカード宣言時の音
  spell_get          スペルカード取得(クリア)音
  menu_move          メニュー項目移動音
  menu_select        メニュー決定音

【探索順】
  SePlayer は同名のファイルを以下の順で探します:
    1. /se/{name}.wav
    2. /se/{name}.mp3
    3. /se/{name}.WAV
    4. /se/{name}.MP3
  最初に見つかったものを使用します。

【形式】
  - WAV: 16bit PCM / 44.1kHz / モノラル 推奨
  - MP3: PCMデコード後にClipへロードします。短いSEなら容量増もごく僅か
  - 短い音(0.05〜1秒)推奨。連射に耐えるよう setFramePosition(0); start() で頭出し
  - 同じSEを連続再生すると、再生中のClipは止めて頭から鳴らし直されます
    (完全な多重発音には対応していません)

【ファイルが見つからない場合】
  最初の再生時に「[SE] file not found (.wav/.mp3): /se/xxx」とログが出て、
  以後はそのSEを無音でスキップします。クラッシュはしません。
