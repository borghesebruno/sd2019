import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.io.*;
import org.json.*;

class State {
    public String files;
    public Date updateTime;
}

public class Peer {
    public HashMap<String, State> peers = new HashMap<String, State>();
    public static GetFilesThread getFilesT = new GetFilesThread();
    public static SendMyStateThread sendMyFilesT;

    public static void main (String args[]) throws IOException, InterruptedException {
        if(args.length != 2) {
            System.out.println("Uso correto: java Peer <porta para escutar> <porta para enviar>");
        }
        
        System.out.println("Iniciando Peer");

        getFilesT.start();
        sendMyFilesT = new SendMyStateThread(Integer.parseInt(args[1]), getFilesT);
        sendMyFilesT.start();
    }
}

class GetFilesThread extends Thread {
    private String peerState = new String();
    private File folder = new File("/home/bruno/repos/sd2019/EP1/files");
    private void listFilesForFolder(File folder) {
        StringBuilder sb = new StringBuilder();
        for (File fileEntry : folder.listFiles()) {
            sb.append(fileEntry.getName());
            sb.append('/');
        }
        if(sb.toString().endsWith("/"))
            sb.deleteCharAt(sb.length()-1);
        peerState = sb.toString();
    }

    public String getPeerState() {
        return this.peerState;
    }

    public void run() {
        try {
            while(true) {
                listFilesForFolder(folder);
                Thread.sleep(5000);
            }
        }
        catch (Exception e) { }
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
                buffer = (getFilesThread.getPeerState()).getBytes();
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