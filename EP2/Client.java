import java.net.*;
import java.util.*;
import java.io.*;

public class Client {
    public static SendMyStateThread sendMyStateT;
    public static ReceiveStateThread receiveStateT;

    public static String pickRandomPeer(List<String> peers) {
        Random r = new Random();
        return peers.get(r.nextInt(peers.size()));
    }

    public static void main (String args[]) throws IOException, InterruptedException, UnknownHostException {
        if(args.length != 1) {
            System.out.println("Uso correto: java Client <porta para escutar>");
            return;
        }
        
        int port = Integer.parseInt(args[0]);
        String myAddress = InetAddress.getLocalHost().getHostAddress()+":"+port;
        List<String> peers = new List<String>();
        System.out.println("Iniciando Cliente no endereço: " + myAddress);

        BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("addresses.txt"));
			String line;
			while (reader.readLine() != null)
                peers.add(line);
            
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

        sendMyStateT = new SendMyStateThread(peers, myAddress);
        sendMyStateT.start();
        
        receiveStateT = new ReceiveStateThread(port, peers);
        receiveStateT.start();
    }
}

class SendMyStateThread extends Thread {
    private List<String> peers;
    private String myAddress;

    SendMyStateThread(List<String> peers, String myAddress) {
        this.peers = peers;
        this.myAddress = myAddress;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer;
            DatagramPacket datagram;

            while(true) {
                String sendTo = Peer.pickRandomPeer(peers);
                InetAddress address = InetAddress.getByName(sendTo.split(":")[0]);

                try {
                    PeerState state = getFilesThread.getPeerState();
                    state.peerWhoSent = myAddress;
                    ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(byteArr) ;
                    out.writeObject(state);
                    buffer = byteArr.toByteArray();
                    out.close();
                    byteArr.close();

                    System.out.println("thread T2 - enviando proprio estado por gossip ao peer " +sendTo);
                    System.out.println();

                    datagram = new DatagramPacket(buffer, buffer.length, address, Integer.parseInt(sendTo.split(":")[1]));
                    socket.send(datagram);

                } catch(Exception e) {
                    e.printStackTrace();
                }
                
                Thread.sleep(5000);
            }

            //socket.close();
        }
        catch (Exception e) { }
    }
}

class ReceiveStateThread extends Thread {
    private int PORT;
    List<String> peers;

    ReceiveStateThread(int port, List<String> peers) {
        this.PORT = port;
        this.peers = peers;
    }

    public void run() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(PORT);
            byte[] receiveData;

            while(true) {
                receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket); 

                ByteArrayInputStream byteArr = new ByteArrayInputStream(receiveData);
                ObjectInputStream in = new ObjectInputStream(byteArr) ;
                PeerState state = (PeerState)in.readObject();
                in.close();
                byteArr.close();

                String type = "DUPLICADO";
                if(peers.containsKey(state.peerAddress)) {
                    PeerState pState = peers.get(state.peerAddress);
                    if(pState.updateTime.before(state.updateTime)) {
                        type = "ATUALIZADO";
                        peers.put(state.peerAddress, state);
                    } else if(state.updateTime.before(pState.updateTime)) {
                        type = "ANTIGO";
                    }

                    System.out.println("Recebimento "+type+" do estado do peer "+state.peerAddress+" por gossip vindo do peer "+state.peerWhoSent);
                    System.out.println("");
                }
            }

            //serverSocket.close();
        }
        catch (Exception e) { }
    }
}