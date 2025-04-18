import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Client {
    private int port;
    private Map<Integer, InetSocketAddress> mappers;
    private Map<Integer, InetSocketAddress> reducers;
    private String inputFileName;
    private String outputDir = "./output/";

    public Client(int port, Map<Integer, InetSocketAddress> mappers, Map<Integer, InetSocketAddress> reducers, String inputFileName) {
        this.port = port;
        this.mappers = mappers;
        this.reducers = reducers;
        this.inputFileName = inputFileName;
    }

    public void launch() throws IOException {

        long startTime = System.currentTimeMillis();
        
        // I/O
        String inputFileName = "./data/bible.txt";
        String finalOutputFileName = outputDir + "wordcount_" + FileManager.getFileName(inputFileName) + ".txt";
        FileManager.deleteFile(finalOutputFileName);

        // Comptage des mappers et reducers
        int nbMappers = mappers.size();
        int nbReducers = reducers.size();
        System.out.println("Comptage de mots sur le fichier " + FileManager.getFileName(inputFileName) + "." + FileManager.getFileExtension(inputFileName) + " avec " + nbMappers + " mappers et " + nbReducers + " reducers.");

        // On prend le fichier texte, on regarde sa taille et on le sépare en m parties
        List<String> lines = FileManager.readFile(inputFileName);
        int totalLines = lines.size();
        int linesPerMapper = totalLines / nbMappers;
        System.out.println("Total de lignes : " + totalLines + ", Lignes par mapper : " + linesPerMapper);

        // On envoie un message à chaque mapper avec l'id du mapper, le nombre de reducers, la portion à mapper
        for (int i = 0; i < nbMappers; i++) {
            int start = i * linesPerMapper;
            int end = (i == nbMappers - 1) ? totalLines : start + linesPerMapper;
            ArrayList<String> portion = new ArrayList<>(lines.subList(start, end));

            InetSocketAddress mapperAddress = mappers.get(i);
            try (Socket socket = new Socket(mapperAddress.getAddress(), mapperAddress.getPort())) {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeInt(i); // ID du mapper
                out.writeInt(nbReducers); // Nombre de reducers
                out.writeObject(portion); // Portion à mapper
                out.flush();
            } catch (IOException e) {
                System.err.println("Erreur lors de l'envoi au mapper " + i + ": " + e.getMessage());
            }
        }
    }
}
