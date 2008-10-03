#!/bin/sh

INSTALL_DIR=`dirname $0`
java -Xmx128m -Dfourmics.exe.dir=${INSTALL_DIR} -cp ${INSTALL_DIR}/fourmicsloader.jar com.fourmics.Main -l fr $*

