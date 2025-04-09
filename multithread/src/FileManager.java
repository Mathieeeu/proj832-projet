import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    
    /**
     * Lit un fichier et retourne une liste de lignes.
     * @param fileName le nom du fichier à lire
     * @return une liste de lignes
     * @throws IOException si une erreur d'entrée/sortie se produit
     */
    public static List<String> readFile(String fileName) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static String getFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
    }

    public static String getFileExtension(String filePath) {
        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }

    public static void createDirectory(String dirPath) {
        java.io.File dir = new java.io.File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void deleteDirectory(String dirPath) {
        java.io.File dir = new java.io.File(dirPath);
        if (dir.exists()) {
            for (java.io.File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    deleteDirectory(file.getAbsolutePath());
                } else {
                    file.delete();
                }
            }
            dir.delete();
        }
    }

    public static void deleteFile(String filePath) {
        java.io.File file = new java.io.File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}
