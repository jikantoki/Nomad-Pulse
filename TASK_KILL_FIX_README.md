# Androidタスクキル後のバックグラウンド位置情報更新の修正

## 問題の概要

Androidアプリでタスクキルされた後、バックグラウンドでの位置情報更新が動作していませんでした。

## 原因

従来の実装では、`onTaskRemoved()`メソッドで直接サービスを再起動しようとしていましたが、Android 8.0以降ではこの方法が制限されています。また、以下の問題がありました：

1. サービスが正しくクリーンアップされていない（`stopSelf()`が呼ばれていない）
2. Android 6.0以降のDozeモード対応が不十分
3. ログが不足しており、デバッグが困難

## 修正内容

### 1. LocationForegroundService.java

#### onTaskRemoved()メソッドの改善
- **`AlarmManager.setExactAndAllowWhileIdle()`を使用**: Android 6.0以降（API 23+）で、Dozeモード中でもアラームが確実に発火するようにしました
- **`stopSelf()`の追加**: サービスを適切にクリーンアップしてから再起動するようにしました
- **ログの追加**: デバッグを容易にするためのログを追加しました
- **`PendingIntent.FLAG_IMMUTABLE`の使用**: Android 12以降のセキュリティ要件に対応しました

```java
@Override
public void onTaskRemoved(Intent rootIntent) {
    super.onTaskRemoved(rootIntent);
    Log.d(TAG, "Task removed, scheduling restart");
    
    // AlarmManagerを使用して1秒後に再起動をスケジュール
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(...);
    } else {
        alarmManager.set(...);
    }
    
    // サービスを適切にクリーンアップ
    stopSelf();
}
```

#### onDestroy()メソッドの改善
- **AlarmManagerを優先**: ブロードキャストではなく、AlarmManagerを使用してより確実に再起動をスケジュールします
- **フォールバック処理の追加**: AlarmManagerが利用できない場合や`SecurityException`が発生した場合のフォールバック処理を追加しました
- **異なるrequest code**: `onTaskRemoved()`と`onDestroy()`で異なるrequest codeを使用して、重複を防ぎます

### 2. ServiceRestartReceiver.java

#### 機能の強化
- **重複起動の防止**: サービスが既に実行中かチェックし、重複起動を防ぎます
- **詳細なログ**: 各ステップでログを出力し、トラブルシューティングを容易にします
- **例外処理の追加**: `IllegalStateException`と`SecurityException`をキャッチして適切に処理します

```java
private void startLocationServiceIfPermitted(Context context) {
    // 権限チェック
    if (!PermissionUtils.hasLocationPermissions(context)) {
        Log.w(TAG, "Cannot start service: Location permissions not granted");
        return;
    }
    
    // 重複起動の防止
    if (isServiceRunning(context, LocationForegroundService.class)) {
        Log.d(TAG, "Service is already running, skipping start");
        return;
    }
    
    // サービスの起動（例外処理付き）
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    } catch (IllegalStateException | SecurityException e) {
        Log.e(TAG, "Failed to start service: " + e.getMessage(), e);
    }
}
```

### 3. AndroidManifest.xml

#### 新しい権限の追加
```xml
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

- **USE_EXACT_ALARM**: Android 14以降（API 34+）で、コア機能として正確なアラームを使用するアプリ向けの取り消し不可能な権限です
- **SCHEDULE_EXACT_ALARM**: 既に存在しており、Android 12以降（API 31+）で必要です

### 4. BACKGROUND_SERVICE.md

ドキュメントを更新し、新しい実装の詳細と権限の説明を追加しました。

## 技術的な詳細

### setExactAndAllowWhileIdle() vs set()

- **`set()`**: 通常のアラーム設定。Dozeモード中は遅延される可能性があります
- **`setExactAndAllowWhileIdle()`**: Dozeモード中でも正確に発火します。バックグラウンドサービスの再起動に最適です

### なぜstopSelf()が必要か

`stopSelf()`を呼び出すことで：
1. サービスのライフサイクルが適切に完了します
2. リソースがクリーンアップされます
3. システムがサービスの状態を正しく認識できます
4. 再起動時に新しいインスタンスが作成されます

### 権限について

- **SCHEDULE_EXACT_ALARM**: ユーザーが設定から取り消し可能
- **USE_EXACT_ALARM**: コア機能として使用する場合、取り消し不可能（Android 14 以降）

位置情報追跡はこのアプリのコア機能であるため、`USE_EXACT_ALARM`権限を使用することが適切です。

## テスト方法

### 1. タスクキル後の動作確認

1. アプリを起動し、位置情報権限を許可
2. 通知が表示されることを確認
3. 最近使用したアプリから、アプリをスワイプして削除
4. 1秒後にサービスが再起動され、通知が再表示されることを確認

### 2. ログの確認

```bash
adb logcat -s LocationFgService ServiceRestartReceiver
```

以下のようなログが表示されるはずです：

```
D/LocationFgService: Task removed, scheduling restart
D/LocationFgService: Scheduled restart using setExactAndAllowWhileIdle
D/ServiceRestartReceiver: Received intent with action: xyz.enoki.nomadpulse.ACTION_RESTART_SERVICE
D/ServiceRestartReceiver: Restarting location service after task kill
D/ServiceRestartReceiver: Started foreground service (API >= 26)
D/LocationFgService: Service created
D/LocationFgService: onStartCommand called
D/LocationFgService: Started as foreground service
```

### 3. デバイス再起動後の動作確認

1. デバイスを再起動
2. 起動後、アプリを開かずに通知が表示されることを確認
3. サービスがバックグラウンドで実行されていることを確認

## 既知の制限事項

### Android 13以降

Android 13以降では、ユーザーが通知ドロワーの「停止」ボタンからフォアグラウンドサービスを停止できます。これはAndroidシステムの仕様であり、回避できません。

### Force Stop

ユーザーが設定→アプリからアプリを強制停止した場合、ユーザーが再度アプリを開くまでサービスは再起動されません。これは期待される動作です。

## セキュリティ上の考慮事項

1. **PendingIntentのセキュリティ**: `FLAG_IMMUTABLE`を使用して、PendingIntentが改変されないようにしています
2. **パッケージ検証**: カスタムアクションの場合、送信元パッケージを検証しています
3. **権限チェック**: サービス起動前に必ず位置情報権限をチェックしています

## 今後の改善案

1. **バッテリー最適化の処理**: ユーザーにバッテリー最適化の除外を求めるUIを追加
2. **サービス状態の通知**: Web層にサービスの状態変化を通知するコールバックを実装
3. **設定可能な通知テキスト**: JavaScriptからも通知テキストをカスタマイズできるようにする
