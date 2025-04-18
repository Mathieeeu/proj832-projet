import subprocess
import sys
import os

def compile_java():
    # Créer le répertoire de sortie s'il n'existe pas
    output_dir = "src/classes"
    os.makedirs(output_dir, exist_ok=True)

    # Compiler tous les fichiers Java dans le répertoire src vers le répertoire de sortie
    compile_command = ["javac", "src/*.java", "-d", output_dir]
    try:
        subprocess.run(compile_command, check=True)
        print("Compilation réussie.")
    except subprocess.CalledProcessError as e:
        print(f"Erreur lors de la compilation : {e}")
        sys.exit(1)

def cleanup_class_files():
    # Supprimer les fichiers .class générés dans le répertoire de sortie
    output_dir = "src/classes"
    for root, dirs, files in os.walk(output_dir):
        for file in files:
            if file.endswith(".class"):
                os.remove(os.path.join(root, file))
    # print("Fichiers .class supprimés.")

def launch_app(app_type, port=None, server_type=None):
    # Construire la commande Java en utilisant le répertoire de sortie comme classpath
    output_dir = "src/classes"
    command = ["java", "-cp", output_dir, "App"]

    # Ajouter les arguments en fonction du type
    if app_type == "client":
        command.append("client")
    elif app_type == "server":
        if port is None or server_type is None:
            print("Pour le type 'server', le port et le type de serveur sont obligatoires.")
            return
        command.extend(["server", str(port), server_type])
    else:
        print("Type invalide. Utilisez 'client' ou 'server'.")
        return

    # Exécuter la commande
    try:
        subprocess.run(command, check=True)
    except subprocess.CalledProcessError as e:
        print(f"Erreur lors de l'exécution de l'application : {e}")

if __name__ == "__main__":
    cleanup_class_files()
    if len(sys.argv) < 2:
        print("Usage: python launch.py <client|server> [<port> <server_type>]")
    else:
        app_type = sys.argv[1]
        if app_type == "server":
            if len(sys.argv) < 4:
                print("Pour le type 'server', le port et le type de serveur sont obligatoires.")
            else:
                port = sys.argv[2]
                server_type = sys.argv[3]
                compile_java()
                launch_app(app_type, port, server_type)
        else:
            compile_java()
            launch_app(app_type)
