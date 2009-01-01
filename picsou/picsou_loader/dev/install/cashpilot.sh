#!/bin/sh

INSTALL_DIR=`dirname $0`
java -Xmx128m -Dcashpilot.exe.dir=${INSTALL_DIR} -cp ${INSTALL_DIR}/cashpilotloader-1.0.jar com.cashpilot.Main -l fr $*

