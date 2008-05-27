export CATALINA_BASE=/mnt/plone/dev/server
export PREVAYLER_PATH=/var/prevayler

rm -rf ${CATALINA_BASE}
mkdir ${CATALINA_BASE}
mkdir ${CATALINA_BASE}/logs
mkdir -p ${CATALINA_BASE}/conf/Catalina/localhost
mkdir -p ${CATALINA_BASE}/webapps/picsou
mkdir ${CATALINA_BASE}/temp
cp picsou_server/config/picsou.xml ${CATALINA_BASE}/conf/Catalina/localhost/
cp picsou_server/config/web.xml ${CATALINA_BASE}/conf/
cp picsou_server/config/server.xml ${CATALINA_BASE}/conf/
cp picsou_server/config/catalina.policy ${CATALINA_BASE}/conf/
cp picsou_server/config/catalina.properties ${CATALINA_BASE}/conf/
cp picsou_server/config/jk2.properties ${CATALINA_BASE}/conf/
export PICSOU_DIR=`pwd`
cd  ${CATALINA_BASE}/webapps/picsou
jar xvf ${PICSOU_DIR}/picsou_server/lib/picsou.war
cd -

