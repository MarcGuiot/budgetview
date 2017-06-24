#!/bin/sh
/usr/bin/java -cp java -cp jars/bv_server-2.0.4.jar com.budgetview.server.license.mail.SendMail "$@"