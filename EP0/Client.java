import java.net.*;
import java.io.*;

public class Client {

    static String nome;

    public static void main (String args[]) throws IOException {
        nome = args[0];
        System.out.println(nome);

        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress addr = InetAddress.getByName("localhost");
        String mensagem = "Vai cair na prova?";
        byte[] buffer = mensagem.getBytes();
        DatagramPacket datagrama = new DatagramPacket(buffer, buffer.length, addr,1234);
        clientSocket.send(datagrama);
        DatagramPacket resposta = new DatagramPacket(new byte[512],512);
        clientSocket.receive(resposta);
        System.out.println(resposta.getData()
            +"\n"+ resposta.getLength()
            +"\n"+ resposta.getAddress()
            +"\n"+ resposta.getPort());
        //Quando não houver mais comunicações a se fazer
        clientSocket.close();
    }
}