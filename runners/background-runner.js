/* バックグラウンドで実行されるコード */
/* このコードは@capacitor/background-runnerによって15分間隔で実行されます */

// Capacitorプラグインを使用して位置情報を取得し、サーバーに送信
// 注: background-runnerコンテキストでは、ネイティブ側（LocationForegroundService）が
// 実際の位置情報取得とサーバー送信を担当します
// このファイルは将来的な拡張のために残されています

addEventListener('backgroundTask', async (event) => {
  try {
    console.log('Background task executed - location updates handled by native service');
    // ネイティブサービス（LocationForegroundService）が位置情報の取得と送信を処理
  } catch (error) {
    console.error('Background task error:', error);
  }
});

