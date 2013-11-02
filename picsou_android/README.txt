Doc : points de depart
- http://developer.android.com/training/basics/firstapp/index.html

Build
- il faut commencer par installer le SDK Android sur sa machine
- le code de l'application Android se trouve dans un nouveau module picsou_android
- dans picsou_android, creer un repertoire local.properties avec la ligne sdk.dir=<votre chemin sdk android>
- le script "picsou_android/update_libs.sh" copie les jars globs/BV nécessaires dans ./libs
- pour le build en ligne de commande, il faut aller dans picsou_android et lancer "ant debug"

Environnement de test (Robolectric)
- ajouter un export ANDROID_HOME=... dans l'environnement (.bashrc ?)
- installer le pom.xml dans picsou_android, ce qui aura pour effet d'installer Robolectric
- pour retirer les warnings du type "Warning: an error occurred while binding shadow class: ShadowGeoPoint" :
 * soit lancer le SDK Manager dans Tools, télécharger "Google Play Services"
 * soit installer et lancer https://github.com/mosabua/maven-android-sdk-deployer/
 * ensuite ajouter la dépendance projet "maps.jar" qui se trouve dans le repertoire Android, sous add-ons.
- pour lancer les tests, il faut configurer IntelliJ de sorte que le répertoire courant soit picsou_android, dans lequel on trouve AndroidManifest.xml.

Code
- le main() de l'application est BudgetOverviewActivity.onStartup()

Utilisation
- dans le menu "dev" de BudgetView, il faut lancer le menu "Dump XML for android app"
- cela génère un fichier "globsdata.xml" dans picsou_android/res/raw, qui sera lu par l'app
- lancer BudgetOverviewActivity
- sous IntelliJ, il faut lancer la vue du menu View/Tools Windows/Android pour voir les traces

Doc : reference
- http://developer.android.com/guide/components/index.html
- http://developer.android.com/reference/packages.html
