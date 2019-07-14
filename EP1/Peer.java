import java.net.*;
import java.util.*;
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

    public String peerAddress;
    public String peerWhoSent;
	public ArrayList<FileState> files;
    public Date updateTime;
    public boolean clean;

    PeerState(String address, boolean clean) {
        this.peerAddress = address;
        this.files = new ArrayList<FileState>();
        this.updateTime = new Date();
        this.clean = clean;
    }

    public String toString() {
        String filesList = "%n";
        for(FileState file : files) {
            filesList += String.format("    "+ file.toString() + "%n");
        }
        return String.format("Arquivos:"+(filesList != "%n" ? filesList : " Sem arquivos%n") +"Atualizado: "+updateTime);
    }
}

public class Peer {
    public static GetFilesThread getFilesT;
    public static SendMyStateThread sendMyStateT;
    public static SendRandomStateThread sendRandomStateT;
    public static ReceiveStateThread receiveStateT;
    public static RemoveStateThread removeStateT;

    public static String pickRandomPeer(HashMap<String, PeerState> peers) {
        List<String> keysAsArray = new ArrayList<String>(peers.keySet());
        Random r = new Random();
        return keysAsArray.get(r.nextInt(keysAsArray.size()));
    }

    public static void main (String args[]) throws IOException, InterruptedException, UnknownHostException {
        if(args.length != 1) {
            System.out.println("Uso correto: java Peer <porta para escutar>");
            return;
        }
        
        int port = Integer.parseInt(args[0]);
        String myAddress = InetAddress.getLocalHost().getHostAddress()+":"+port;
        HashMap<String, PeerState> peers = new HashMap<String, PeerState>();
        System.out.println("Iniciando Peer no endereço: " + myAddress);

        BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("addresses.txt"));
			String line = reader.readLine();
			while (line != null) {
                if(line.charAt(line.length() - 1) != '#')
				    peers.put(line, new PeerState(line, true));
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

        getFilesT = new GetFilesThread(myAddress);
        getFilesT.start();
        Thread.sleep(1000);
        sendMyStateT = new SendMyStateThread(getFilesT, peers, myAddress);
        sendMyStateT.start();
        sendRandomStateT = new SendRandomStateThread(peers, myAddress);
        sendRandomStateT.start();
        receiveStateT = new ReceiveStateThread(port, peers);
        receiveStateT.start();
        removeStateT = new RemoveStateThread(peers);
        removeStateT.start();
    }
}

class GetFilesThread extends Thread {
    private String myAddress;
    private PeerState peerState;
    
    GetFilesThread(String myAddress) {
        this.myAddress = myAddress;
    }

    private Integer listFiles() {
        String home = System.getProperty("user.dir");
        File folder = new File(home+"/files");
        PeerState state = new PeerState(myAddress, false);
        state.files = new ArrayList<FileState>();
        File[] files =  folder.listFiles();
        if(files != null)
            for (File fileEntry : folder.listFiles()) {
                FileState file = new FileState();
                file.name = fileEntry.getName();
                file.length = fileEntry.length();
                file.lastModified = new Date(fileEntry.lastModified());
                state.files.add(file);
            }
        state.updateTime = new Date();
        this.peerState = state;
        return state.files.size();
    }

    public PeerState getPeerState() {
        return this.peerState;
    }

    public void run() {
        try {
            while(true) {
                Integer count = listFiles();
                System.out.println("thread T1 - "+count+" arquivos encontrados na pasta. Estado atual:");
                System.out.println(this.getPeerState());
                System.out.println();
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
    private HashMap<String, PeerState> peers;
    private String myAddress;

    SendMyStateThread(GetFilesThread getFilesThread, HashMap<String, PeerState> peers, String myAddress) {
        this.getFilesThread = getFilesThread;
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

class SendRandomStateThread extends Thread {
    private HashMap<String, PeerState> peers;
    private String myAddress;

    SendRandomStateThread(HashMap<String, PeerState> peers, String myAddress) {
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

                String whoToSend = Peer.pickRandomPeer(peers);
                while(peers.get(whoToSend).clean) {
                    whoToSend = Peer.pickRandomPeer(peers);
                }

                try {
                    PeerState state = peers.get(whoToSend);
                    state.peerWhoSent = myAddress;
                    ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(byteArr) ;
                    out.writeObject(state);
                    buffer = byteArr.toByteArray();
                    out.close();
                    byteArr.close();

                    System.out.println("thread T3 - enviando estado do peer "+sendTo+" por gossip ao peer " +whoToSend);
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

                if(peers.containsKey(state.peerAddress)) {
                    PeerState pState = peers.get(state.peerAddress);
                    if(pState.updateTime.before(state.updateTime)) {
                        peers.put(state.peerAddress, state);
                    }
                }

                System.out.println("Recebimento do estado do peer "+state.peerAddress+" por gossip vindo do peer "+state.peerWhoSent);
                System.out.println("");
            }

            //serverSocket.close();
        }
        catch (Exception e) { }
    }
}

class RemoveStateThread extends Thread {
    private HashMap<String, PeerState> peers;

    RemoveStateThread(HashMap<String, PeerState> peers) {
        this.peers = peers;
    }

    public void run() {
        try {
            while(true) {
                for (HashMap.Entry<String,PeerState> pair : peers.entrySet()) {
                    //apaga os dados se a ultima atualizacao foi a mais de 1 minuto
                    if(!pair.getValue().clean && pair.getValue().updateTime.before(new Date(System.currentTimeMillis() - 60 * 1000))) {
                        System.out.println("thread T4 – eliminando estado do peer "+pair.getKey());
                        System.out.println();

                        PeerState state = new PeerState(pair.getKey(), true);
                        peers.put(pair.getKey(), state);
                    }
                }
                Thread.sleep(10000);
            }
        }
        catch (Exception e) { }
    }
}