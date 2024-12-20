import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class ImageHandler {
	
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    
    // Traite l'image recue en paramètre et l'envoie au client.
    public void SendTreatedImage(File imagePath) throws IOException {
        BufferedImage image = readImage(imagePath);
        try {
            sendImage(Sobel.process(image));
        } catch (IOException e) {
            System.out.println("Erreur de traitement du fichier");
            e.printStackTrace();
        }
    }
   
    // Méthode qui va permettre de lire l'image/fichier à partir d'un flux d'entrée dataIn
    public BufferedImage receiveImage() throws IOException {
    	//Recoit la taille de l'image en bytes() et crée un tableay de cette taille
        int size = dataIn.readInt();
        byte[] imageArray = new byte[size];
        // Lis les  données de l'image contenant (size) bytes et met ces données and un byteArrayInputStream, qui sera ensuite converti en BufferedImage.
        dataIn.readFully(imageArray);
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageArray));
        //Retourne l'image recue.
        return image;
    }
    
    public BufferedImage readImage(File imagePath) throws IOException {
        BufferedImage image = ImageIO.read(imagePath);
        return image;
    }
    
    // Envoyer l'image via un flux de donnée out "dataOut"
    public void sendImage(BufferedImage image) throws IOException {
    	try {
    	// Créer le flux de byte pour envoyer les données (tableau de bytes qui sera envoyer comme un flux de données)
    	ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    	// Écrire les données de l'image dans le stream de bytes
    	ImageIO.write(image, "jpg", byteArrayOutputStream);
    	// écrire la taille de l'image (en bytes) dans le socket pour indiquer combien de données seront envoyés.
        dataOut.writeInt(byteArrayOutputStream.size());
        // écrire les données de l'image séquentiellement dans le socket après l'avoir transformé en array de bytes.
        dataOut.write(byteArrayOutputStream.toByteArray());
        // vide le stream de donnée du socket
        dataOut.flush();
        byteArrayOutputStream.close();
    	} catch(IOException e) {
    		System.out.println("Erreur lors de l'envoie du fichier");
    		e.printStackTrace();
    	}
    }
    
    // Méthode qui va permettre de lire l'image/fichier et de le créer à partir d'un flux d'entrée dataIn
    public void saveImage(BufferedImage image, String savedImagePath) throws IOException {
    	try {
    	// ImageIO créé l'image à partir des données du bufferedImage
    	ImageIO.write(image, "jpg", new File(savedImagePath));
    	} catch(IOException e) {
    		System.out.println("Erreur lors de l'écriture du fichier");
    		e.printStackTrace();
    	}
    }
    

    // Setter methods for dataIn and dataOut
    public void setDataIn(DataInputStream dataIn) {
        this.dataIn = dataIn;
    }

    public void setDataOut(DataOutputStream dataOut) {
        this.dataOut = dataOut;
    }
}