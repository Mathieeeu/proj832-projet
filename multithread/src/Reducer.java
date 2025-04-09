import java.io.IOException;

public interface Reducer {
    void reduce(String outputDir, int idReducer, int nbMappers) throws IOException;
}
