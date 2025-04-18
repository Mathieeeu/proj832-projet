# Ça c'est le readme sur comment fonctionne le programme de map reduce distribué

DFS chiant à faire si c'est pas sur le réseau local de l'univ
RMI transparent mais supporte très mal les fautes et peu avoir des limites en terme de volume de données
sockets c cool, en java il y a des trucs pour envoyer de gros fichiers (encapsulateurs de flux)

Fonctionnement : 
- Un Main où on peut choisir de lancer un client ou un serveur
- Un client (qui enverra la requête)
- Des serveurs qui font le mapping ou le reducing (il peut y avoir deux serveurs qui tournent en même temps sur une machine (avec des ports différents), voire plus avec des ports encore différents)
- Quand on lance un serveur, il affiche son adresse IP et son port
- Dans le code du client, il faut hardcoder l'adresse IP et le port des serveurs
- Le client prend un fichier texte, le coupe en m morceaux (m = nombre de serveurs mappers)
- Ils font le mapping et calculent les % de chaque reducer dans la répartition des mots comptés par le mapping
- Le client calcule les transferts optimaux entre les mappers et les reducers (car certains reducers sont sur les mêmes machines que les mappers)
- Chaque mapper envoie ses résultats à tous les reducers directement (en prenant les ids des reducers calculés par le client)
- Chaque reducer reçoit les résultats des mappers et fait le reduce
- Les résultats sont envoyés au client qui fusionne tout !

Structure : 
- App.java : le main (doit faire 3 lignes environ (initialisation d'objet, parametres avec des setters et lancement du client ou serveur))
- FileManager.java : le programme qui gère les fichiers (lecture, écriture, etc.)
- SocketManager.java : le programme qui gère les sockets (envoi, réception, etc.)
- Client.java : le programme du client (ancien main dans la partie multithread)
- Server.java : On y lance un serveur et on choisit s'il est mapper ou reducer
- Mapper.java : le programme qui fait le mapping
- Reducer.java : le programme qui fait le reducing
