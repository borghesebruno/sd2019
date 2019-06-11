import java.net.*;
import java.io.*;

public class Client {

    static String name;

    public static void main (String args[]) throws IOException {
        name = args[0];
        System.out.println("Iniciando Cliente " + name);

        DatagramSocket clientSocket = new DatagramSocket();
        /*InetAddress address = InetAddress.getByName("192.168.1.17");
        byte[] buffer = ("Eu sou o cliente " + name).getBytes();
        DatagramPacket datagram = new DatagramPacket(buffer, buffer.length, address, 1234);
        clientSocket.send(datagram);

        DatagramPacket answer = new DatagramPacket(new byte[512], 512);
        clientSocket.receive(answer);
        System.out.println(answer.getData().toString()
            +"\n"+ answer.getLength()
            +"\n"+ answer.getAddress()
            +"\n"+ answer.getPort());
        clientSocket.close();*/

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        InetAddress IPAddress = InetAddress.getByName("192.168.1.17");
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        String sentence = inFromUser.readLine();
        sentence = name + " diz: " + sentence;
        sendData = sentence.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 1234);
        clientSocket.send(sendPacket);
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String modifiedSentence = new String(receivePacket.getData());
        System.out.println("RECEIVED:" + modifiedSentence);
        clientSocket.close();
    }
}