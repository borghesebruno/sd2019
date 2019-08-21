import java.net.*;
import java.util.*;
import java.io.*;

public class Cordenador {
    private static String mappersAddress = "192.168.1.3";
    private static String[] mappersPorts = {"2223", "2224", "2225"};
    private static String port = "2222";

    public static void main (String args[]) throws UnknownHostException {
        String myAddress = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        System.out.println("Iniciando Cordenador no endereço: " + myAddress);

        ClientRequest request = new ClientRequest();
        try {
            DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(port));
            byte[] receiveData;

            receiveData = new byte[2048];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket); 

            ByteArrayInputStream byteArr = new ByteArrayInputStream(receiveData);
            ObjectInputStream in = new ObjectInputStream(byteArr) ;
            request = (ClientRequest)in.readObject();
            in.close();
            byteArr.close();
            serverSocket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Lista com " + request.urls.size() + " URLs recebida.");
    }
}