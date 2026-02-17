/* バックグラウンドで実行されるコード */
/* このコードは@capacitor/background-runnerによって15分間隔で実行されます */

// Capacitorプラグインを使用して位置情報を取得し、サーバーに送信
addEventListener('backgroundTask', async (event) => {
  try {
    console.log('Background task started');

    // 位置情報を取得
    const position = await CapacitorGeolocation.getCurrentPosition({
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    });

    console.log('Position obtained:', position.coords.latitude, position.coords.longitude);

    // サーバーに送信
    const response = await fetch('https://nomadpulse.enoki.xyz/php/update_location.php', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        lat: position.coords.latitude,
        lng: position.coords.longitude,
        timestamp: position.timestamp
      })
    });

    const result = await response.json();
    console.log('Location sent to server:', result);

  } catch (error) {
    console.error('Background location error:', error);
  }
});

