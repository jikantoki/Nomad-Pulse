# Nomad Pulse

位置情報を記録・共有するAndroidアプリ

<img src="./public/icon.png" width="256px" alt="アイコン">

[最新版APKダウンロード](https://raw.githubusercontent.com/jikantoki/Nomad-Pulse/refs/heads/main/nomad-pulse.apk)

---

## このプロジェクトについて

Nomad Pulse は、GPS位置情報をバックグラウンドで継続的に記録し、フレンドと現在地を共有できるAndroidアプリです。  
Vue 3 + Capacitor で構築されたハイブリッドアプリで、PHPバックエンドとMySQL DBを組み合わせて動作します。

### 主な機能

- バックグラウンドでの継続的な位置情報取得（フォアグラウンドサービス）
- フレンドリスト・フレンド申請
- フレンドとのリアルタイム位置情報共有（地図表示）
- QRコードによるユーザー検索・フレンド追加
- プッシュ通知
- メールアドレス認証付きアカウント登録・ログイン
- プロフィール設定

---

## 技術スタック

| 領域 | 技術 |
| --- | --- |
| フロントエンド | Vue 3 + TypeScript + Vite + Vuetify 3 |
| モバイル | Capacitor 8（Android） |
| 地図 | Leaflet（@vue-leaflet/vue-leaflet） |
| 状態管理 | Pinia |
| バックエンド | PHP |
| データベース | MySQL |
| プッシュ通知 | Web Push / Firebase Cloud Messaging |

---

## ファイル構成

```
Nomad-Pulse/
├── src/                        # Vue フロントエンドソース
│   ├── pages/                  # ページコンポーネント（ファイルベースルーティング）
│   │   ├── index.vue           # ホーム（地図表示）
│   │   ├── login.vue           # ログイン
│   │   ├── registar.vue        # アカウント登録
│   │   ├── friendlist.vue      # フレンドリスト
│   │   ├── qrcode.vue          # QRコード表示・読み取り
│   │   ├── about.vue           # アプリについて
│   │   ├── terms.vue           # 利用規約
│   │   ├── tutorial.vue        # チュートリアル
│   │   ├── password_reset.vue  # パスワードリセット
│   │   ├── settings/           # 設定ページ群
│   │   └── user/               # ユーザープロフィールページ
│   ├── components/             # 共通コンポーネント
│   ├── stores/                 # Pinia ストア
│   ├── js/                     # ユーティリティ・API関数
│   ├── mixins/                 # Vue ミックスイン
│   ├── styles/                 # SCSS スタイル
│   └── plugins/                # Vuetify等プラグイン設定
├── android/                    # Capacitor Androidネイティブコード
│   └── app/src/main/java/xyz/enoki/nomadpulse/
│       ├── MainActivity.java               # アプリエントリポイント
│       ├── LocationForegroundService.java  # バックグラウンド位置情報サービス
│       ├── ServiceRestartReceiver.java     # サービス再起動ブロードキャストレシーバー
│       └── PermissionUtils.java            # 権限チェックユーティリティ
├── php/                        # PHP APIサーバー
│   ├── createAccount.php       # アカウント登録
│   ├── loginAccount.php        # ログイン
│   ├── updateGeoLocation.php   # 位置情報更新
│   ├── getMyFriendList.php     # フレンドリスト取得
│   ├── friendRequest.php       # フレンド申請
│   ├── getProfile.php          # プロフィール取得
│   ├── updateProfile.php       # プロフィール更新
│   ├── sendPushForAccount.php  # プッシュ通知送信
│   └── ...
├── public/                     # 静的ファイル（アイコン等）
├── runners/                    # Capacitor バックグラウンドランナー
├── database.sql                # MySQL スキーマ
├── capacitor.config.ts         # Capacitor設定
└── vite.config.mts             # Vite設定
```

---

## セットアップ

このアプリは **フロントエンド（Vite）** と **PHPバックエンド** の2つのサーバーが必要です。

### 前提条件

- Node.js + yarn
- PHP + Composer
- MySQL
- Android Studio（Androidビルド時）

### 1. フロントエンドのセットアップ

```shell
# リポジトリをクローン後
yarn install
```

#### 環境変数の設定

ルートに `.env` ファイルを作成し、以下を記述（クォーテーション不要）：

```env
VUE_APP_API_ID=default
VUE_APP_API_TOKEN=PHPサーバーで発行するアクセストークン
VUE_APP_API_ACCESSKEY=PHPサーバーで発行するアクセスキー
VUE_APP_API_HOST=APIサーバーのURL（例: https://api.example.com）

VUE_APP_WEBPUSH_PUBLICKEY=WebPush用パブリックキー
VUE_APP_WEBPUSH_PRIVATEKEY=WebPush用プライベートキー
```

WebPush用の鍵はこちらで生成できます: <https://web-push-codelab.glitch.me/>

### 2. PHPサーバーのセットアップ

レンタルサーバーや自前のPHP環境を用意し、`php/` ディレクトリをドキュメントルートに配置します。

```shell
composer install
```

#### PHPの設定ファイル（`/env.php`）

リポジトリルート直下（PHPサーバー側）に `env.php` を作成し、以下を記述：

```php
<?php
define('DIRECTORY_NAME', '/プロジェクトルートのディレクトリ名');

define('VUE_APP_WebPush_PublicKey', 'パブリックキー');
define('VUE_APP_WebPush_PrivateKey', 'プライベートキー');
define('WebPush_URL', 'プッシュ通知を使うドメイン');
define('WebPush_icon', 'プッシュ通知アイコンURL');
define('Default_user_icon', 'デフォルトユーザーアイコンURL');

define('MySQL_Host', 'MySQLサーバー');
define('MySQL_DBName', 'DB名');
define('MySQL_User', 'DB操作ユーザー名');
define('MySQL_Password', 'DBパスワード');

define('SMTP_Name', '送信者名');
define('SMTP_Username', 'SMTPユーザー名');
define('SMTP_Mailaddress', '送信メールアドレス');
define('SMTP_Password', 'SMTPパスワード');
define('SMTP_Server', 'SMTPサーバー');
define('SMTP_Port', 587);
```

#### .htaccessの設定例

```htaccess
<IfModule mod_rewrite.c>
RewriteEngine on
RewriteBase /
RewriteCond %{REQUEST_FILENAME} !-f
RewriteCond %{REQUEST_FILENAME} !-d
RewriteRule ^(.+)$ php/$1 [L]
</IfModule>
Header append Access-Control-Allow-Origin: "*"
```

### 3. MySQLのセットアップ

`database.sql` をMySQLにインポートします：

```shell
mysql -u ユーザー名 -p DB名 < database.sql
```

PHPMyAdminが使える環境であれば、GUIからインポートも可能です。

### 4. デフォルトAPIトークンの発行

このアプリは独自のアクセストークンでAPIを保護しています。初回セットアップ時に以下を実行してください：

1. PHPサーバーの `/makeApiForAdmin.php` にブラウザでアクセス
2. 表示されたトークン・キーをコピー
3. `.env` の `VUE_APP_API_TOKEN` / `VUE_APP_API_ACCESSKEY` に記述

> **注意**: トークンを紛失した場合は、MySQLの `api_list` テーブルの `secretId='default'` 行を削除し、再度 `/makeApiForAdmin.php` にアクセスしてリセットしてください。

---

## 開発コマンド

```shell
# 依存パッケージのインストール
yarn install

# 開発サーバー起動（http://localhost:9000）
yarn dev

# 本番ビルド
yarn build

# 型チェック
yarn type-check

# リント・自動修正
yarn lint

# Capacitor Android同期（ビルド後に実行）
npx cap sync android

# Android APKビルド
cd android && ./gradlew assembleDebug
```

---

## バックグラウンドサービスについて

このアプリはバックグラウンドでの継続的な位置情報取得のため、Androidネイティブのフォアグラウンドサービスを実装しています。

### 実装内容

| クラス | 役割 |
| --- | --- |
| `LocationForegroundService` | 位置情報の継続取得・永続通知の表示 |
| `ServiceRestartReceiver` | タスクキル・デバイス再起動時のサービス自動再起動 |
| `MainActivity` | アプリ起動時のサービス開始・権限リクエスト |
| `PermissionUtils` | 位置情報権限チェックのユーティリティ |

### 再起動メカニズム

- **START_STICKY**: システムがメモリ不足でサービスを終了した場合、自動再起動
- **AlarmManager**: タスクキル時に1秒後の再起動をスケジュール
- **BOOT_COMPLETED**: デバイス再起動時にサービスを自動開始

### 必要な権限

- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_LOCATION`: フォアグラウンドサービス実行
- `ACCESS_FINE_LOCATION` / `ACCESS_BACKGROUND_LOCATION`: 位置情報取得
- `RECEIVE_BOOT_COMPLETED`: デバイス起動時の自動起動
- `SCHEDULE_EXACT_ALARM`: 正確なタイマーによる再起動スケジュール
- `POST_NOTIFICATIONS`: 通知表示（Android 13以降）

### 制限事項

- Android 13（API 33）以降では、通知ドロワーの「停止」ボタンでサービスを強制終了できます（システム仕様のため回避不可）
- 設定画面からの「強制停止」を行うとサービスは停止し、ユーザーが再度アプリを開くまで再起動しません

詳細は [`BACKGROUND_SERVICE.md`](./BACKGROUND_SERVICE.md) および [`IMPLEMENTATION_JAPANESE.md`](./IMPLEMENTATION_JAPANESE.md) を参照してください。

---

## トラブルシューティング

### Androidビルドエラーが出る

```shell
cd android && ./gradlew clean
npx cap sync android
cd android && ./gradlew assembleDebug
```

### PHPサーバーに接続できない

- `env.php` の `MySQL_Host` や `SMTP_*` 設定を確認
- `composer install` を実行済みか確認

### 開発サーバーが起動しない

```shell
yarn install
yarn dev
```

ポートが競合している場合は `vite.config.mts` の `server.port` を変更してください（デフォルト: 9000）。
