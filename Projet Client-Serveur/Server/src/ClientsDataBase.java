import java.io.*;
import java.util.*;

public class ClientsDataBase {
	
	private static final String File_Path = ".\\Serverfile\\database.txt";
	private static final String Separator = ",";

	// Écrit les username et password dans un database .txt
	public void writeToDB(String username, String password) throws IOException {
		if(databaseIsPresent()) {
			try (BufferedWriter write = new BufferedWriter(new FileWriter(File_Path, true))) {
				write.write(username + Separator + password);
				write.newLine();
			}catch (IOException e) {
				System.out.println("Erreur lors de l'écriture dans le database");
				e.printStackTrace();
			}
		} else {
			System.out.println("Erreur lors de l'écriture dans le database");
		}
	}
	
	// Lis la database et retourne une copie de celle-ci pour être utiliser comme objet dans le code.
	public Map<String,String> readDB() throws IOException {
		Map<String,String> user = new HashMap<>();
		if(databaseIsPresent()) {
			try (BufferedReader read = new BufferedReader(new FileReader(File_Path)))	{
				String line;
				while ((line = read.readLine()) != null) {
					// Split la string en deux partie dans un array, avec le SEPARATOR comme indicateur de séparation
					String[] parts = line.split(Separator);
					if(parts.length == 2) {
						String username = parts[0];
						String password = parts[1];
						user.put(username, password);
					}
				}
			} catch (IOException e) {
				System.out.println("Erreur lors de la lecture du database");
				e.printStackTrace();
			}
		} else {
			System.out.println("Erreur lors de la lecture du database");
		}
		return user;
	}
	
	// Trouver si un username est présent dans le database
	public boolean userPresent(String username) throws Exception{
		try {
		   Map<String, String> savedUsers = readDB();
           for(Map.Entry<String,String> entry : savedUsers.entrySet()) {
        	   if (username.equals(entry.getKey())) {
        		   return true;
        	   }
           }
		} catch(Exception e) {
			System.out.println("Erreur: Impossible de déterminer si le user existe.");
			e.printStackTrace();
		}
		return false;
	}
	
	// Trouver un si un combo username/password se trouve dans le database.
	public boolean goodCredentials(String username, String password) throws Exception {
		try {
		Map<String, String> savedUsers = readDB();
        for(Map.Entry<String,String> entry : savedUsers.entrySet()) {
        	if (username.equals(entry.getKey()) && entry.getValue().equals(password)) {
        		return true;
        	}
        }
		} catch(Exception e) {
			System.out.println("Erreur: Impossible de déterminer si ce sont les bons credentials.");
			e.printStackTrace();
		}
		return false;
	}
	// Détermine si le fichier database existe dans le directory, et le créé si non.
	public boolean databaseIsPresent() {
		File pathIsPresent = new File(File_Path);
		if(!pathIsPresent.exists()) {
			System.out.println("Création du database.txt dans le dossier Serverfile.\n");
			try {
			if(pathIsPresent.createNewFile()) {
				System.out.println("database.txt a été créer correctement.\n");
				return true;
			} else {
				System.out.println("Erreur lors de la création de database.txt.");
				return false;
			}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (pathIsPresent.exists()) {
			return true;
		}
		return false;
	}
}
