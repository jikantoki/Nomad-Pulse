<?php
require_once '../env.php'; //環境変数読み込み
require_once './settings.php'; //ルートディレクトリ読み込み
require_once DIR_ROOT . '/php/myAutoLoad.php'; //自動読み込み
require_once DIR_ROOT . '/php/functions/authAPIforUse.php'; //APIが有効かどうか自動判定

if (
  !isset($_SERVER['HTTP_TARGETID']) &&
  !isset($_SERVER['HTTP_ACCEPT'])
) {
  echo json_encode([
    'status' => 'invalid',
    'reason' => 'invalid authentication information',
    'errCode' => 10
  ]);
  exit;
}

$myId = $_SERVER['HTTP_ID'];
$targetId = $_SERVER['HTTP_TARGETID'];
$accept = $_SERVER['HTTP_FRIENDACCEPT'];

$mySecretId = idToSecretId($myId);
$targetSecretId = idToSecretId($targetId);

$friendStatus = $accept ? true : false;

$unixtime = time();

print_r($friendStatus);
if($friendStatus){
  SQL("
    update follow_list set
    status = 'friend',
    unixtime = {$unixtime}
    where
    fromUserId = '{$targetSecretId}'
    and
    toUserId = '{$mySecretId}';
  ");
} else {
  $s = SQL("
    delete from follow_list
    where
    fromUserId = '{$targetSecretId}'
    and
    toUserId = '{$mySecretId}';
  ");
  print_r($s);
  $s = SQL("
    delete from follow_list
    where
    fromUserId = '{$mySecretId}'
    and
    toUserId = '{$targetSecretId}';
  ");
  print_r($s);
}

echo json_encode([
  'status' => 'ok',
  'reason' => 'Thank you!',
  'friendStatus' => $friendStatus
]);