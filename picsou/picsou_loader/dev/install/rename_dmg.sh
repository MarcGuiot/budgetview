#/bin/sh

SOFT_VERSION=`java -jar ../picsou/obfuscated/fourmics.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

mv Fourmics.dmg Fourmics-${SOFT_VERSION}.dmg