# Tests

Commandes de test : voir le `README.md`. Index des notes : [AGENTS.md](../AGENTS.md).

## Généralités (`bv_desktop`)

- JUnit 3 (`TestCase`). Surefire prend `**/*Test.java` et `**/*Tests.java`. Pour désactiver un test, renommer
  la méthode `disabled_test…` avec un commentaire qui donne la raison (`@Ignore` est sans effet) ; exclure la
  classe dans le `pom` s'il n'y reste plus aucun test.
- Les tests fonctionnels (`src/test/java/com/budgetview/functests`, classes de base `FunctionalTestCase` →
  `LoggedInFunctionalTestCase`) pilotent l'IHM par uispec4j.
- Réglages de surefire et leurs raisons :
  - `-Xmx1024m` : à 256 Mo, les tests s'enlisaient en `OutOfMemoryError` ;
  - `reuseForks=false`, une JVM par classe : les tests fonctionnels laissent fuir fenêtres et threads, une JVM
    partagée finit par mourir et emporte la suite ;
  - `-Dapple.awt.UIElement=true` : sans cela, chaque JVM de test vole le focus sur macOS.
- Sur un portable, empêcher la mise en veille pendant la suite (`caffeinate -i` sur macOS) : une JVM gelée par
  la veille fait échouer les tests d'IHM par délai dépassé. Rien d'autre de lourd ne doit tourner à côté, pour
  la même raison.
- Diagnostic : rapports dans `target/surefire-reports/*.txt`, pile complète avec `-DtrimStackTrace=false`.
  `OperationChecker.checkDataIsOk` affiche le détail des incohérences de données trouvées.

## Pièges des tests fonctionnels

- **Fenêtre partagée** : `LoggedInFunctionalTestCase` garde la fenêtre principale d'un test à l'autre. Un test
  qui passe seul mais échoue dans sa classe vient presque toujours d'un réglage d'IHM laissé modifié par un test
  précédent : tout test qui change un tel réglage le rétablit avant de finir.
- **Langue** : la 6.00 force le français au démarrage (`Application.LANG_PROPERTY`, `budgetview.lang` = `fr`,
  `en` ou `keep`). `FunctionalTestCase` met `keep`, pour que les tests restent dans la langue qu'ils fixent
  (anglais).
- **Sorties** : `FunctionalTestCase` remplace `AppExit` par une exception, pour que la JVM de surefire survive à
  une sortie de l'application. D'où la règle : jamais `System.exit`.
- **Licence** : toutes les extensions sont actives ; `LicenseChecker.register()` ne fait plus rien et le menu
  « Activer les extensions… » n'existe plus.
- **Déconnexion** : un test qui se déconnecte puis se reconnecte doit travailler sur disque
  (`setInMemory(false)` dans son `setUp`, voir `PreferencesTest`). La déconnexion recrée la session, ce qui
  vide une base en mémoire.
- **Repository construit à la main** : un test unitaire doit y charger les constantes du modèle, comme
  `PicsouInit` (voir `DataCheckingServiceTest`), sinon le contrôle des liens signale de faux liens cassés.

## `globs`

`mvn clean test -pl globs`. Son `pom` fixe une JVM par classe, une limite de temps et les `--add-opens`.
`clean` après un changement de signature (les tests non touchés ne sont pas recompilés) ;
`mvn install -pl globs -DskipTests` avant de tester `bv_desktop`, qui prend `globs` dans `~/.m2`.
