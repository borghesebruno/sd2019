import java.net.*;
import java.util.*;
import java.io.*;

public class Coordenador {
    private static String[] mappersAddress = { "192.168.1.3"};//, "192.168.1.3", "192.168.1.3" };
    private static String[] mappersPorts = { "2223"};//, "2224", "2225" };
    private static String port = "2222";

    public static void main (String args[]) throws UnknownHostException {
        String myAddress = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        System.out.println("Iniciando Coordenador no endereço: " + myAddress);

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

        Integer nURLs = request.urls.size();
        System.out.println("Lista com " + nURLs + " URLs recebida.");
        System.out.println("Separando URLs.");

        Integer nMappers = mappersAddress.length;
        ArrayList<ArrayList<String>> mappersURLs = new ArrayList<ArrayList<String>>();
        for(Integer i = 0; i < nMappers; i++) {
            mappersURLs.add(new ArrayList<String>());
        }

        for(Integer i = 0; i < nURLs; i++) {
            Integer index = (i + nMappers) % nMappers;
            mappersURLs.get(index).add(request.urls.get(i));
        }

        try {
            ClientRequest req;

            DatagramSocket socket = new DatagramSocket();
            byte[] buffer;
            DatagramPacket datagram;

            for(Integer i = 0; i < nMappers; i++) {
                System.out.println("Enviando " + mappersURLs.get(i).size() + " URLs para o mapper no endereço " + mappersAddress[i]+":"+mappersPorts[i] + ".");

                req = new ClientRequest(request.clientWhoSent, mappersURLs.get(i));
                req.setParts(i, nMappers);
                ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(byteArr) ;
                out.writeObject(req);
                buffer = byteArr.toByteArray();
                out.close();
                byteArr.close();

                datagram = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(mappersAddress[i]), Integer.parseInt(mappersPorts[i]));
                socket.send(datagram);
            }

            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Encerrando Coordenador no endereço: " + myAddress);
    }
}