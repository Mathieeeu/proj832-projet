import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordCountMapper implements Mapper {
    private int idMapper;

    public WordCountMapper(int idMapper) {
        this.idMapper = idMapper;
    }

    @Override
    public void map(List<String> input, String outputDir, int nbReducers) throws IOException {
        for (int i = 0; i < nbReducers; i++) {
            String outputFileName = outputDir + "mapper_" + idMapper + "_reducer_" + i + ".txt";

            try (FileWriter writer = new FileWriter(outputFileName)) {
                for (String line : input) {
                    String[] words = line.split("[\\s\\.\\\'\\\",!?;:\\-()\\[\\]]+"); // Séparation des mots s'il y a 1+ espaces
                    
                    Map<String, Integer> wordCount = new HashMap<>();
                    for (String word : words) {
                        if (isItMyJob(word, nbReducers, i)) {
                            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                        }
                    }
                    for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                        String word = entry.getKey();
                        int value = entry.getValue();
                        writer.write(word + " " + value + "\n");
                    }
                }
            }
        }
    }

    private boolean isItMyJob(String word, int nbReducers, int idReducer) {
        return word.length() % nbReducers == idReducer;
    }
}