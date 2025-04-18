import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {
    private int port;
    private String type; // "mapper" ou "reducer"
    private String outputDir = "./output/"; // Répertoire de sortie pour les fichiers de sortie

    public Server(int port, String type) {
        this.port = port;
        this.type = type;
    }

    public void launch() throws IOException {
        switch (type) {
            case "mapper" -> runMapperServer();
            case "reducer" -> runReducerServer(); 
            default -> System.out.println("Type de serveur inconnu : " + type);
        }
    }

    private void runMapperServer() throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Mapper en attente de connexion sur le port " + port + "...");
            while (true) {
                // Attendre une connexion d'un client et afficher le message reçu
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connexion acceptée de " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
                    int idMapper = in.readInt(); // ID du mapper
                    int nbReducers = in.readInt(); // Nombre de reducers
                    ArrayList<String> input = (ArrayList<String>) in.readObject(); // Portion à mapper
                    System.out.println("Mapper " + idMapper + " a reçu une portion de " + input.size() + " lignes à mapper.");
                    
                    // TODO : Appeler la méthode map pour traiter la portion reçue
                    Mapper mapper = new Mapper(idMapper, outputDir);
                    mapper.map(input, nbReducers);

                    // TODO : calculer les % de répartition des mots par reducer et l'envoyer au client pour qu'il cherche la meilleure distribution des ids reducers
                    

                    // TODO : Recevoir le message du client avec les ids et adresses des reducers

                    // TODO : Envoyer les comptes aux reducers
                    

                } catch (ClassNotFoundException e) {
                    System.err.println("Erreur lors de la désérialisation de l'objet : " + e.getMessage());
                } finally {
                    clientSocket.close();
                }

            }   
        } catch (IOException e) {
            System.err.println("Erreur lors de l'exécution du serveur Mapper : " + e.getMessage());
        }
    }

    private void runReducerServer() throws IOException {
        //TODO
    }
}