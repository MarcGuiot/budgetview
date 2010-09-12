#/bin/sh

SOFT_VERSION=`java -jar ../picsou/obfuscated/budgetview.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

mv BudgetView.dmg BudgetView-${SOFT_VERSION}.dmg