import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {

        long startTime = System.currentTimeMillis();
        
        // I/O
        String inputFileName = "./multithread/data/bible.txt";
        String outputDirBase = "./multithread/output/";
        String outputDir = outputDirBase + FileManager.getFileName(inputFileName) + "_mapreduce/";
        String finalOutputFileName = outputDirBase + "wordcount_" + FileManager.getFileName(inputFileName) + ".txt";
        FileManager.deleteDirectory(outputDir);
        FileManager.createDirectory(outputDir);
        FileManager.deleteFile(finalOutputFileName);
        
        // On crée m mappers et n reducers
        int nbMappers = 5;
        int nbReducers = 3;
        System.out.println("Comptage de mots sur le fichier " + FileManager.getFileName(inputFileName) + "." + FileManager.getFileExtension(inputFileName) + " avec " + nbMappers + " mappers et " + nbReducers + " reducers.");

        // Création des instances pour les mappers
        List<Mapper> mappers = new ArrayList<>();
        for (int i = 0; i < nbMappers; i++) {
            mappers.add(new WordCountMapper(i));
        }

        // On prend le fichier texte, on regarde sa taille et on le sépare en m parties
        List<String> lines = FileManager.readFile(inputFileName);
        int totalLines = lines.size();
        int linesPerMapper = totalLines / nbMappers;

        // Multithread pour les mappers
        List<Thread> mapperThreads = new ArrayList<>();
        for (int i = 0; i < nbMappers; i++) {
            int start = i * linesPerMapper;
            int end = (i == nbMappers - 1) ? totalLines : start + linesPerMapper;
            List<String> portion = lines.subList(start, end);

            int mapperIndex = i;
            Thread mapperThread = new Thread(() -> {
                try {
                    mappers.get(mapperIndex).map(portion, outputDir, nbReducers);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            mapperThreads.add(mapperThread);
            mapperThread.start();
        }

        // Attendre la fin des threads de mapping
        for (Thread thread : mapperThreads) {
            thread.join();
        }

        // On crée n reducers
        List<Reducer> reducers = new ArrayList<>();
        for (int i = 0; i < nbReducers; i++) {
            reducers.add(new WordCountReducer(i));
        }
        
        // Multithread pour les reducers
        List<Thread> reducerThreads = new ArrayList<>();
        for (int i = 0; i < nbReducers; i++) {
            int reducerIndex = i;
            Thread reducerThread = new Thread(() -> {
                try {
                    reducers.get(reducerIndex).reduce(outputDir, reducerIndex, nbMappers);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            reducerThreads.add(reducerThread);
            reducerThread.start();
        }
        
        // Attendre la fin des threads de réduction
        for (Thread thread : reducerThreads) {
            thread.join();
        }

        // Fusion des résultats des reducers dans un seul fichier de sortie, triés par ordre décroissant
        try (FileWriter writer = new FileWriter(finalOutputFileName)) {
            List<String> allLines = new ArrayList<>();
            for (int i = 0; i < nbReducers; i++) {
                String reducerOutputFileName = outputDir + "reducer_" + i + ".txt";
                List<String> linesFromReducer = FileManager.readFile(reducerOutputFileName);
                allLines.addAll(linesFromReducer);
            }

            // Trier les lignes par ordre décroissant
            allLines.sort((line1, line2) -> {
                int count1 = Integer.parseInt(line1.split("\\s+")[1].trim());
                int count2 = Integer.parseInt(line2.split("\\s+")[1].trim());
                return Integer.compare(count2, count1); // Tri décroissant
            });

            // Écrire les lignes triées dans le fichier final
            for (String line : allLines) {
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Statistiques de nerd
        long endTime = System.currentTimeMillis();
        System.out.println("Map-reduce terminé en " + ((endTime - startTime) / 1000.0) + " secondes.");
        System.out.println("Résultats écrits dans " + finalOutputFileName + ".");
    }
}

/**
 * On veut quoi ?
 * - On prend un fichier texte en entrée et on crée m mappers et n reducers
 * - Le programme principal regarde la taille du fichier, et calcule les portions que devront traiter chaque mapper
 * - Le programme principal fait travailler les mappers sur leur portion
 * - Les mappers écrivent leurs propres résultats dans des fichiers (un par couple mapper-reducer (mapper_m_reducer_n.txt)). Les mots traités dans chaque fichier sont ceux qui appartiennent au domaine du reducer (avec la fonction isItMyJob() de Mapper)
 * - MAPPING TERMINE  :-)
 *  
 * - On veut faire la même chose avec les reducers :
 * - On prend les fichiers de sortie des mappers et on compte les occurrences de chaque mot (dans une liste ou une map?)
 * - Les reducers renvoient leurs résultats dans un fichier de sortie chacun dans le répertoire de sortie (output/<nom du fichier d'entrée>/)
 * - Le programme principal attend la fin de tous les reducers et écrit les résultats finaux dans un fichier de sortie
 * - Avec un petit tri par ordre décroissant pour le style en plus B-)
 * - REDUCING TERMINE  :-h
 * 
 * PROBLEME : 
 * - Les mappers doivent faire les fichiers d'entrée des reducers directement pour pas que les reducers aient à lire tous les fichiers (le nom sera mapper_n_reducer_m.txt) donc la logique isItMyJob() doit être dans le mapper et pas dans le reducer (corrigé)
 * - Il faut quand meme garder la version actuelle car elle est interessante à utiliser avec un seul reducer (et sans la tache isItMyJob() du coup) pour comparer les performances à l'avenir (corrigé, il suffit de mettre nbReducers à 1 dans le Main)
 */