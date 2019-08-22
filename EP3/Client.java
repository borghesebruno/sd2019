import java.net.*;
import java.util.*;
import java.io.*;

public class Client {
    private static String coordenadorAddress = "192.168.1.3:2222";
    private static String port = "2221";
    
    public static void main (String args[]) throws UnknownHostException, InterruptedException, IOException {
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
        
        System.out.println("Enviando lista com " + urls.size() + " URLs para o coordenador no endereço " + coordenadorAddress + ".");
        
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

                datagram = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(coordenadorAddress.split(":")[0]), Integer.parseInt(coordenadorAddress.split(":")[1]));
                socket.send(datagram);

            } catch(Exception e) {
                e.printStackTrace();
            }
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Aguardando resposta por 30 segundos.");

        ReducerResponse response = new ReducerResponse();
        try {
            DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(port));
            serverSocket.setSoTimeout(30000);
            byte[] receiveData;

            receiveData = new byte[2048];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket); 

            ByteArrayInputStream byteArr = new ByteArrayInputStream(receiveData);
            ObjectInputStream in = new ObjectInputStream(byteArr) ;
            response = (ReducerResponse)in.readObject();
            in.close();
            byteArr.close();
            serverSocket.close();
        }
        catch (SocketTimeoutException e) {
            System.out.println("Indice nao recebido, encerrando Cliente no endereço: " + myAddress);
            return;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Indice invertido recebido.");
        System.out.println("Salvando no arquivo \"index.txt\".");

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("index.txt"));

            urls = new ArrayList<String>(response.index.keySet());
            for(Integer i = 0; i < urls.size(); i++) {
                String url = urls.get(i);
                ArrayList<String> pointers = response.index.get(url);
                String line = url + " : { ";
                for(Integer j = 0; j < pointers.size(); j++) {
                    line += pointers.get(j) + "; ";
                }
                line += " } ";
                writer.write(line);
                writer.newLine();
            }
            
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("Encerrando Client no endereço: " + myAddress);
    }
}