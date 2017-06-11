#!/bin/sh

/usr/bin/java -cp /home/picsou/budgetviewLicenceServer136.jar:/home/picsou/activation-1.1.jar:/home/picsou/mail-1.4.jar org.designup.picsou.license.mail.SendMail "$@"