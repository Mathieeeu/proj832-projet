import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordCountReducer implements Reducer {
    private int idReducer;

    public WordCountReducer(int idReducer) {
        this.idReducer = idReducer;
    }

    public void reduce(String outputDir, int idReducer, int nbMappers) throws IOException {
        
        // Map pour compter les occurrences de chaque mot à partir des fichiers de chaque mapper
        Map<String, Integer> wordCount = new HashMap<>();
        
        for (int i = 0; i < nbMappers ; i++) {
            // Pour chaque fichier
            String inputFileName = outputDir + "mapper_" + i + "_reducer_" + idReducer + ".txt";
            
            // On lit le fichier et on compte les mots
            List<String> lines = FileManager.readFile(inputFileName);
            for (String line : lines) {
                String[] parts = line.split("\\s+"); // Séparation des mots s'il y a 1+ espaces
                String word = parts[0];
                int count = Integer.parseInt(parts[1]);
                wordCount.put(word, wordCount.getOrDefault(word, 0) + count);
            }
        }

        // Écriture du résultat dans un fichier de sortie
        try (FileWriter writer = new FileWriter(outputDir + "reducer_" + idReducer + ".txt")) {
            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                String word = entry.getKey();
                int count = entry.getValue();
                writer.write(word + " " + count + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    } 
}
