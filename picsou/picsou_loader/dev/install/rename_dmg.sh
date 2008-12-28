#/bin/sh

SOFT_VERSION=`java -jar ../picsou/obfuscated/cashpilot.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

mv CashPilot.dmg CashPilot-${SOFT_VERSION}.dmg