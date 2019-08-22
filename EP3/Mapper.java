import java.net.*;
import java.util.*;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Mapper {
    private static String reducerAddress = "192.168.1.3:2226";

    public static void main (String args[]) throws UnknownHostException, IOException {
        if(args.length == 0)
            System.out.println("Uso correto: java Mapper <porta>");
        
        String port = args[0];
        String myAddress = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        System.out.println("Iniciando Mapper no endereço: " + myAddress);

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
        System.out.println("Processando URLs.");

        HashMap<String, ArrayList<String>> index = new HashMap<String, ArrayList<String>>();
        for(Integer i = 0; i < nURLs; i++) {
            String url = request.urls.get(i);
            System.out.println("Obtendo url: " + url);

            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");

            ArrayList<String> mapped = new ArrayList<String>();
            for (Element link : links) {
                mapped.add(link.attr("abs:href"));
            }

            index.put(url, mapped);
        }

        try {
            MapperResponse response;

            DatagramSocket socket = new DatagramSocket();
            byte[] buffer;
            DatagramPacket datagram;

            System.out.println("Enviando indices encontrados para o reducer no endereço " + reducerAddress + ".");

            response = new MapperResponse(request.clientWhoSent, index);
            response.setParts(request.part, request.parts);
            ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteArr) ;
            out.writeObject(response);
            buffer = byteArr.toByteArray();
            out.close();
            byteArr.close();

            datagram = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(reducerAddress.split(":")[0]), Integer.parseInt(reducerAddress.split(":")[1]));
            socket.send(datagram);

            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Encerrando Mapper no endereço: " + myAddress);
    }
}