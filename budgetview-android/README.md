# BudgetView Android

## Signature des APK pour publication sur Google Play

### Configuration du poste de travail

1. Copier le fichier `doc/signing-config.gradle.example` vers un dossier personnel (par exemple
`~/.signing/`)

1. Renommer ce fichier `budgetview.gradle` et modifier les mots de passe ainsi que le nom d'alias
associé au fichier *keystore*.

1. Dans ce même dossier, copier la clé *keystore* et renommer le fichier `budgetview.keystore`

1. Le répertoire contiendra ainsi deux fichiers. Dans notre exemple :

   - `~/.signing/budgetview.keystore`
   - `~/.signing/budgetview.gradle`

1. Dans les variables d'environnement, ajouter la variable `SIGNING_BUDGETVIEW` indiquant le chemin
vers le dossier où trouver les fichiers créés ci-dessus, avec en plus le nom des fichiers sans
l'extension. Par exemple: `SIGNING_BUDGETVIEW=~/.signing/budgetview`

Le poste de travail est maintenant configuré et prêt à produire les APK.

### Générer un fichier APK *production*

Il faut bien entendu installer le SDK Android et que les outils du SDK soient dans le *PATH*. Le
plus simple est d'installer Android Studio et de taper les lignes de commande directement depuis
la fenêtre terminal d'Android Studio.

Vous pouvez ensuite générer vos APK release avec la ligne de commande:

```
gradlew aR
```

Le fichier APK est disponible dans `app/build/outputs/apk/`.