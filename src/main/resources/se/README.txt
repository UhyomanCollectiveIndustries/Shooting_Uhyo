SE (Sound Effect) を .wav 形式でここに配置してください。

【必要なファイル一覧】
  shoot.wav          自機弾の発射音
  enemy_hit.wav      敵に弾が当たった音
  explosion.wav      敵撃破時の爆発音
  player_hit.wav     自機被弾音
  bomb.wav           ボム発動音
  item.wav           アイテム取得音
  spell_declare.wav  スペルカード宣言時の音
  spell_get.wav      スペルカード取得(クリア)音
  menu_move.wav      メニュー項目移動音
  menu_select.wav    メニュー決定音

【形式】
  - 推奨: 16bit PCM WAV / 44.1kHz / モノラル
  - 短い音(0.1〜1秒)推奨。Clipにロードしてキャッシュします
  - 同名SEを連続再生すると、再生中のClipは止めて頭から鳴らし直されます
    (完全な多重発音には対応していません)

【ファイルが見つからない場合】
  最初の再生時に「[SE] file not found: /se/xxx.wav」とログが出て、
  以後はそのSEを無音でスキップします。クラッシュはしません。
