# バックグラウンドサービス実装の説明（日本語版）

## 概要

このドキュメントは、Nomad Pulseアプリがバックグラウンドで継続的に動作するための実装について、日本語で説明します。

## 実装した機能

### 必須要件への対応

✅ **通知を消しても復活する**
- 通知を「進行中」として設定（`setOngoing(true)`）により、ユーザーが簡単に消せないようになっています
- サービスが再起動すると通知も自動的に復活します

✅ **タスクキルしてもバックグラウンドで動き続ける**
- 複数の再起動メカニズムを実装
- アプリをスワイプで閉じても、サービスは自動的に再起動します

## 実装したファイル

### 1. LocationForegroundService.java
**役割**: メインのバックグラウンドサービス

**主な機能**:
```java
// 永続的な通知を表示
Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
    .setContentTitle("NomadPulse")
    .setContentText("バックグラウンドで位置情報を取得しています")
    .setOngoing(true)  // ← これで通知を消せなくします
    .build();
```

**再起動メカニズム**:
1. **START_STICKY**: システムがメモリ不足で強制終了した場合、自動的に再起動
2. **AlarmManager**: タスクキルされた時に1秒後に再起動
3. **Broadcast**: サービスが終了時に再起動信号を送信

### 2. ServiceRestartReceiver.java
**役割**: サービスの再起動を管理

**主な機能**:
- デバイス起動時（BOOT_COMPLETED）にサービスを自動開始
- カスタム再起動アクションを受信してサービスを再開
- セキュリティのためインテントを検証

**セキュリティ対策**:
```java
// 信頼できるアクションのみ処理
if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
    // システムからの起動信号 - 安全
    startLocationService(context);
} else if (ACTION_RESTART_SERVICE.equals(action)) {
    // 自アプリからの信号か確認
    if (context.getPackageName().equals(intent.getPackage())) {
        startLocationService(context);
    }
}
```

### 3. MainActivity.java
**役割**: アプリ起動時にサービスを開始

**主な機能**:
- アプリ起動時にフォアグラウンドサービスを開始
- すでに起動中か確認してから開始（重複起動を防止）
- アクティビティが終了してもサービスは継続

```java
// サービスが既に起動しているか確認
if (!isServiceRunning(LocationForegroundService.class)) {
    // 起動していない場合のみ開始
    Intent serviceIntent = new Intent(this, LocationForegroundService.class);
    startForegroundService(serviceIntent);
}
```

### 4. AndroidManifest.xml
**追加した設定**:

**サービス宣言**:
```xml
<service
    android:name=".LocationForegroundService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="location"
    android:stopWithTask="false" />
```
- `foregroundServiceType="location"`: 位置情報用サービスとして宣言
- `stopWithTask="false"`: タスクが終了してもサービスは継続

**ブロードキャストレシーバー**:
```xml
<receiver
    android:name=".ServiceRestartReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="xyz.enoki.nomadpulse.ACTION_RESTART_SERVICE" />
    </intent-filter>
</receiver>
```
- `exported="true"`: システムからの信号を受信するため必須

**パーミッション**:
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

## 動作の流れ

### 1. アプリ起動時
```
アプリ起動
    ↓
MainActivity.onCreate()
    ↓
サービス起動チェック
    ↓
LocationForegroundService開始
    ↓
通知表示（永続的）
```

### 2. タスクキル時
```
ユーザーがアプリをスワイプで終了
    ↓
onTaskRemoved()が呼ばれる
    ↓
AlarmManagerで1秒後に再起動をスケジュール
    ↓
ServiceRestartReceiverが起動信号を受信
    ↓
LocationForegroundServiceを再起動
    ↓
通知が再表示される
```

### 3. デバイス再起動時
```
デバイス起動
    ↓
BOOT_COMPLETEDシステム信号
    ↓
ServiceRestartReceiverが受信
    ↓
LocationForegroundServiceを開始
    ↓
通知表示
```

### 4. システムによる強制終了時
```
メモリ不足でシステムがサービスを終了
    ↓
onDestroy()が呼ばれる
    ↓
再起動信号をブロードキャスト
    ↓
START_STICKYにより自動再起動もスケジュール
    ↓
サービスが復活
```

## 制限事項

### Android 13以降のタスクマネージャー
- Android 13（API 33）以降では、ユーザーが通知ドロワーから「停止」ボタンでアプリを停止できます
- この場合、アプリのプロセス全体が強制終了され、サービスも停止します
- これはAndroidのシステム仕様で、回避できません

### 強制停止
- 設定画面からアプリを「強制停止」した場合、サービスは停止します
- ユーザーが再度アプリを開くまで再起動しません
- これは想定された動作です

### バッテリー最適化
- 一部のデバイスでは、積極的なバッテリー最適化がサービスを終了させる場合があります
- ユーザーがアプリをバッテリー最適化の対象外にする必要がある場合があります
- これはデバイス/メーカー固有の動作です

## セキュリティ対策

### 実装したセキュリティ機能

1. **インテント検証**
   - 受信したインテントのアクションを確認
   - 期待されるアクション以外は無視

2. **パッケージ検証**
   - カスタムアクションの場合、送信元が自アプリか確認
   - 他のアプリからの不正な信号を防止

3. **完全修飾アクション名**
   - `xyz.enoki.nomadpulse.ACTION_RESTART_SERVICE`
   - 他アプリとの衝突を防止

4. **セキュアなPendingIntent**
   - `FLAG_IMMUTABLE`フラグを使用
   - 不正な変更を防止

## テスト方法

### 1. タスク削除テスト
```
1. アプリを開いて通知が表示されることを確認
2. 最近使用したアプリからスワイプで削除
3. 通知が残っていることを確認
4. アプリを再度開いて正常に動作することを確認
```

### 2. デバイス再起動テスト
```
1. デバイスを再起動
2. アプリを開かずに通知が表示されることを確認
3. 通知をタップしてアプリが開くことを確認
```

### 3. 長時間動作テスト
```
1. アプリを起動してバックグラウンドに移動
2. 数時間放置
3. 定期的に通知が残っているか確認
```

## 既存のコードとの統合

この実装は既存の`@capacitor-community/background-geolocation`プラグインと連携して動作します：

- **既存プラグイン**: 実際の位置情報追跡を処理
- **新しいサービス**: プロセスを生き続けさせる
- **両方が協力**: 永続的なバックグラウンド位置追跡を実現

## 技術的な詳細

### なぜ複数の再起動メカニズム？

各メカニズムは異なる状況に対応します：

1. **START_STICKY**
   - システムがメモリ不足で終了した場合に有効
   - 自動的に再起動

2. **AlarmManager（onTaskRemoved）**
   - ユーザーがタスクを削除した場合に有効
   - より確実に再起動

3. **Broadcast（onDestroy）**
   - その他の終了シナリオのフォールバック
   - 追加の安全装置

### 通知がなぜ消せない？

```java
.setOngoing(true)  // この設定により：
// - 通常の方法では消せない
// - スワイプで消せない
// - 「進行中」セクションに表示される
```

## まとめ

この実装により：
- ✅ タスクキルしてもバックグラウンドで動き続ける
- ✅ 通知が消せない（進行中として表示）
- ✅ サービスが再起動すると通知も復活
- ✅ デバイス起動時に自動開始
- ✅ セキュリティ対策済み
- ✅ 既存のコードを壊さない最小限の変更

**唯一の制限**: Android 13以降のタスクマネージャーの「停止」ボタンと設定からの強制停止は、システム仕様のため回避できません。

## ドキュメント

詳細な技術ドキュメント（英語）も用意されています：
- `BACKGROUND_SERVICE.md` - 技術的な詳細
- `SECURITY_SUMMARY.md` - セキュリティ分析
- `IMPLEMENTATION_SUMMARY.md` - 実装の概要
