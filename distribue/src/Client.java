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

    public void launch() throws IOException, ClassNotFoundException {

        long startTime = System.currentTimeMillis();
        
        // I/O
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
                out.writeInt(nbMappers);
                out.writeInt(nbReducers); // Nombre de reducers
                out.writeObject(portion); // Portion à mapper
                out.flush();
            } catch (IOException e) {
                System.err.println("Erreur lors de l'envoi au mapper " + i + ": " + e.getMessage());
                return;
            }
        }

        // On attend la réponse de tous les mappers (elles contiendront juste la répartition des mots par couple mapper/reducer)
        // Format de la réponse : "m0r0s12_m0r1s14_m1r0s11_m1r1s20_" avec deux mappers (0 et 1) et deux reducers et des fichiers de tailles 12, 14, 11 et 20.
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Client en attente de réponses sur le port " + port + "...");
            int responsesRecieved = 0;
            String mapperResponses = "";
            while (responsesRecieved < nbMappers) {
                // Attendre une connexion d'un mapper
                Socket clientSocket = serverSocket.accept();
                InetAddress clientAddress = clientSocket.getInetAddress();
                int clientPort = clientSocket.getPort();
                System.out.println("Connexion acceptée de " + clientAddress + ":" + clientPort);

                try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
                    mapperResponses += (String) in.readObject();
                }
                clientSocket.close();
                responsesRecieved++;
            }
            System.out.println("Réponses des mappers : " + mapperResponses);
            // TODO : à faire en option (c'est ici qu'on pourrait modifier les ids des reducers pour qu'ils soit optis)
        } catch (IOException e) {
            System.err.println("Erreur lors de la réception des résultats du mapping : " + e.getMessage());
            return;
        }

        // TODO : On donne les adresses des reducers aux mappers (pour qu'ils sachent où envoyer les résultats)
        for (int i = 0; i < nbMappers; i++) {
            InetSocketAddress mapperAddress = mappers.get(i);
            try (Socket socket = new Socket(mapperAddress.getAddress(), mapperAddress.getPort())) {
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(reducers);
                out.flush();
            } catch (IOException e) {
                System.err.println("Erreur lors de l'envoi des adresses des reducers au mapper " + i + ": " + e.getMessage());
                return;
            }
        }

        // TODO : On attend la réponse de tous les reducers (elle contiendra leur compte final de mots) puis faire la fusion :)
        
        
    }
}
