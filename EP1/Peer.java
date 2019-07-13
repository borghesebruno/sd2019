import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Date;
import java.io.*;

class FileState implements Serializable {
    public String name;
    public Long length;
    public Date lastModified;

    public String toString() {
        return "Nome: "+name +"; Tamanho: "+length +"; Mod: "+lastModified;
    }
}

class PeerState implements Serializable {
    private static final long serialVersionUID = 1L;
	public ArrayList<FileState> files;
    public Date updateTime;

    public String toString() {
        return "Arquivos: "+files.toString() +"; Upd: "+updateTime;
    }
}

public class Peer {
    public static GetFilesThread getFilesT;
    public static SendMyStateThread sendMyStateT;
    public static ReceiveStateThread receiveStateT;

    public static void main (String args[]) throws IOException, InterruptedException {
        if(args.length != 1) {
            System.out.println("Uso correto: java Peer <porta para escutar>");
            return;
        }
        
        System.out.println("Iniciando Peer na porta " + args[0]);

        HashMap<String, PeerState> peers = new HashMap<String, PeerState>();

        BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("addresses.txt"));
			String line = reader.readLine();
			while (line != null) {
                if(line.charAt(line.length() - 1) != '#')
				    peers.put(line, new PeerState());
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

        getFilesT = new GetFilesThread();
        getFilesT.start();
        sendMyStateT = new SendMyStateThread(getFilesT, Integer.parseInt(args[0]), peers);
        sendMyStateT.start();
        receiveStateT = new ReceiveStateThread(Integer.parseInt(args[0]), peers);
        receiveStateT.start();
    }
}

class GetFilesThread extends Thread {
    private PeerState peerState = new PeerState();

    private void listFiles() {
        File folder = new File("/home/bruno/repos/sd2019/EP1/files");
        PeerState state = new PeerState();
        state.files = new ArrayList<FileState>();
        for (File fileEntry : folder.listFiles()) {
            FileState file = new FileState();
            file.name = fileEntry.getName();
            file.length = fileEntry.length();
            file.lastModified = new Date(fileEntry.lastModified());
            state.files.add(file);
        }
        state.updateTime = new Date();
        this.peerState = state;
    }

    public PeerState getPeerState() {
        return this.peerState;
    }

    public void run() {
        try {
            while(true) {
                listFiles();
                Thread.sleep(5000);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class SendMyStateThread extends Thread {
    private GetFilesThread getFilesThread;
    private int PORT;
    private HashMap<String, PeerState> peers;

    SendMyStateThread(GetFilesThread getFilesThread, int port, HashMap<String, PeerState> peers) {
        this.getFilesThread = getFilesThread;
        this.PORT = port;
        this.peers = peers;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket(PORT);
            byte[] buffer;
            DatagramPacket datagram;

            while(true) {
                List<String> keysAsArray = new ArrayList<String>(peers.keySet());
                Random r = new Random();
                String sendTo = keysAsArray.get(r.nextInt(keysAsArray.size()));
                InetAddress address = InetAddress.getByName(sendTo.split(":")[0]);

                try {
                    ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(byteArr) ;
                    out.writeObject(getFilesThread.getPeerState());
                    buffer = byteArr.toByteArray();
                    out.close();
                    byteArr.close();

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
    private HashMap<String, PeerState> peers;

    ReceiveStateThread(int port, HashMap<String, PeerState> peers) {
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

                System.out.println(receivePacket.getAddress().getHostAddress() +":"+ receivePacket.getPort() +" enviou: " + state.toString());

                //if(this.peers)
            }

            //serverSocket.close();
        }
        catch (Exception e) { }
    }
}