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

$res = SQLfetchAll("
select * from follow_list where
fromUserId = '{$secretId}'
or
toUserId = '{$secretId}'
;
");

$cnt = 0;
foreach ($res as $friend) {
  $friendRealId = null;
  echo $friend;
  if ($friend['fromUserId'] == $secretId) {
    $friendRealId = secretIdToId($friend['toUserId']);
  } else {
    $friendRealId = secretIdToId($friend['fromUserId']);
  }
  $res[$cnt]['friendRealId'] = $friendRealId;
  $res[$cnt]['friendProfile'] = SQLfind('user_profile_list', 'userId', $friendRealId);
  $cnt++;
}

echo json_encode([
  'status' => 'ok',
  'id' => $id,
  'friendList' => $res
]);
