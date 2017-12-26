#!/bin/sh
/usr/bin/java -cp java -cp jars/bv_server-2.3.jar com.budgetview.server.license.mail.SendMail "$@"