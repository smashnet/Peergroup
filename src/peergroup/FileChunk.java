/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peergroup;

/**
 *
 * @author Nicolas Inden
 */
public class FileChunk {
    
    private int id;
    private String path;
    private byte[] hash;
    
    public FileChunk(){
        
    }
    
    public FileChunk(int no, byte[] digest){
        this.id = no;
        this.hash = digest;
    }
    
}
