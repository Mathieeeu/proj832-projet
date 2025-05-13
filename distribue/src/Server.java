import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;

public class Server {
    private int port;
    private String type; // "mapper" ou "reducer"
    private String outputDir = "./output/"; // Répertoire de sortie pour les fichiers de sortie
    private int clientPort = 5001;

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
                // Attendre une connexion d'un client
                Socket clientSocket = serverSocket.accept();
                InetAddress clientAddress = clientSocket.getInetAddress();
                System.out.println("Connexion acceptée de " + clientAddress + ":" + clientPort);

                // mapping
                try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
                    int idMapper = in.readInt(); // ID du mapper
                    int nbMappers = in.readInt();
                    int nbReducers = in.readInt(); // Nombre de reducers
                    ArrayList<String> input = (ArrayList<String>) in.readObject(); // Portion à mapper
                    System.out.println("Mapper " + idMapper + " a reçu une portion de " + input.size() + " lignes à mapper.");
                    
                    // Appeler la méthode map pour traiter la portion reçue
                    Mapper mapper = new Mapper(idMapper, outputDir);
                    mapper.map(input, nbReducers);

                    // Envoi des résultats de mapping au client (juste la taille des fichiers de sortie pour éviter de trop charger le réseau)
                    // Format de la réponse : "m0r0s12_m0r1s14_" s'il y a deux reducers (0 et 1) et des fichiers de tailles 12 et 14.
                    String message = "";
                    for (int idReducer = 0; idReducer < nbReducers; idReducer++) {
                        String fileName = outputDir + "mapping/" +"mapper_" + idMapper + "_reducer_" + idReducer + ".txt";
                        int fileSize = FileManager.getNbLines(fileName);
                        System.out.println("map" + idMapper + " -> red" + idReducer + " : " + fileSize);
                        message += "m" + idMapper + "r" + idReducer + "s" + fileSize + "_";
                    }
                    try(Socket socket = new Socket(clientAddress, clientPort)) {
                        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                        out.writeObject(message);
                        out.flush();
                    } catch (IOException e) {
                        System.err.println("Erreur lors de l'envoi des résultats du mapping à " + clientAddress + ":" + clientPort + " : " + e.getMessage());
                        return;
                    }

                    // Recevoir le message du client avec les ids et adresses des reducers
                    Socket clientSocket2 = serverSocket.accept();
                    Map<Integer, InetSocketAddress> reducerAddresses = null;
                    try (ObjectInputStream in2 = new ObjectInputStream(clientSocket2.getInputStream())) {
                        reducerAddresses = (Map<Integer, InetSocketAddress>) in2.readObject();
                        System.out.println("Réception des adresses des reducers : " + reducerAddresses);
                    } catch (ClassNotFoundException e) {
                        System.err.println("Erreur lors de la désérialisation de l'objet : " + e.getMessage());
                        return;
                    } finally {
                        clientSocket2.close();
                    }
                    if (reducerAddresses == null) {
                        System.err.println("Erreur lors de la réception des adresses des reducers.");
                        return;
                    }

                    // Envoyer les comptes aux reducers
                    for (int idReducer = 0; idReducer < nbReducers; idReducer++) {
                        InetSocketAddress reducerAddress = reducerAddresses.get(idReducer);
                        System.out.println("Envoi des résultats de mapping au reducer " + idReducer + " : " + reducerAddress.getAddress() + ":" + reducerAddress.getPort());
                        try (Socket socket = new Socket(reducerAddress.getAddress(), reducerAddress.getPort())) {
                            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                            String fileName = outputDir + "mapping/" + "mapper_" + idMapper + "_reducer_" + idReducer + ".txt";
                            ArrayList<String> lines = (ArrayList<String>) FileManager.readFile(fileName);
                            out.writeObject(clientAddress);
                            out.writeInt(nbMappers);
                            out.writeInt(idReducer);
                            out.writeObject(lines); // Envoi de la liste de lignes au reducer
                            out.flush();
                            System.out.println("Envoi des résultats de mapping au reducer " + idReducer + " : " + lines.size() + " lignes.");
                        } catch (IOException e) {
                            System.err.println("Erreur lors de l'envoi des résultats au reducer " + idReducer + ": " + e.getMessage());
                            return;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Erreur lors de la désérialisation de l'objet : " + e.getMessage());
                } finally {
                    // Opération terminée
                    clientSocket.close();
                    System.out.println("Mapping terminé !\n");
                }
            }   
        } catch (IOException e) {
            System.err.println("Erreur lors de l'exécution du serveur Mapper : " + e.getMessage());
        }
    }

    private void runReducerServer() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Reducer en attente de connexion sur le port " + port + "...");
            int nbMappers = 9999999;
            int idReducer = -1;
            InetAddress clientAddress = null;
            while (true) {       
                // Réception des lignes à traiter à partir de plusieurs mappers
                ArrayList<String> allLines = new ArrayList<>();
                int responsesRecieved = 0;
                while (responsesRecieved < nbMappers) {
                    // Attendre une connexion d'un client
                    Socket mapperSocket = serverSocket.accept();
                    InetAddress mapperAddress = mapperSocket.getInetAddress();
                    int mapperPort = mapperSocket.getPort();
                    System.out.println("Connexion acceptée de " + mapperAddress + ":" + mapperPort);
                    try (ObjectInputStream in = new ObjectInputStream(mapperSocket.getInputStream())) {
                        clientAddress = (InetAddress) in.readObject();
                        nbMappers = in.readInt();
                        idReducer = in.readInt();
                        ArrayList<String> lines = (ArrayList<String>) in.readObject(); // Portion à réduire
                        allLines.addAll(lines);
                        // System.out.println("Reducer a reçu " + lines.size() + " lignes à réduire.");
                        responsesRecieved++;
                        mapperSocket.close();
                    } catch (ClassNotFoundException e) {
                        System.err.println("Erreur lors de la désérialisation de l'objet : " + e.getMessage());
                        return;
                    }
                }
                if (idReducer == -1) {
                    System.err.println("Erreur lors de la réception des lignes à réduire.");
                    return;
                }
                System.out.println("Réduction de " + allLines.size() + " lignes.");

                // Opération du reducing
                Reducer reducer = new Reducer(idReducer, outputDir);
                try {
                    reducer.reduce(allLines);
                } catch (IOException e) {
                    System.err.println("Erreur lors de l'exécution du reducer : " + e.getMessage());
                }

                // Envoi du résultat au client (tous les mots)
                try (Socket clientSocket = new Socket(clientAddress, clientPort)) {
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    String fileName = outputDir + "reducing/" + "reducer_" + idReducer + ".txt";
                    ArrayList<String> lines = (ArrayList<String>) FileManager.readFile(fileName);
                    out.writeObject(lines);
                    out.flush();
                    System.out.println("Envoi des résultats du reducer " + idReducer + " au client (" + lines.size() + " lignes)");
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Erreur lors de l'ouverture du socket : " + e.getMessage());
                    return;
                } finally {
                    // Opération terminée
                    System.out.println("Réduction terminée !\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'exécution du serveur Reducer : " + e.getMessage());
        }
    }
}