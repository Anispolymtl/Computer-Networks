import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
	private static String imageFilePath = ".\\Clientfile\\";
	private static Socket socket;
	
	public static void main(String[] args) throws Exception {
			
		try {
			// Adresse et port du serveur
			// Reader pour les inputs.
			int port = 0;
			String serverAddress = "";
			String username = "";
			String password = "";
			Scanner readObject = new Scanner(System.in);
			IpAddressChecker ip = new IpAddressChecker();
			// S'assure que le dossier pour
			File pathIsPresent = new File(imageFilePath);
			if(!pathIsPresent.exists()) {
				System.out.println("Création du dossier Clientfile dans le directory actuel.");
    			if(pathIsPresent.mkdir()) {
    				System.out.println("\nClientfile a été créer correctement. Veuillez utiliser ce dossier comme répertoire pour envoyer/recevoir vos fichiers.\n");
    			} else {
    				System.out.println("\nErreur lors de la création du dossier Clientfile.");
    			}
			} else {
    			System.out.println("\nNote: Le dossier Clientfile est utilisé par défaut pour la réception/envoie des données client.\n");
    		}
			
			// Input de l'addresse IP et vérification de la validité
			System.out.println("Entrer l'adresse IP du serveur et le numéro de port:");
			System.out.println("Adresse IP du serveur: ");
			serverAddress = readObject.nextLine();
			while (!ip.ipAddressEstValide(serverAddress)) {
				System.out.println("\nVeuillez entrer une adresse IP dont le format est valide.");
				serverAddress = readObject.next();
			}
			
			// Input du numéro de port et vérification de la validité
			while (port < 5000 | port > 5050) {
				System.out.println("Entrer le numéro de Port (entre 5000 et 5050): ");
				if (readObject.hasNextInt()) {
				port = readObject.nextInt();
				} else {
				System.out.println("Veuillez entrer des nombres seulement.\n");
				readObject.next();
				}
			}
			
			// Création d'une nouvelle connexion avec le serveur
			socket = new Socket(serverAddress, port);
			System.out.format("Serveur lancé sur [%s:%d]\n", serverAddress, port);
			
			// Création d'un canal entrant pour recevoir les messages envoyés, par le serveur
			DataInputStream in = new DataInputStream(socket.getInputStream());
			// Création d'un canal sortant pour envoyer des messages au serveur
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			// Réception & affichage d'un message envoyé par le serveur sur le canal lorsque connecté.
			String helloMessageFromServer = in.readUTF();
			System.out.println(helloMessageFromServer);
			
			// Input du Username et password de l'utilisateur
			System.out.println("Entrer votre Nom d'utilisateur:");
			username = readObject.next();
			System.out.println("Entrer votre mot de passe:");
			password = readObject.next();
						
			// Envoi du Username et du password au serveur (client handler)
			boolean isConnected = false;
			String connexionResponse = "";
			out.writeUTF(username);
			out.writeUTF(password);
			
			//Boucle de vérification du username/password jusqu'à 4 essais max.
			while (!isConnected) {
				// Lis la réponse du serveur
				connexionResponse = in.readUTF(); 
				if (connexionResponse.equals("Connected")) {
					// Si le nom d'utilisateur et le mdp concordent avec un dans la base de donnée
					System.out.println("\nVous êtes connecté au serveur.");
					isConnected = true;
				} else if(connexionResponse.equals("Created")) {
					// Si le compte n'existait pas dans la base de donnée
					System.out.println("\nVotre compte a été créé.Vous ête connecté au serveur");
					isConnected = true;
				} else if(connexionResponse.equals("Mauvais mot de passe. Veuillez réessayer.")) {
					// Si le nom d'utilisateur existe dans la base de donnée mais le mot de passe n'est pas le bon
					System.out.println(connexionResponse);
					System.out.println("Entrer votre mot de passe:");
					password = readObject.next();
					out.writeUTF(password);
				} else {
					// Si il y a une erreur lors de la transmission de donnée.
					System.out.println(connexionResponse);
					out.flush();
					out.close();
					socket.close();
					break;
				}
			}
			
			// Envoyer le fichier au serveur  //
			
			// Créer un objet ImageHandler pour utiliser ses méthodes lors de l'interaction.
			String sentImageName = "";
			String receivedImageName = "";
			ImageHandler imageHandler = new ImageHandler();
			imageHandler.setDataIn(in);
			imageHandler.setDataOut(out);
			// Compteur pour les échecs d'input.
			int attemptNumber = 0;
			
			// Label pour pouvoir break le loop en cas d'échec successifs lors d'input utilisateurs.
			connectedLoop: {
			while(isConnected) {
				// Demande le nom du fichier a envoyer
				System.out.println("\nVeuillez entrer le nom complet de l'image à envoyer, incluant le type :");
				sentImageName = readObject.next();
				File sentImageFile = new File(imageFilePath + sentImageName);
				// Vérifie si l'image existe dans le dossier de l'exécutable.
					while (!sentImageFile.exists()) {
						System.out.println("\nIl n'y a pas de fichier portant ce nom dans le dossier Clientfile. Veuillez réessayer.");
						System.out.println("\nVeuillez entrer le nom complet de l'image à envoyer, incluant le type, ou taper 'exit' pour se déconnecter:");
						sentImageName = readObject.next();
						sentImageFile = new File(imageFilePath + sentImageName);
						// Break loop if too many attempts or exit is specified.
						if(++attemptNumber > 3|| sentImageName.equals("exit")) {
							System.out.println("\nTrop d'essais infructueux. Vous allez être déconnecté.");
							isConnected = false;
							break connectedLoop;
						}
					}
				
				// Change le filepath courant avec celui de l'image à traiter et envoie le nom de l'image au serveur 
				out.writeUTF(sentImageName);
				// Envoie l'image au serveur et confirmation de la réception par celui-ci
				File untreatedImage = new File(imageFilePath + sentImageName);
				imageHandler.sendImage(imageHandler.readImage(untreatedImage));
				System.out.println("L'image a été envoyée au serveur.");
				String isReceived = in.readUTF();
				
				// Si l'image a bien été recue par le serveur
				if (!isReceived.equals("L'image n'a pas bien été recue par le serveur.")) {
					// Afficher le message de confirmation de réception du fichier par le serveur
					System.out.println(isReceived);
					// Demande le nom qu'on désire donné au fichier traité à recevoir et vérifie s'il n'existe pas déja;
					System.out.println("\nVeuillez entrer le nom complet du fichier désiré lors de la sauvegarde de l'image traitée : ");
					receivedImageName = readObject.next();
					File receivedImageFile = new File(imageFilePath + receivedImageName);
					attemptNumber = 0;
					while (receivedImageFile.exists()) {
						System.out.println("\nUn fichier existant porte déja ce nom. Veuillez choisir un autre nom pour votre fichier reçue, ou taper 'exit' pour quitter: ");
						receivedImageName = readObject.next();
						receivedImageFile = new File(imageFilePath + receivedImageName);
						// Break case pour empêcher un loop infini.
						if(++attemptNumber > 4 || receivedImageName.equals("exit")) {
							System.out.println("\nTrop d'essais infructueux ou demande de déconnection. Vous allez être déconnecté.");
							isConnected = false;
							break connectedLoop;
						}
					}
					// Recevoir le fichier traiter et sauvegarde celui-ci avec le nom indiqué.
					imageHandler.saveImage(imageHandler.receiveImage(), imageFilePath + receivedImageName);
				} else {
					// indique qu'il y a eu erreur lors de l'envoie.
					System.out.println("\nErreur lors de l'envoie de l'image au serveur.");
					
				}
				
				// Demande si on veux envoyer un autre fichier pour être traité
				String sendOtherFile = "";
				System.out.println("\nVoulez-vous envoyer un autre fichier? Taper oui pour continuer, non pour vous déconnecter. (oui/non)");
				sendOtherFile = readObject.next();
				
				if(sendOtherFile.equals("non")) {
					// Déconnexion en cas de refus. Break la boucle qui permet l'envoie de fichier.
					System.out.println("Vous aller être déconnecté. Merci d'avoir utilisé PolySobel.");
					isConnected = false;
					out.writeUTF("non");
				} else if (sendOtherFile.equals("oui")){
					// Envoie la réponse au serveur pour recommencer la boucle de traitement de fichier.
					out.writeUTF("oui");
				} else {
					System.out.println("Réponse invalide. Vous allez être déconnecté.");
					out.writeUTF("non");
				}
			} // fin de la boucle while
			} // fin de l'étiquette connectedLoop
			
			// Fermeture de La connexion avec le serveur
			System.out.println("La connexion avec le serveur est terminé.");
			readObject.close();
			out.flush();
			out.close();
			in.close();
		} catch (Exception e) {
			System.out.println("Erreur quelconque du système. Veuillez redémarrer votre terminal et vous reconnecter.");
			e.printStackTrace();
		}  finally {
            try {
    			socket.close();
            }
            catch (IOException e) {
                System.out.println("Couldn't close a socket!!!!!!! WTF");
            }
        }		
	}
}
