# Architecture

Où se trouve quoi. Index des notes : [AGENTS.md](../AGENTS.md).

## Modules

Réacteur Maven racine `pom.xml` :

| Module | Rôle | Dans le build ? |
|---|---|---|
| `globs/` | GlobsFramework, **version privée et ancienne** du [framework](https://github.com/MarcGuiot/globsframework) : modèle de données réactif (`GlobType`, champs statiques annotés `@Key`, `@Target`…), repository, changesets. Contient aussi `org.globsframework.gui.splits`, le moteur de mise en page Swing des fichiers `.splits` | oui |
| `saxstack/` | Parseur SAX à pile et écriture XML (`org.saxstack`) | oui |
| `prevayler/` | Prevayler (persistance par journal de transactions + snapshots), forké | oui |
| `siteweaver/` | Générateur de l'ancien site web | oui |
| `budgetview/bv_shared` | Code commun client/serveur | oui |
| `budgetview/bv_desktop` | **L'application** (~1 100 classes, ~350 classes de test) | oui |
| `budgetview/bv_server` | Serveur (licences, cloud, mobile ; Jetty, MySQL, Stripe) | **commenté** |
| `budgetview/bv_analytics` | Analyse de logs | **commenté** |
| `budgetview/bv_website` | Contenu du site (fr/en), voir « Comportement attendu » | hors Maven |
| `uispec4j/` | Framework de tests d'IHM Swing (fork), à installer à part (voir [build.md](build.md)) | **commenté** |
| `splits/` | Ancienne copie du moteur `splits`, doublon de celui de `globs` | hors Maven |
| `budgetview-android/` | Application mobile (Gradle, à part) | hors Maven |
| `functests/` | Ancien projet de tests IntelliJ | hors Maven |

Le projet a d'abord été versionné sous Mercurial (`.hgignore`, `howto/livraison.txt`).

## Application desktop (`budgetview/bv_desktop`)

- Point d'entrée : `com.budgetview.desktop.DesktopApp` → `Application.main`. Version :
  `Application.APPLICATION_VERSION` (6.00), `JAR_VERSION` (156, celui de la 5.01, dernière version distribuée).
- Variantes pour le développement : `InMemoryDesktopApp` (données en mémoire),
  `src/test/java/com/budgetview/desktop/DevDesktopApp.java`.
- Paquets principaux sous `com.budgetview` :
  - `model/` : les `GlobType` métier (Account, Transaction, Series, SeriesBudget, Project, Month…) ;
  - `desktop/` : l'IHM, un paquet par écran ou fonction (budget, categorization, accounts, projects,
    analysis, dashboard…) ;
  - `desktop/upgrade` : migration des données d'une version à l'autre (`UpgradeTrigger`, `ConfigUpgradeTrigger`) ;
  - `triggers/` : logique réactive recalculée à chaque changement du repository globs ;
  - `io/importer` (OFX, QIF, CSV, JSON) et `io/exporter` ;
  - `persistence/` : Prevayler, et `direct/` (dont `Dump`, outil de lecture d'un dossier de données) ;
  - `bank/`, `budgea/`, `io/budgea` : synchronisation bancaire par l'agrégateur Budgea ;
  - `client/`, `mobile/`, `session/` : dialogue avec le serveur.
- Ressources (`src/main/resources`) : écrans en `layout/**/*.splits`, textes en `i18n/lang_fr.properties` et
  `lang_en.properties`, banques par pays dans `banks/`, thèmes de couleurs dans `colors/`.
- Depuis la 6.00, toutes les extensions sont activées au démarrage, sans licence.

## Données utilisateur

- Dossier : `~/.budgetview` (Linux, Windows), `~/Library/Application Support/BudgetView` (macOS), voir
  `desktop/startup/AppPaths.java`. Un fichier `redirect.txt` dans ce dossier peut désigner un autre emplacement
  (« Préférences → Stockage »). Surcharges par propriétés système préfixées `budgetview.`
  (`budgetview.data.path`, `budgetview.prevayler.path`…).
- Un dossier de données peut contenir **plusieurs utilisateurs**, chacun avec ses données chiffrées par son mot
  de passe ; un compte créé sans mot de passe s'appelle `autologUser`.
- Journal de l'application : `<dossier de données>/logs/log.txt`. L'application y redirige `System.out`.

## Comportement attendu

`budgetview/bv_website` est la meilleure description du comportement voulu : aide utilisateur dans
`fr/content/support/` (`faq/`, `guides/`) et journal des versions dans `support/derniers_changements.html`
(5.0 du 2017-10-29 : synchronisation Budgea ; 5.01 du 2017-11-12 : jar 156). Le consulter avant de trancher
entre le code et un test qui le contredit.
