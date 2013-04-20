#!/bin/sh

# -Dbudgetview.jar.path=path_to_jar_direct (the new version are dowloaded in this directory)
# -Dbudgetview.data.path=path_to_data (can be a shared folder)


INSTALL_DIR=`dirname $0`
java -Xmx196m -Dbudgetview.exe.dir=${INSTALL_DIR} -cp ${INSTALL_DIR}/budgetviewloader-1.0.jar com.budgetview.Main -l en "$@"

