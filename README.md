# PROJ832 - Comptage de mots

## Consignes

**Compteur de mots à la map-reduce**

Il s’agit ici de reproduire le comportement d’Hadoop sur l’exemple de comptage de mots vu en cours. Vous ne devez pas utiliser la plateforme Hadoop, mais concevoir une plateforme distribuée de comptage de mots basée sur map-reduce (à programmer avec des Sockets ou du RMI par exemple). En entrée : un ensemble de fichiers texte contenant des mots ; en sortie : un dictionnaire comportant l’ensemble des mots associés au compteur de leur nombre d’occurrences. Dans une première phase, une tache par fichier va compter les nombres d’occurrences des mots du fichier qu’elle a reçue (MAP), dans un deuxième temps un nombre fixé de taches « reduce » vont récupérer chez chacunes des taches map le sous ensemble des mots dont elle est « responsable » et comptabiliser pour chacun des ces mots, le nombre total d’occurrences. Le niveau de difficulté peut se « régler » : 

- En fixant ou non de nombreux paramètres comme : le nombre de taches maps, la répartition des mots sur les taches reduces
- En prenant en compte ou non les fautes de taches map et réduce… (monitoring + relance / exécution spéclative…)
- Selon le niveau d’implémentation (socket/rmi/mono-processus multithreadé…)

**Deux approches possibles** (on peut faire des deux d'affilée dans l'idéal, ou juste une)

- Multithread en java
- Avec plusieurs processus en java (ou python, ou c) avec des communications par sockets ou RMI ou systèmes de fichiers
    - **ça marche à peu près comme ça :**
    - en gros, la machine "utilisateur" prend un gros fichier et le découpe en n plus petits fichiers (n défini à l'avance)
    - il y a n mappers qui tournent sur d'autres machines (ou localhost) et écoutent les sockets du processus de l'utilisateur
    - l'utilisateur envoie à chaque mapper son bout de fichier et ils font leurs tâches
    - _quand ils ont fini, ils envoient au processus utilisateur le nombre de leurs données qui seront assignées à chaque reducer (par exemple si on a 2 reducers, un qui prend tous les mots qui commencent par `<m` et l'autre par tous les mots qui commencent par `>m`, les mappers vont compter ça et l'utilisateur enverra donc la tâche vers un reducer sur la machine qui aura le plus à faire (genre si 70% des mots comptés par map1 vont vers le red2, on va dire que red2 est le reducer de la meme machine que map1 t'as capté))_ ∗ ∗∗
    - _l'utilisateur en déduit sur quel reducer ils doivent envoyer leurs données (pour minimiser les échanges entre machines)_ ∗
    - les reducers font leur job
    - les reducers envoient le résultat à l'utilisateur

    ∗ ces étapes peuvent être sautées et remplacées par les données qui passent juste de mapper à utilisateur puis à reducer sans chercher à optimiser les échanges.
  
    ∗∗ on peut tester différentes fonctions de séparation du travail pour les reducers (par position de lettres, par modulo du code de la premiere lettre, par modulo du hashage du mot, par modulo de la taille du mot...) -> en théorie le plus opti c'est le modulo du hashage (car quasi-équilibré) mais il peut prendre beaucoup plus longtemps qu'un truc plus simple donc faut en test plusieurs
