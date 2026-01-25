<?php
// require_once DIR_ROOT . '/env.php';

//メール関係

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

// 本当はここenvから取得したい
define('MailHeader', "<p>
<img src=\"https://nomadpulse.enoki.xyz/wp-content/uploads/2026/01/icon.png\" alt=\"Nomad Pulse\" height=\"64px\">
<br>
いつも Nomad Pulse をご利用いただきありがとうございます。
<hr>
</p>");
define('MailFooter', "<p>
<hr>
このメールに返信することはできません。
<br>
また、このメールに身に覚えのない場合は、エノキ電気までお問い合わせください。
<br>
<a href=\"https://nomadpulse.enoki.xyz\">Nomad Pulse</a> by <a href=\"https://enoki.xyz\">エノキ電気</a>
<br>
お問い合わせはこちら: <a href=\"mailto:info@enoki.xyz\">info@enoki.xyz</a>
<br>
&copy; 2024 エノキ電気
</p>");

function sendMail($to, $title, $message)
{
  try {
    //全メール共通設定
    $mail = new PHPMailer(true);
    $mail->CharSet = 'utf-8';
    $mail->isSMTP();
    $mail->SMTPAuth = true;
    $mail->Host = SMTP_Server;
    $mail->Username = SMTP_Username;
    $mail->Password = SMTP_Password;
    $mail->Port = SMTP_Port;
    $mail->setFrom(SMTP_Mailaddress);
    $mail->FromName = SMTP_Name;
    $mail->isHTML(true);

    //メールによる設定
    $mail->addAddress($to);
    $mail->Subject = $title;
    $mail->Body = MailHeader . "<br>{$message}<br>" . MailFooter;

    //送信
    $mail->send();
  } catch (Exception $e) {
    echo $e;
  }
}
