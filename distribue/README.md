# Map Reduce en distribué

## Fonctionnement

- Le programme principal `App` où on peut choisir de lancer un client ou un serveur
- Un client (qui enverra la requête) par défaut sur le port 5001
- Des serveurs qui font le mapping ou le reducing (il peut y avoir deux serveurs qui tournent en même temps sur une machine (avec des ports différents), voire plus avec des ports encore différents)
- Quand on lance un serveur, il affiche son adresse IP et son port
- Dans le code du client, il faut hardcoder l'adresse IP et le port des serveurs
- Le client prend un fichier texte, le coupe en m morceaux (m = nombre de serveurs mappers)
- Ils font le mapping et calculent les % de chaque reducer dans la répartition des mots comptés par le mapping
- Optionnel : Le client calcule les transferts optimaux entre les mappers et les reducers (car certains reducers sont sur les mêmes machines que les mappers) (**pas encore fait**)
- Chaque mapper envoie ses résultats à tous les reducers directement (en prenant les ids des reducers calculés par le client)
- Chaque reducer reçoit les résultats des mappers et fait le reduce
- Les résultats sont envoyés au client qui fusionne tout !

## Structure

- **/data** : répertoire contenant les fichiers .txt qui seront passés au MapReduce
- **/output** : répertoire de sortie dans lequel le résultat final est stocké + les fichiers des étapes intermédiaires (de reducing et mapping)
- **/src** : répertoire contenant le code source Java

- **App.java** : le main (initialisation d'objet, parametres avec des setters et lancement du client ou serveur)
- **FileManager.java** : le programme qui gère les fichiers (lecture, écriture, etc.)
- **Client.java** : le programme du client (ancien _main_ dans la partie multithread)
- **Server.java** : On y lance un serveur et on choisit s'il est mapper ou reducer
- **Mapper.java** : le programme qui fait le mapping
- **Reducer.java** : le programme qui fait le reducing

## Lancement

1. Lancer les mappers (avec la commande `python lauch.py server[numeroDuPort] mapper`) et les reducers (`python lauch.py [numeroDuPort] server`) ATTENTION un mapper/reducer par console
2. Démarrer le client dans une nouvelle console à l'aide de `python lauch.py client [nomDuFichierTxt]`

### Exemple 

- Lancer les mappers sur les ports prédéfinis (hardcodés dans le code de `Client.java`) :

    ```bash
    python lauch.py server 5010 mapper
    python lauch.py server 5011 mapper
    ```

- Lancer les reducers sur les ports prédéfinis (hardcodés dans le code de `Client.java`) :

    ```bash
    python lauch.py server 5020 reducer
    python lauch.py server 5021 reducer
    ```

- Lancer le client avec le fichier texte `qqch.txt` :

    ```bash
    python lauch.py client qqch.txt
    ```
