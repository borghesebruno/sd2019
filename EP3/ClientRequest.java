import java.io.*;
import java.util.*;

class ClientRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public String clientWhoSent;
    public ArrayList<String> urls;
    
    ClientRequest() { }

    ClientRequest(String clientWhoSent, ArrayList<String> urls) {
        this.clientWhoSent = clientWhoSent;
        this.urls = urls;
    }
}