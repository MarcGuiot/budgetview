#!/bin/sh
/usr/bin/java -cp java -cp jars/bv_server-2.0.1.jar com.budgetview.server.license.mail.SendMail "$@"