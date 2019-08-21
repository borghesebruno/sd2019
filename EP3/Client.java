import java.net.*;
import java.util.*;
import java.io.*;

public class Client {
    private static String cordenadorAddress = "192.168.1.3:2222";
    private static String port = "2221";
    
    public static void main (String args[]) throws UnknownHostException, InterruptedException {
        String myAddress = InetAddress.getLocalHost().getHostAddress() + ":" + port;
        System.out.println("Iniciando Client no endereço: " + myAddress);

        ArrayList<String> urls = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader("urls.txt"));
			String line = reader.readLine();
			while (line != null) {
				urls.add(line);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
        }
        
        System.out.println("Enviando lista com " + urls.size() + " URLs.");
        
        ClientRequest req = new ClientRequest(myAddress, urls);

        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer;
            DatagramPacket datagram;

            try {
                ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(byteArr) ;
                out.writeObject(req);
                buffer = byteArr.toByteArray();
                out.close();
                byteArr.close();

                datagram = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(cordenadorAddress.split(":")[0]), Integer.parseInt(cordenadorAddress.split(":")[1]));
                socket.send(datagram);

            } catch(Exception e) {
                e.printStackTrace();
            }
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Thread.sleep(30000);

        /*ReducerResponse response = new ReducerResponse();
        try {
            DatagramSocket serverSocket = new DatagramSocket(Integer.parseint(port));
            byte[] receiveData;

            receiveData = new byte[2048];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket); 

            ByteArrayInputStream byteArr = new ByteArrayInputStream(receiveData);
            ObjectInputStream in = new ObjectInputStream(byteArr) ;
            response = (ReducerResponce)in.readObject();
            in.close();
            byteArr.close();
            serverSocket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Indice invertido recebido.");
        System.out.println("Salvando no arquivo \"index.txt\".");

        BufferedWriter writer = new BufferedWriter(new FileWriter("index.txt"));
        
        response.sites
        
        writer.close();*/
    }
}