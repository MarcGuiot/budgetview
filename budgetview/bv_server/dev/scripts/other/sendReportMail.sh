#/bin/sh

cd scripts
now=`date +"%F %T"`
cat ../picsou0.log.0 | ./filter.sh |  mail -s "Picsou activite du $now" marc.guiot@free.fr,regis.medina@gmail.com

