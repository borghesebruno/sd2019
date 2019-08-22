import java.io.*;
import java.util.*;

class ReducerResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    public HashMap<String, ArrayList<String>> index;

    ReducerResponse() { }

    ReducerResponse(HashMap<String, ArrayList<String>> index) {
        this.index = index;
    }
}