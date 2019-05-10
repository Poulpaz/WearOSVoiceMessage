# WearOSVoiceMessage

<b>Etape 1 :</b> Installer <a href="https://play.google.com/store/apps/details?id=com.google.android.wearable.app&hl=fr">Wear OS</a> sur le Play Store de votre téléphone.<br />
<b>Etape 2 :</b> Connecter votre montre en Bluetooth via l'application Wear OS.

## Déploiement avec Android Studio

<b> Etape 3 :</b> Activer le débogage Bluetooth dans les paramètres avancés de l'application Wear OS, l'hôte doit être déconnecté et la cible doit être connectée.<br />
<b>Etape 4 :</b> Activer le débogage ADB et le débogage Bluetooth dans les Options pour les développeur de la montre.<br />
<b>Etape 5 :</b> Activer le débogage USB dans les Options pour les développeurs du téléphone.<br />
<b>Etape 6 :</b> Brancher le téléphone à l'ordinateur puis ouvrir Android Studio, cloner le projet dans sa totalité.<br />
<b>Etape 7 :</b> Ouvrir un terminal dans le projet Android Studio puis effectuer les commandes suivantes...<br />
`echo 'export ANDROID_HOME=/Users/$USER/Library/Android/sdk' >> ~/.bash_profile`<br />
`echo 'export PATH=${PATH}:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools' >> ~/.bash_profile`<br />
`source ~/.bash_profile`<br />
`adb devices`<br />
`adb forward tcp:4444 localabstract:/adb-hub`<br />
`adb connect localhost:4444`<br />
<br />Une fois les commandes effectuées, dans les paramètres avancés de l'application Wear OS, l'hôte doit être à l'état de connecté et la cible doit être également connectée.<br />
<b>Etape 8 :</b> Lancer les modules <b>wear</b> et <b>mobile</b> respectivement sur chaque appareil.<br />

## Utilisation simple
Ouvrir l'application.<br />
Cliquer sur le micro<br />
Parler lorsque l'Assistant Google est ouvert (être connecté à Internet)<br />
Attendre la validation ou valider, puis envoyer le message<br />

## Mots utilisables de base
ouvrir youtube, ouvrir musique, ouvrir message

## Ajouter des mots images

Vous pouvez ajouter des images dans les dossiers suivants :<br />
Stockage interne du téléphone puis `/Documents/Watch`<br />
Stockage interne de la montre puis `/Pictures`<br />
Bien penser à renommer les images comme "courgette.png". Après cette étape, on pourra dire "courgette" et on enverra une courgette.

## Vidéo
https://app.box.com/s/1owagpjldz8v05ffp2rowu5y7u0od4a1


