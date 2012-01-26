<?php
 
        function mylog($msg) {
            $fmt = "[%s] [%s] %s\n";
            $ip = $_SERVER['REMOTE_ADDR'];
            $time = date('d.m.Y h:i:s');
            $line = sprintf($fmt, $time, $ip, $msg);
            error_log($line, 3, "/home/kalugny/logs/user/emailbyweb.log");
        }

        function exception_handler($exception) {
            mylog("Uncaught exception: " . $exception->getMessage());
        }

        set_exception_handler('exception_handler');



        require_once "Mail.php";
        require_once "config.php";



        //define("PIWIK_ENABLE_TRACKING");
        //$_GET["idsite"] = "3";
        //require_once "../piwik/piwik.php";
        //echo($_GET["idsite"]);


        // -- Piwik Tracking API init -- 
        require_once "../PiwikTracker.php";
        PiwikTracker::$URL = 'http://toplessproductions.com/static/piwik/';

        $t = new PiwikTracker( $idSite = 3);
        $t->setCustomVariable(1,'Force IP',$_SERVER['REMOTE_ADDR']);
        $t->doTrackPageView('email by web');

        $from = "<EmergencyButtonApp@gmail.com>";

        $to = $_POST["to"];
        $subject = $_POST["subject"];
        $body = $_POST["message"];
        $secret = $_POST["secret"];
        $replyto = $_POST["replyto"];

        if ($secret != $SECRET) {
            log("Wrong secret");
            die("...");
        }

        $host = "ssl://smtp.gmail.com";
        $port = "465";
        $username = "EmergencyButtonApp@gmail.com";
        $password = $PW;

        $headers = array ('From' => $from,
          'To' => $to,
          'Subject' => $subject,
          'Reply-To' => $replyto);
        $smtp = Mail::factory('smtp',
          array ('host' => $host,
            'port' => $port,
            'auth' => true,
            'username' => $username,
            'password' => $password));

        $mail = $smtp->send($to, $headers, $body);

        if (PEAR::isError($mail)) {
           //echo("<p>Error: " . $mail->getMessage() . "</p>");
           echo("email fail");
           mylog($mail->getMessage());
           $t->doTrackPageView('fail');
         } else {
           echo("success");
           mylog("success $to $subject $secret");
           //$t->doTrackPageView('success');
         }
         
         // an attempt to use the 1pixel piwik analytics directly
         //require_once "../piwik/piwik.php";

    ?>
