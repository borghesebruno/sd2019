import java.io.*;
import java.util.*;

class ClientRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public String clientWhoSent;
    public ArrayList<String> urls;
    public Integer part;
    public Integer parts;
    
    ClientRequest() { }

    ClientRequest(String clientWhoSent, ArrayList<String> urls) {
        this.clientWhoSent = clientWhoSent;
        this.urls = urls;
    }

    void setParts(Integer part, Integer parts) {
        this.part = part;
        this.parts = parts;
    }
}