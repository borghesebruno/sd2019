import java.io.*;
import java.util.*;

class MapperResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    public String clientWhoSent;
    public HashMap<String, ArrayList<String>> index;
    public Integer part;
    public Integer parts;
    
    MapperResponse() { }

    MapperResponse(String clientWhoSent, HashMap<String, ArrayList<String>> index) {
        this.clientWhoSent = clientWhoSent;
        this.index = index;
    }

    void setParts(Integer part, Integer parts) {
        this.part = part;
        this.parts = parts;
    }
}