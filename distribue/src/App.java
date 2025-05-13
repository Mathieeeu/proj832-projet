import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class App {
    public static void main(String[] args) throws UnknownHostException {
        System.out.println("\u001B[38;5;33mMapReduce\u001B[0m");

        if (args.length == 0) {
            System.out.println("Aucun argument fourni.");
            return;
        }

        String appType = args[0];

        FileManager.deleteDirectory("./output/");
        FileManager.createDirectory("./output/");
        FileManager.createDirectory("./output/mapping/");
        FileManager.createDirectory("./output/reducing/");

        switch (appType.toLowerCase()) {
            case "client" -> {
                System.out.println("Mode Client sélectionné.");

                Map<Integer, InetSocketAddress> mappers = new HashMap<>();
                Map<Integer, InetSocketAddress> reducers = new HashMap<>();

                mappers.put(0, new InetSocketAddress("localhost", 5010));
                mappers.put(1, new InetSocketAddress("localhost", 5011));
                // mappers.put(2, new InetSocketAddress("localhost", 5012));
                // mappers.put(3, new InetSocketAddress("localhost", 5013));

                reducers.put(0, new InetSocketAddress("localhost", 5020));
                reducers.put(1, new InetSocketAddress("localhost", 5021));
                // reducers.put(2, new InetSocketAddress("localhost", 5022));


                // Vérification de la présence du fichier d'entrée
                String inputFile = "./data/" + args[1] + ".txt";
                if (args.length < 2) {
                    System.out.println("Le nom du fichier d'entrée est manquant.");
                    return;
                }
                if (args[1] == null || FileManager.exists(inputFile) == false) {
                    System.out.println("Le fichier " + inputFile + " est manquant ou n'existe pas.");
                    return;
                }
                System.out.println("Fichier d'entrée : " + inputFile);
                Client client = new Client(5001, mappers, reducers, inputFile);

                try {
                    client.launch();
                } catch (Exception e) {
                    System.err.println("Erreur lors du lancement du client : " + e.getMessage());
                }
            }

            case "server" -> {
                if (args.length < 3) {
                    System.out.println("Pour le mode serveur, le port et le type de serveur sont obligatoires.");
                    return;
                }
                System.out.println("Mode Serveur sélectionné.");
                String port = args[1];
                String serverType = args[2].toLowerCase(); // "mapper" ou "reducer"
                System.out.println("Adresse IP : " + InetAddress.getLocalHost().getHostAddress());
                System.out.println("Port : " + port);
                System.out.println("Type de serveur : " + serverType);

                Server server = new Server(Integer.parseInt(port), serverType);
                try {
                    server.launch();
                } catch (Exception e) {
                    System.err.println("Erreur lors du lancement du serveur : " + e.getMessage());
                }
            }

            default -> System.out.println("Type d'application non reconnu. Utilisez 'client' ou 'server' svp");
        }
    }
}
