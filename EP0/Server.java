import java.net.*;
import java.io.*;

public class Server {

    static String name;

    public static void main (String args[]) throws IOException {
        name = args[0];
        System.out.println(name);

        DatagramSocket serverSocket = new DatagramSocket(1234);
        /*DatagramPacket msg = new DatagramPacket(new byte[512], 512);
        serverSocket.receive(msg);

        String answer = "Eu sou o servidor " + name;
        byte[] buffer = answer.getBytes();
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, msg.getAddress(), msg.getPort());
        serverSocket.send(datagram);
        serverSocket.close();*/

        byte[] receiveData;
        byte[] sendData;
        boolean goon = true;
        while(goon)
        {
            receiveData = new byte[1024];
            sendData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String sentence = new String(receivePacket.getData());
            System.out.println("RECEIVED: " + sentence);
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            String capitalizedSentence = name + "diz: " + sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
            if(sentence == "Cliente diz: fim") {
                goon = false;
            }
        }
        serverSocket.close();
    }
}