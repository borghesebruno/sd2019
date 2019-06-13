import java.net.*;
import java.io.*;

public class Server {
    public static void main (String args[]) throws IOException, InterruptedException {
        System.out.println("Iniciando Servidor");

        DatagramSocket serverSocket = new DatagramSocket(1234);

        ServerThread[] threads = { new ServerThread("1"), new ServerThread("2") };
        threads[0].start();
        threads[1].start();
        boolean[] completed = { false, false };

        byte[] receiveData;
        boolean goOn = true;
        while(goOn)
        {
            receiveData = new byte[128];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            
            String[] received = new String(receivePacket.getData()).split("-");
            int fromClient = Integer.parseInt(received[0]);
            String msg = received[1];
            System.out.println("Mensagem recebida do cliente " + fromClient + ": " + msg);
            
            completed[fromClient - 1] = threads[fromClient - 1].receive(msg);
            if(completed[0] && completed[1]) {
                goOn = false;
            }
        }

        serverSocket.close();
    }
}

class ServerThread extends Thread {
    private String threadNumber;
    private boolean[] msgs = {false, false, false, false};
    private boolean complete = false;

    ServerThread(String number) {
        threadNumber = number;
    }
    
    public void run() {
        try {
            while(!complete) {;}
        }
        catch (Exception e) {

        }
    }

    public boolean receive(String msg) {
        switch(msg) {
            case "1": msgs[0] = true;
            case "2": msgs[1] = true;
            case "3": msgs[2] = true;
            case "4": msgs[3] = true;
            default: break;
        }
        complete = msgs[0] && msgs[1] && msgs[2] && msgs[3];
        if(complete) {
            System.out.println("Todas as mensagens do cliente " + threadNumber + " foram recebidas.");
        }
        return complete;
    }
}