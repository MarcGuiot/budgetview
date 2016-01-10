#/bin/sh

SOFT_VERSION=`java -jar ../picsou/target/obfuscated/budgetview.jar -v -soft | grep "Software version:" |
              sed -e 's/Software version://g' | sed -e 's/  *//g'`

mv BudgetView-en.dmg BudgetView-${SOFT_VERSION}-en.dmg
mv BudgetView-fr.dmg BudgetView-${SOFT_VERSION}-fr.dmg
mv BudgetView-InMemory-fr.dmg BudgetView-InMemory-${SOFT_VERSION}-fr.dmg
