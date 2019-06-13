import java.net.*;
import java.io.*;

public class Client {
    private static final int PORT = 1234;

    private static String number;
    private static DatagramSocket socket;
    private static InetAddress address;
    private static byte[] buffer;
    private static DatagramPacket datagram;

    public static void main (String args[]) throws IOException, InterruptedException {
        if(args.length < 2) {
            System.out.println("Uso correto: java Cliente <1|2> <seq|fora|falta|dupla>");
            return;
        }

        number = args[0];
        System.out.println("Iniciando Cliente " + number);

        socket = new DatagramSocket();
        address = InetAddress.getByName("192.168.1.17");
        
        switch(args[1]) {
            case "seq":
                System.out.println("Enviando mensagens ordenadas:");
                sendOrdered();
                break;
            case "fora":
                System.out.println("Enviando mensagens fora de ordem:");
                sendUnordered();
                break;
            case "falta":
                System.out.println("Enviando mensagens com algumas faltando:");
                sendMissing();
                break;
            case "dupla":
                System.out.println("Enviando mensagens com duplicadas:");
                sendDuplicated();
                break;
            default:
                break;
        }

        System.out.println("Encerrando Cliente " + number);
        socket.close();
    }

    private static void sendOrdered () throws IOException, InterruptedException {
        for(int i = 1; i < 5; i++) {
            buffer = (number + '-' + i).getBytes();
            System.out.println("--- enviando: " + i);
            datagram = new DatagramPacket(buffer, buffer.length, address, PORT);
            socket.send(datagram);
            Thread.sleep(500);
        }
    }

    private static void sendUnordered () throws IOException, InterruptedException {
        int[] nums = {3,1,4,2};
        for(int i = 0; i < 4; i++) {
            buffer = (number + '-' + nums[i]).getBytes();
            System.out.println("--- enviando: " + nums[i]);
            datagram = new DatagramPacket(buffer, buffer.length, address, PORT);
            socket.send(datagram);
            Thread.sleep(500);
        }
    }

    private static void sendMissing () throws IOException, InterruptedException {
        for(int i = 1; i < 5; i++) {
            if(i != 2 && i != 3) {
                buffer = (number + '-' + i).getBytes();
                System.out.println("--- enviando: " + i);
                datagram = new DatagramPacket(buffer, buffer.length, address, PORT);
                socket.send(datagram);
                Thread.sleep(500);
            }
        }
    }

    private static void sendDuplicated () throws IOException, InterruptedException {
        int[] nums = {1,1,2,3,2,3,4,4};
        for(int i = 0; i < 8; i++) {
            buffer = (number + '-' + nums[i]).getBytes();
            System.out.println("--- enviando: " + nums[i]);
            datagram = new DatagramPacket(buffer, buffer.length, address, PORT);
            socket.send(datagram);
            Thread.sleep(500);
        }
    }
}