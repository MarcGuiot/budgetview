#!/bin/sh

INSTALL_DIR=`dirname $0`
java -Xmx128m -Dbudgetview.exe.dir=${INSTALL_DIR} -cp ${INSTALL_DIR}/budgetviewloader-1.0.jar com.budgetview.Main -l fr "$@"

