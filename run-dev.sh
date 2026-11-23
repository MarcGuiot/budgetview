#!/bin/bash
# Lance la version de développement de BudgetView à côté de l'application
# officielle, sans jamais toucher à ses données.
#
#   ./run-dev.sh            lance le jar existant (le construit s'il manque)
#   ./run-dev.sh --build    reconstruit le jar avant de lancer
#
# Variables :
#   BV_DEV_DATA   dossier de données (défaut : ~/.budgetview-dev)
#   JAVA_HOME     JDK 8 (défaut : Zulu 8 dans ~/Library/Java/JavaVirtualMachines)
#
# Les autres arguments sont passés à l'application (ex. un fichier OFX à ouvrir).

set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
DESKTOP="$ROOT/budgetview/bv_desktop"
JAR="$DESKTOP/budgetview.jar"
DATA="${BV_DEV_DATA:-$HOME/.budgetview-dev}"
export JAVA_HOME="${JAVA_HOME:-$HOME/Library/Java/JavaVirtualMachines/zulu-8.jdk/Contents/Home}"

# Garde-fou : refuser les dossiers de données de l'application officielle.
REAL="$HOME/Library/Application Support/BudgetView"
case "$DATA" in
  "$REAL"|"$REAL"/*|"$HOME/.budgetview"|"$HOME/.budgetview/"*)
    echo "Refusé : $DATA est un dossier de données de l'application officielle." >&2
    exit 1 ;;
esac
if [ -f "$REAL/redirect.txt" ]; then
  REDIRECT="$(tr -d '\r\n' < "$REAL/redirect.txt")"
  case "$DATA" in
    "$REDIRECT"|"$REDIRECT"/*)
      echo "Refusé : $DATA est le dossier redirigé de l'application officielle." >&2
      exit 1 ;;
  esac
fi

if ! "$JAVA_HOME/bin/java" -version 2>&1 | grep -q '"1\.8'; then
  echo "JDK 8 introuvable dans JAVA_HOME=$JAVA_HOME (voir README.md)." >&2
  exit 1
fi

if [ "${1:-}" = "--build" ] || [ ! -f "$JAR" ]; then
  [ "${1:-}" = "--build" ] && shift
  echo "Construction de budgetview.jar…"
  (cd "$ROOT" && mvn -q -B -Pgen-version clean install -DskipTests)
fi

mkdir -p "$DATA"
echo "Données de dev : $DATA (log : $DATA/logs/log.txt)"

cd "$DESKTOP"
exec "$JAVA_HOME/bin/java" \
  -DSINGLE_INSTANCE_DISABLED=true \
  -Dbudgetview.data.path="$DATA" \
  -jar "$JAR" "$@"
