import java.net.*;
import java.util.*;
import java.io.*;

public class Reducer {
    private static String port = "2226";

    public static void main (String args[]) throws UnknownHostException, IOException {        
        String myAddress = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        System.out.println("Iniciando Reducer no endereço: " + myAddress);

        ArrayList<MapperResponse> requests = new ArrayList<MapperResponse>();
        String fromClient = "";
        Integer totalParts = 0;

        MapperResponse request = new MapperResponse();
        ServerSocket socket = new ServerSocket(Integer.parseInt(port));
        try {
            Socket skt = socket.accept();

            ObjectInputStream objectInput = new ObjectInputStream(skt.getInputStream());
            request = (MapperResponse) objectInput.readObject();
            String fromWho = skt.getInetAddress().getHostAddress() +':'+ skt.getPort();
            System.out.println("Indice recebido do mapper do endereço " + fromWho);

            requests.add(request);

            fromClient = request.clientWhoSent;
            totalParts = request.parts;
            Integer receivedParts = 1;
            while(receivedParts < totalParts) {
                skt = socket.accept();

                objectInput = new ObjectInputStream(skt.getInputStream());
                request = (MapperResponse) objectInput.readObject();
                fromWho = skt.getInetAddress().getHostAddress() +':'+ skt.getPort();
                System.out.println("Indice recebido do mapper do endereço " + fromWho);

                requests.add(request);

                receivedParts++;
            }
            
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        /*try {
            MapperResponse request = new MapperResponse();
            DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(port));
            DatagramPacket receivePacket;
            ByteArrayInputStream byteArr;
            ObjectInputStream in;
            byte[] receiveData;

            receiveData = new byte[2048];
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            String fromWho = receivePacket.getAddress().getHostAddress() +':'+ receivePacket.getPort();
            System.out.println("Indice recebido do mapper do endereço " + fromWho);

            byteArr = new ByteArrayInputStream(receiveData);
            in = new ObjectInputStream(byteArr) ;
            request = (MapperResponse)in.readObject();
            in.close();
            byteArr.close();

            requests.add(request);

            fromClient = request.clientWhoSent;
            totalParts = request.parts;
            Integer receivedParts = 1;
            while(receivedParts < totalParts) {
                receiveData = new byte[2048];
                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                fromWho = receivePacket.getAddress().getHostAddress() +':'+ receivePacket.getPort();
                System.out.println("Indice recebido do mapper do endereço " + fromWho);

                byteArr = new ByteArrayInputStream(receiveData);
                in = new ObjectInputStream(byteArr) ;
                request = (MapperResponse)in.readObject();
                in.close();
                byteArr.close();

                requests.add(request);

                receivedParts++;
            }
            
            serverSocket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }*/

        System.out.println(totalParts + " indices recebidos.");
        System.out.println("Criando indice invertido e ordenado.");

        HashMap<String, ArrayList<String>> invertedIndex = new HashMap<String, ArrayList<String>>();
        
        for(Integer i = 0; i < requests.size(); i++) {
            HashMap<String, ArrayList<String>> index = requests.get(i).index;
            Integer nURLs = index.size();
            Iterator<String> urls = index.keySet().iterator();
            
            while(urls.hasNext()) {
                String url = urls.next();
                if(!invertedIndex.containsKey(url)) {
                    invertedIndex.put(url, new ArrayList<String>());
                }

                ArrayList<String> foundURLs = index.get(url);
                for(Integer j = 0; j < foundURLs.size(); j++) {
                    String found = foundURLs.get(j);
                    
                    if(!invertedIndex.containsKey(found)) {
                        ArrayList<String> foundPointer = new ArrayList<String>();
                        foundPointer.add(url);
                        invertedIndex.put(found, foundPointer);
                    } else {
                        invertedIndex.get(found).add(url);
                    }
                }
            }
        }

        System.out.println("Enviando indice invertido para o cliente no endereço " + fromClient + ".");
        try {
            Socket clientSocket = new Socket(fromClient.split(":")[0], Integer.parseInt(fromClient.split(":")[1]));

            ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());
            objectOutput.writeObject(new ReducerResponse(invertedIndex));

            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Encerrando Reducer no endereço: " + myAddress);
    }
}