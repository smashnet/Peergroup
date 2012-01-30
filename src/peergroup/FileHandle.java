/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peergroup;

import java.io.*;
import java.util.LinkedList;
import java.security.MessageDigest;

/**
 *
 * @author Nicolas Inden
 */
public class FileHandle {
    
    private File file;
    private byte[] hash;
    private long size;
    private LinkedList<FileChunk> chunks;
            
    
    public FileHandle(){
        
    }
    
    /*
     * Use this constructor for files located on your device
     */
    public FileHandle(String filename) throws Exception{ 
        this.file = new File(filename);
        this.hash = this.calcHash(this.file);
        this.size = this.file.length();
        Constants.log.addMsg("FileHandle: Opening " + filename + " (Size: " + this.size + ")", 3);
        if(this.size <= 512000){
            this.createChunks(102400);
        }else if((this.size > 512000) && (this.size <= 10485760)){
            this.createChunks(524288);
        }else if(this.size > 10485760){
            this.createChunks(2097152);
        }
    }
    
    public FileHandle(String filename, byte[] fileHash, long fileSize) throws Exception{ 
        this.file = new File(filename);
        this.hash = fileHash;
        this.size = fileSize;
        Constants.log.addMsg("FileHandle: Opening " + filename + " (Size: " + this.size + ")", 3);
    }
    
    private void createChunks(int size) throws Exception{
        if(!(this.chunks == null)){
            Constants.log.addMsg("("+this.file.getName()+") Chunklist not empty!", 4);
            return;
        }
        Constants.log.addMsg("FileHandle: Creating chunks for " + this.file.getName(), 3);
        FileInputStream stream = new FileInputStream(this.file);
        this.chunks = new LinkedList<FileChunk>();
        int bytesRead = 0;
        int id = 0;
        byte[] buffer = new byte[size];
        
        while((bytesRead = stream.read(buffer)) != -1){
            FileChunk next = new FileChunk(id,calcHash(buffer));
            this.chunks.add(next);
            this.saveToDisc(id, buffer);
            id++;
        }
    }
    
    private void saveToDisc(int id,byte[] data){
        try{
            File tmp = new File("tmp/");
            tmp.mkdir();
            File myChunk = new File("tmp/"+this.file.getName()+"-"+id);
            myChunk.createNewFile();
            FileOutputStream outStream = new FileOutputStream(myChunk);
            outStream.write(data);
            outStream.close();
        }catch(IOException ioe){
            Constants.log.addMsg("Error creating chunk:" + ioe, 1);
        }
    }
    
    private byte[] calcHash(File in) throws Exception{
        FileInputStream stream = new FileInputStream(in);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        
        while((bytesRead = stream.read(buffer)) != -1){
            sha.update(buffer,0,bytesRead);
        }
        
        return sha.digest();
    }
    
    private byte[] calcHash(byte[] in) throws Exception{
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(in);
        
        return sha.digest();
    }
    
    public String getHash(){
        StringBuilder hexString = new StringBuilder();
    	for (int i=0;i<this.hash.length;i++) {
    	  hexString.append(Integer.toHexString(0xFF & this.hash[i]));
    	}
        
        return hexString.toString();
    }
    
    @Override
    public String toString(){
        String out = "";
        out += "Filename: " + this.file.toString() + "\n";
        out += "Size: " + this.size/1048576.0 + " MB\n";
        out += "Chunks: " + this.chunks.size() + " pieces\n";
        out += "SHA-256: " + this.getHash() + "\n";
        return out;
    }
}
