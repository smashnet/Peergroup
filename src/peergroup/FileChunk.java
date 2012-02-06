package peergroup;

import java.util.LinkedList;

/**
 *
 * @author Nicolas Inden
 */
public class FileChunk {
    
    private int id;
    private byte[] hash;
    private String path;
    private LinkedList<P2Pdevice> peers;
    
    public FileChunk(){
        
    }
    
    public FileChunk(int no, byte[] digest){
        this.id = no;
        this.hash = digest;
    }
    
}
