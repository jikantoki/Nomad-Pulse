<?php
require_once '../env.php'; //環境変数読み込み
require_once './settings.php'; //ルートディレクトリ読み込み
require_once DIR_ROOT . '/php/myAutoLoad.php'; //自動読み込み
require_once DIR_ROOT . '/php/functions/authAPIforUse.php'; //APIが有効かどうか自動判定
require_once DIR_ROOT . '/php/functions/authAccountforUse.php'; //ログイン状態が有効かどうか判定


/**
 * Base64エンコードされた画像データをサーバーに保存し、そのURLを返します。
 *
 * @param string $base64_data Base64画像文字列（データURIスキーム含む場合あり）
 * @return string|false 保存された画像の公開URL、失敗した場合は false
 */
function save_base64_image_to_server($base64_data)
{
  // 1. 設定項目
  $upload_dir = 'uploads/';
  // 現在のスクリプトの場所に基づいてベースURLを動的に決定
  $base_url = (isset($_SERVER['HTTPS']) ? 'https' : 'http') . '://' . $_SERVER['HTTP_HOST'] . dirname($_SERVER['PHP_SELF']) . '/' . $upload_dir;

  // uploadsディレクトリが存在しない場合は作成を試みる
  if (!is_dir($upload_dir)) {
    if (!mkdir($upload_dir, 0755, true)) {
      // ディレクトリ作成失敗
      error_log("Failed to create upload directory: " . $upload_dir);
      return false;
    }
  }

  // 2. データURIスキームのプレフィックスを除去し、MIMEタイプを特定
  $mime_type = 'image/png'; // デフォルト
  $encoded_image = $base64_data;

  if (strpos($base64_data, 'data:') === 0) {
    $data_parts = explode(',', $base64_data);
    $encoded_image = end($data_parts);
    // MIMEタイプを抽出
    preg_match('/data:(.*?);/', $base64_data, $matches);
    if (!empty($matches[1])) {
      $mime_type = $matches[1];
    }
  }

  // 3. Base64文字列をバイナリデータにデコード
  $decoded_image = base64_decode($encoded_image);

  if ($decoded_image === false) {
    error_log("Base64 decoding failed.");
    return false;
  }

  // 4. MIMEタイプに基づいてファイル拡張子を決定
  $extension = '.png';
  if (strpos($mime_type, 'jpeg') !== false || strpos($mime_type, 'jpg') !== false) {
    $extension = '.jpg';
  } elseif (strpos($mime_type, 'png') !== false) {
    $extension = '.png';
  } else {
    // サポートされていないMIMEタイプ
    error_log("Unsupported MIME type: " . $mime_type);
    return false;
  }

  // 5. 一意なファイル名を生成し、保存パスとURLを構築
  $file_name = uniqid('img_') . $extension;
  $file_path = $upload_dir . $file_name;
  $file_url = $base_url . $file_name;

  // 6. ファイルをサーバーに保存
  if (file_put_contents($file_path, $decoded_image) !== false) {
    // 成功したらURLを返す
    return $file_url;
  } else {
    // 保存失敗
    error_log("Failed to save image file: " . $file_path);
    return false;
  }
}

if (
  !isset($_SERVER['HTTP_ID'])
) {
  echo json_encode([
    'status' => 'invalid',
    'reason' => 'invalid authentication information',
    'errCode' => 10
  ]);
  exit;
}

$id = $_SERVER['HTTP_ID'];
$secretId = idToSecretId($id);

$icon = "";
if (isset($_POST['icon'])) {
  $icon = save_base64_image_to_server($_POST['icon']) ?? '';
}

$coverImg = "";
if (isset($_POST['coverImg'])) {
  $coverImg = save_base64_image_to_server($_POST['coverImg']) ?? '';
}

$name = "";
if (isset($_POST['name'])) {
  $name = str_replace("'", "\\'", $_POST['name']);
}

$message = "";
if (isset($_POST['message'])) {
  $message = str_replace("'", "\\'", $_POST['message']);
}

// 将来的な拡張用
$links = "";
if (isset($_POST['links'])) {
  $links = str_replace("'", "\\'", $_POST['links']);
}

SQL("
update user_profile_list set
icon = '{$icon}',
coverImg = '{$coverImg}',
name = '{$name}',
message = '{$message}'
where secretId = '{$secretId}';
");

echo json_encode([
  'status' => 'ok',
  'id' => $id,
]);
