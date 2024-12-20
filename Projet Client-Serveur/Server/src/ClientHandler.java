import java.io.*;
import java.net.Socket;
import java.time.*;
import java.time.temporal.ChronoUnit; 

public class ClientHandler extends Thread {
    private Socket socket;
    private int clientNumber;
    private static String serverImageFilePath= ".\\Serverfile\\";
    public ClientHandler(Socket socket, int clientNumber) {
        this.socket = socket;
        this.clientNumber = clientNumber;
        System.out.println("New connection with client#" + clientNumber + " at " + socket);
    }
    public void run() {
    	
        try {
        	// Initialise l'objet database pour pouvoir utiliser ses méthodes.
        	ClientsDataBase DB = new ClientsDataBase();
        	boolean loggedIn = false;
        	
        	// S'assure que le dossier pour les serverFile existe
        	File pathIsPresent = new File(serverImageFilePath);
    		if(!pathIsPresent.exists()) {
    			System.out.println("\nCréation du dossier Serverfile dans le directory actuel.");
    			if(pathIsPresent.mkdir()) {
    				System.out.println("Serverfile a été créer correctement. Veuillez utiliser ce dossier comme répertoire pour envoyer/recevoir vos fichiers.\n");
    			} else {
    				System.out.println("Erreur lors de la création du dossier Serverfile.");
    			}
    		} else {
    			System.out.println("\nNote: Le dossier Serverfile est utilisé par défaut pour la réception/envoie des données serveur.\n");
    		}
    		
    		// Crée les flux in et out pour l'échange de données avec les clients.
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            
            // Créer un objet ImageHandler pour utiliser ses méthodes lors de l'interaction serveur/client
            ImageHandler imageHandler = new ImageHandler();
       		imageHandler.setDataIn(in);
       		imageHandler.setDataOut(out);
            
            //Welcome message
            out.writeUTF("Hello from server - you are client# " + clientNumber + "\n");
            
            // Variable pour identification du client && compteur d'essais.
            String username = in.readUTF();
            String password = in.readUTF();
            int attemptNumber = 0;
            
            // Boucle d'identification jusqu'à 4 mauvais password max.
            while (!loggedIn) {
            	try {
            	// Vérifie si le username est présent dans la base de donné
	           	if(DB.userPresent(username)) {
	           		// Vérifie si le combo username/password est correct
	            	loggedIn = DB.goodCredentials(username, password);
	            		if(loggedIn) {
	            			System.out.println("Connexion Réussie avec le client#" + clientNumber);
	            			out.writeUTF("Connected");
	            		} else {
	            			System.out.println("Invalid Credientials. Connexion refused for client#" + clientNumber);
	            			out.writeUTF("Mauvais mot de passe. Veuillez réessayer.");
	            			password = in.readUTF();
	            		}
	            	} else {
	            	DB.writeToDB(username, password);
	            	System.out.println("Le nouveau compte client a été créé.");
	            	out.writeUTF("Created");
	            	loggedIn = true;
	            }
	           	if (attemptNumber++ > 2) {
	           		System.out.println("Too many invalid Credientials. Connexion terminated for client#" + clientNumber);
	           		out.writeUTF("Trop d'essais infructueux. Vous allez être déconnecté.");
	           		break;
	           	}
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            } 
            
            // Recoit le(s) fichier(s) lorsque le client est identifié correctement et connecté.
           	while(loggedIn) {
           		String receivedImageName = in.readUTF();
           		imageHandler.saveImage(imageHandler.receiveImage(), serverImageFilePath + receivedImageName);
           		
           		File receivedImage = new File(serverImageFilePath + receivedImageName);
           		boolean isReceived = receivedImage.exists();
           		String isReceivedTxt = "L'image a bien été recue par le serveur. Elle se trouve dans le directory suivant: " ;
           		String isNotReceivedTxt = "L'image n'a pas bien été recue par le serveur." ;
           		String receivedImagePath = receivedImage.getPath();
           		
           		if(isReceived) {
           			// Envoie le txt de réception.
           			out.writeUTF(isReceivedTxt + receivedImagePath); 
           			
           			// Affiche message de réception dans le serveur
               		String serverInfo = (socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());
               		LocalDate today = LocalDate.now();
               		LocalTime rightNow = LocalTime.now().truncatedTo(ChronoUnit.SECONDS);
               		System.out.println("\n[" + username + " - " + serverInfo + " - " + today + "@" + rightNow + "] : Image " + receivedImageName + " reçue pour traitement.\n");
           
               		// Traite le fichier avec l'algo et l'envoie au client          	
               		imageHandler.SendTreatedImage(receivedImage);
               		
           		} else {
           			System.out.println("Error handling client#" + clientNumber + ": image have not been received properly. ");
           			out.writeUTF(isNotReceivedTxt);
           		}	
           		// Attend de savoir si le client veux envoyer un autre fichier.
           		String treatAnotherFile = in.readUTF();
           		if(!treatAnotherFile.equals("oui")) {
           			loggedIn = false;
           		}
           	}
                   	
        }
        catch(IOException e) {
            System.out.println("Error handling client#" + clientNumber + ": " + e);
        } 
        finally {
            try {
                socket.close();
            }
            catch (IOException e) {
                System.out.println("Couldn't close a socket, what's going on?");
            }
            System.out.println("Connection with client# " + clientNumber + " closed");

        }
    }
}