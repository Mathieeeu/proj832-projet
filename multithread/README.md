# Map Reduce en Multithread

## Structure

- **/data** : répertoire contenant les fichiers .txt qui seront passés au MapReduce
- **/output** : répertoire de sortie dans lequel le résultat final est stocké + les fichiers des étapes intermédiaires (de reudcing et mapping)
- **/src** : répertoire contenant le code source Java

- **Main.java** : Point d'entrée du programme.
- **Mapper.java** : Interface pour le Mapper.
- **Reducer.java** : Interface pour le Reducer.
- **WordCountMapper.java** : Implémentation complete du Mapper pour le comptage de mots.
- **WordCountReducer.java** : Implémentation complète du Reducer pour le comptage de mots.
- **FileManager.java** : Gère la lecture et l'écriture des fichiers.

## Fonctionnement

Chaque étape est commentée dans le code.

## Lancement

Le fichier à traiter est hardcodé dans le code source. Il faut donc modifier la ligne pour changer de fichier.
Il faut juste compiler/exécuter le fichier `Main.java`.