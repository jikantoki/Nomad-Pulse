<?php
require_once '../env.php'; //環境変数読み込み
require_once './settings.php'; //ルートディレクトリ読み込み
require_once DIR_ROOT . '/php/myAutoLoad.php'; //自動読み込み
require_once DIR_ROOT . '/php/functions/authAPIforUse.php'; //APIが有効かどうか自動判定
require_once DIR_ROOT . '/php/functions/authAccountforUse.php'; //ログイン状態が有効かどうか判定

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
  $icon = str_replace("'", "\\'", $_POST['icon']);
}

$coverImg = "";
if (isset($_POST['coverImg'])) {
  $coverImg = str_replace("'", "\\'", $_POST['coverImg']);
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
