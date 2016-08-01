<?php
  header('Content-disposition: attachment; filename=demo.ofx');
  header('Content-type: application/x-ofx');
  readfile('demo.ofx');
  ?>