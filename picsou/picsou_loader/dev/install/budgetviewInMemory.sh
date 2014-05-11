#!/bin/sh

INSTALL_DIR=`dirname $0`
java -Xmx196m -Dbudgetview.exe.dir=${INSTALL_DIR} -cp ${INSTALL_DIR}/budgetview.jar picsou.MainInMemory -l LANG "$@"

