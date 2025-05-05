import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Reducer{

    private int idReducer;
    private String outputDir;

    public Reducer(int idReducer, String outputDir){
        this.idReducer = idReducer;
        this.outputDir = outputDir + "reducing/";
    }

    public void reduce(ArrayList<String> lines) throws IOException{

        // Comptage des mots
        Map<String, Integer> wordCount = new HashMap<>();

        for (String line : lines) {
            String[] parts = line.split("\\s+"); // Séparation des mots s'il y a 1+ espaces
            String word = parts[0];
            int count = Integer.parseInt(parts[1]);
            wordCount.put(word, wordCount.getOrDefault(word, 0) + count);
        }

        // Écriture du résultat dans un fichier de sortie
        String outputFileName = outputDir + "reducer_" + idReducer + ".txt";
        try (FileWriter writer = new FileWriter(outputFileName)) {
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