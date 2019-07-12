import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.io.*;

class FileState {
    public String name;
    public Long length;
    public Date lastModified;

    public String toString() {
        return "Nome: "+name +"; Tamanho: "+length +"; Mod: "+lastModified;
    }
}

class PeerState implements Serializable {
    public ArrayList<FileState> files;
    public Date updateTime;

    public String toString() {
        return "Arquivos: "+files.toString() +"; Upd: "+updateTime;
    }
}

public class Peer {
    public HashMap<String, PeerState> peers = new HashMap<String, PeerState>();
    public static GetFilesThread getFilesT;
    public static SendMyStateThread sendMyFilesT;

    public static void main (String args[]) throws IOException, InterruptedException {
        if(args.length != 2) {
            System.out.println("Uso correto: java Peer <porta para escutar> <porta para enviar>");
        }
        
        System.out.println("Iniciando Peer");

        getFilesT = new GetFilesThread();
        getFilesT.start();
        /*sendMyFilesT = new SendMyStateThread(Integer.parseInt(args[1]), getFilesT);
        sendMyFilesT.start();*/
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

    @Override
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
    private int PORT;
    private GetFilesThread getFilesThread;

    SendMyStateThread(int port, GetFilesThread getFilesThread) {
        this.PORT = port;
        this.getFilesThread = getFilesThread;
    }

    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");
            byte[] buffer;
            DatagramPacket datagram;

            while(true) {
                ByteArrayOutputStream byteArr = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(byteArr) ;
                out.writeObject(getFilesThread.getPeerState());
                buffer = byteArr.toByteArray();
                out.close();
                byteArr.close();

                datagram = new DatagramPacket(buffer, buffer.length, address, PORT);
                socket.send(datagram);
                Thread.sleep(5000);
            }

            //socket.close();
        }
        catch (Exception e) { }
    }
}

class ReceiveStateThread extends Thread {
    private int PORT;
    private HashMap<String, State> peers;

    ReceiveStateThread(int port, HashMap<String, State> peers) {
        this.PORT = port;
        this.peers = peers;
    }

    public void run() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(PORT);
            byte[] receiveData;

            while(true) {
                receiveData = new byte[128];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
            }

            //serverSocket.close();
        }
        catch (Exception e) { }
    }
}