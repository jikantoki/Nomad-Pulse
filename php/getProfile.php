<?php
require_once '../env.php'; //環境変数読み込み
require_once './settings.php'; //ルートディレクトリ読み込み
require_once DIR_ROOT . '/php/myAutoLoad.php'; //自動読み込み
require_once DIR_ROOT . '/php/functions/authAPIforUse.php'; //APIが有効かどうか自動判定

if (
  !isset($_SERVER['HTTP_TARGETID'])
) {
  echo json_encode([
    'status' => 'invalid',
    'reason' => 'invalid authentication information',
    'errCode' => 10
  ]);
  exit;
}

$getFriendStatus = false;
if (isset($_SERVER['HTTP_ID']) && isset($_SERVER['HTTP_TOKEN'])) {
  $id = $_SERVER['HTTP_ID'];
  $token = $_SERVER['HTTP_TOKEN'];
  $secretId = idToSecretId($id);
  if ($secretId) {
    $isAuthed = authAccount($secretId, $token);
    if ($isAuthed) {
      $getFriendStatus = true;
    }
  }
}

$targetId = $_SERVER['HTTP_TARGETID'];
$res = getProfile($targetId);
if ($res) {
  $friendStatus1 = SQLfind('follow_list', 'fromUserId', $res['secretId']);
  $friendStatus2 = SQLfind('follow_list', 'toUserId', $res['secretId']);
  if ($friendStatus1) {
    $friendStatus = $friendStatus1['status'];
  } else if ($friendStatus2) {
    $friendStatus = $friendStatus2['status'];
  } else {
    $friendStatus = null;
  }
  echo json_encode([
    'status' => 'ok',
    'reason' => 'Thank you!',
    'res' => $res,
    'id' => $targetId,
    'friendStatus' => $friendStatus
  ]);
} else {
  echo json_encode([
    'status' => 'ng',
    'reason' => 'Unknown user',
    'id' => $targetId,
    'errCode' => 20
  ]);
}
