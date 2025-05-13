import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mapper{

    private int idMapper;
    private String outputDir;

    public Mapper(int idMapper, String outputDir) {
        this.idMapper = idMapper;
        this.outputDir = outputDir + "mapping/";
    }
    
    public void map(List<String> input, int nbReducers) throws IOException{
        for (int i = 0; i < nbReducers; i++) {
            String outputFileName = outputDir + "mapper_" + idMapper + "_reducer_" + i + ".txt";
            
            try (FileWriter writer = new FileWriter(outputFileName)) {
                Map<String, Integer> wordCount = new HashMap<>();
                for (String line : input) {
                    String[] words = line.split("[\\s\\.\\\'\\\",!?;:\\-()\\[\\]]+");
                    
                    for (String word : words) {
                        word = word.toLowerCase();
                        if (isItMyJob(word, nbReducers, i)) {
                            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                        }
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

    private boolean isItMyJob(String word, int nbReducers, int idReducer) {
        return word.length() % nbReducers == idReducer;
    }
}