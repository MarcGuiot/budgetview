<?php
header('Content-Description: File Transfer');
header('Content-disposition: attachment; filename=demo.ofx');
header('Content-type: text/ofx');
readfile('/files/demo.ofx');
?>