import java.net.*;
import java.io.*;

public class Server {
    public static void main (String args[]) throws IOException {
        System.out.println(args[0]);

        DatagramSocket serverSocket = new DatagramSocket(1234);
        DatagramPacket mensagem = new DatagramPacket(new byte[512],512);
        serverSocket.receive(mensagem);
        String resposta = "Provavelmente!";
        byte[] buffer = resposta.getBytes();
        DatagramPacket datagramaResposta = new DatagramPacket(buffer, buffer.length,mensagem.getAddress(),mensagem.getPort());
        serverSocket.send(datagramaResposta);
        //Quando a comunicação acabar
        serverSocket.close();
    }
}