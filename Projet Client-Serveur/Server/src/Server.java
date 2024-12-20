import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.*;

public class Server {
private static ServerSocket Listener;
    
    public static void main (String[] args) throws Exception {
        
        int clientNumber = 0;
        
        String serverAddress = "";
        int serverPort = 0;
        
        Scanner readObject = new Scanner(System.in);
		IpAddressChecker ip = new IpAddressChecker();
		
		// Input de l'addresse IP et vérification
		System.out.println("Entrer l'adresse IP du poste sur lequel le serveur s'exécute et un numéro de port d'écoute:");
		System.out.println("Adresse IP du serveur: ");
		serverAddress = readObject.nextLine();
		while (!ip.ipAddressEstValide(serverAddress)) {
			System.out.println("\nVeuillez entrer une adresse IP valide.");
			serverAddress = readObject.next();
		}
		
		// Input du numéro de port et vérification
		while (serverPort < 5000 | serverPort > 5050) {
			System.out.println("Entrer le numéro de Port d'écoute (entre 5000 et 5050): ");
			if (readObject.hasNextInt()) {
			serverPort = readObject.nextInt();
				if (serverPort < 5000 | serverPort > 5050) {
					System.out.println("\nLe numéro de port doit être entre 5000 et 5050 inclusivement.");
				}
			} else {
			System.out.println("\nVeuillez entrer des nombres seulement.");
			readObject.next();
			}
		}
		readObject.close();
      
		// Ouverture du socket et vérification de l'IP
        Listener = new ServerSocket();
        Listener.setReuseAddress(true);
        InetAddress serverIP = InetAddress.getByName(serverAddress);
        Listener.bind(new InetSocketAddress(serverIP, serverPort));
        System.out.format("\nThe server is running on %s:%d%n", serverAddress, serverPort);
        
        try {
        
            while(true) {
                
                new ClientHandler(Listener.accept(), clientNumber++).start();
        
            }}
            finally {
            Listener.close();
        }
    }
}