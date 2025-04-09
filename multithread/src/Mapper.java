import java.io.IOException;
import java.util.List;

public interface Mapper {
    void map(List<String> input, String outputDir, int nbReducers) throws IOException;
}