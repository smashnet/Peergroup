/*
* Peergroup - FileHandle.java
* 
* Peergroup is a file synching tool using XMPP for data- and 
* participantmanagement and Apache Thrift for direct data-
* exchange between users.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* License: Not for public distribution!
*/

package peergroup;

import java.io.*;
import java.util.LinkedList;
import java.util.Arrays;
import java.security.MessageDigest;

/**
 * A FileHandle includes all information needed to work with
 * a file.
 *
 * @author Nicolas Inden
 */
public class FileHandle {
    
	/**
	* The file id
	*/
	private int id;
	/**
	* The Java File object 
	*/
    private File file;
	/**
	* The fileversion is used to maintain the most recent filecontent via lamport clocks
	*/
	private int fileVersion;
	/**
	* The hash of the file as byte array
	*/
    private byte[] hash;
	/**
	* The size of the file in bytes
	*/
    private long size;
	/**
	* A linked list of FileChunk objects which this file consists of
	*/
    private LinkedList<FileChunk> chunks;
	/**
	* An indicator if the file is completely available on the local storage
	*/
    private boolean complete;
    
    public FileHandle(){
        
    }
    
    /**
     * Use this constructor for complete files located on your device
     */
    public FileHandle(String filename) throws Exception{ 
        this.file = new File(filename);
		this.fileVersion = 0;
        this.hash = this.calcHash(this.file);
        this.size = this.file.length();
        this.complete = true;
		Constants.log.addMsg("FileHandle: New file from storage: " + filename 
								+ " (Size: " + this.size + ", Hash: " + this.getHexHash() + ")", 3);
		
        if(this.size <= 512000){								// size <= 500kByte 				-> 100kByte Chunks
			this.createChunks(102400);
		}else if(this.size > 512000 && this.size <= 5120000){	// 500kByte < size <= 5000kByte 	-> 200kByte Chunks
			this.createChunks(204800);
		}else if(this.size > 5120000 && this.size <= 51200000){	// 5000kByte < size <= 50000kByte 	-> 1000kByte Chunks
			this.createChunks(1024000);
		}else if(this.size > 51200000){							// 50000kByte < size 				-> 2000kByte Chunks
			this.createChunks(2048000);
		}
    }
	
    /**
     * Use this constructor for complete files located on your device
     */
    public FileHandle(File newFile) throws Exception{ 
        this.file = newFile;
		this.fileVersion = 0;
        this.hash = this.calcHash(this.file);
        this.size = this.file.length();
        this.complete = true;
		Constants.log.addMsg("FileHandle: New file from storage: " + this.file.getName() 
								+ " (Size: " + this.size + ", Hash: " + this.getHexHash() + ")", 3);
		
        if(this.size <= 512000){								// size <= 500kByte 				-> 100kByte Chunks
			this.createChunks(102400);
		}else if(this.size > 512000 && this.size <= 5120000){	// 500kByte < size <= 5000kByte 	-> 200kByte Chunks
			this.createChunks(204800);
		}else if(this.size > 5120000 && this.size <= 51200000){	// 5000kByte < size <= 50000kByte 	-> 1000kByte Chunks
			this.createChunks(1024000);
		}else if(this.size > 51200000){							// 50000kByte < size 				-> 2000kByte Chunks
			this.createChunks(2048000);
		}
    }
    
	/**
	* Use this constructor for files to be received via network
	*/
    public FileHandle(String filename, int vers, byte[] fileHash, long fileSize, LinkedList<FileChunk> chunks) 
    throws Exception{ 
        this.file = new File(Constants.rootDirectory + filename);
		this.fileVersion = vers;
        this.hash = fileHash;
        this.size = fileSize;
		this.chunks = chunks;
		this.complete = false;
        Constants.log.addMsg("FileHandle: New file via network: " + filename
								+ " (Size: " + this.size + ", Hash: " + this.getHexHash() + ")", 3);
		// Create empty file on disk
		try{
			this.file.createNewFile();
			RandomAccessFile out = new RandomAccessFile(this.file,"rwd");
			out.setLength(this.size);
			out.close();
		}catch(IOException ioe){
			Constants.log.addMsg("FileHandle: Cannot create new file from network: " + ioe,4);
		}
    }
    
	/**
	* Creates a linked list of FileChunk for this FileHandle
	* 
	* @param size the size of a chunk (last one might be smaller)
	*/
    private void createChunks(int size) throws Exception{
        if(!(this.chunks == null)){
            Constants.log.addMsg("(" + this.file.getName() + ") Chunklist not empty!", 4);
            return;
        }
        Constants.log.addMsg("FileHandle: Creating chunks for " + this.file.getName(), 3);
        FileInputStream stream = new FileInputStream(this.file);
        this.chunks = new LinkedList<FileChunk>();
        int bytesRead = 0;
        int id = 0;
        byte[] buffer = new byte[size];
        
        while((bytesRead = stream.read(buffer)) != -1){
            FileChunk next = new FileChunk(id,calcHash(buffer),bytesRead,id*size,true);
            this.chunks.add(next);
            id++;
        }
    }
    
	/**
	* General function to calculate the hash of a given file
	* 
	* @param in the file
	* @return hash as byte array
	*/
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
    
	/**
	* General function to calculate the hash of a given byte array
	* 
	* @param in the byte array
	* @return hash as byte array
	*/
    private byte[] calcHash(byte[] in) throws Exception{
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        sha.update(in);
        
        return sha.digest();
    }
	
	/**
	* This updates all parameters and increments the fileversion, 
	* if a local filechange is detected.
	*/
	public void localUpdate() throws Exception{
		Constants.log.addMsg("FileHandle: Local update triggered for " + this.file.getName()	+ " Scanning for changes!",3);
		boolean changed = false;
		FileInputStream stream = new FileInputStream(this.file);
        int bytesRead = 0;
        int id = 0;
		int chunkSize = (int)this.chunks.get(0).getSize();
        byte[] buffer = new byte[chunkSize];
		this.fileVersion += 1;
		this.hash = calcHash(this.file);
		this.size = this.file.length();
        
        while((bytesRead = stream.read(buffer)) != -1){
            if(id < this.chunks.size()){ //change is within existent chunks
				if(!(Arrays.equals(calcHash(buffer),this.chunks.get(id).getHash()))){ // new chunk hash != old chunk hash
					Constants.log.addMsg("FileHandle: Chunk " + id + " changed! Updating chunklist...",3);
					FileChunk updated = new FileChunk(id,calcHash(buffer),bytesRead,id*chunkSize,true);
					this.chunks.set(id,updated);
					changed = true;
				}
				if(bytesRead < chunkSize && id < (this.chunks.size()-1)){ // chunk is smaller than others and is not the last chunk -> file got smaller
					Constants.log.addMsg("FileHandle: Smaller chunk is not last chunk! Pruning following chunks...",3);
					int i = this.chunks.size()-1;
					while(i > id){
						this.chunks.removeLast();
						i--;
					}
					changed = true;
				}
				if(bytesRead > this.chunks.get(id).getSize() && id == this.chunks.size()-1){
					Constants.log.addMsg("FileHandle: Chunk " + id + " changed! Updating chunklist...",3);
					FileChunk updated = new FileChunk(id,calcHash(buffer),bytesRead,id*chunkSize,true);
					this.chunks.set(id,updated);
					changed = true;
				}
			}else{ // file is grown and needs more chunks
				Constants.log.addMsg("FileHandle: File needs more chunks than before! Adding new chunks...",3);
				FileChunk next = new FileChunk(id,calcHash(buffer),bytesRead,id*chunkSize,true);
				this.chunks.add(next);
				changed = true;
			}
            id++;
        }
		if(!changed)
			Constants.log.addMsg("No changes found...",4);
	}
	
	/**
	* This updates all parameters and increments the fileversion, 
	* if a filechange is received via XMPP.
	*/
	public void netUpdate(){
		// TODO
	}
	
	/**
	* This updates the filename in the datastructure
	*/
	public void renameFile(){
		// TODO
	}

	/**
	* Returns the data of the requested file chunk
	* 
	* @param id the id of the chunk
	* @return data of the chunk as byte array
	*/
	public byte[] getChunkData(int id){
		if(this.chunks == null){
			Constants.log.addMsg("Cannot return chunkData -> no chunk list available",1);
			return null;
		}
				
		FileChunk recent = this.chunks.get(id);
		if(!recent.isComplete()){
			Constants.log.addMsg("Cannot return chunkData -> no chunk not complete",1);
			return null;
		}
		
		try{
			FileInputStream stream = new FileInputStream(this.file);
			long bytesSkipped, bytesRead;
			byte[] buffer = new byte[(int)recent.getSize()];
			
			bytesSkipped = stream.skip(recent.getOffset()); // Jump to correct part of the file
			if(bytesSkipped != recent.getOffset())
				Constants.log.addMsg("FileHandle: Skipped more or less bytes than offset", 4);
			
			bytesRead = stream.read(buffer);
			
			if(bytesRead == -1)
				Constants.log.addMsg("FileHandle: getChunkData EOF",4);
			
			return buffer;
		}catch(IOException ioe){
			Constants.log.addMsg("Error skipping bytes in chunk:" + ioe, 1);
			return null;
		}
	}
	
	/**
	* Returns the hash of this file as readable hex string
	* @return the hex string
	*/
    public String toHexHash(byte[] in){
        StringBuilder hexString = new StringBuilder();
    	for (int i=0;i<in.length;i++) {
    	  hexString.append(Integer.toHexString(0xFF & in[i]));
    	}
        
        return hexString.toString();
    }
    
	/**
	* Returns the hash of this file as readable hex string
	* @return the hex string
	*/
    public String getHexHash(){
        StringBuilder hexString = new StringBuilder();
    	for (int i=0;i<this.hash.length;i++) {
    	  hexString.append(Integer.toHexString(0xFF & this.hash[i]));
    	}
        
        return hexString.toString();
    }
	
	public LinkedList<FileChunk> getChunkList(){
		return this.chunks;
	}
	
	public byte[] getByteHash(){
		return this.hash;
	}
	
	public boolean isComplete(){
		return this.complete;
	}
	
	public File getFile(){
		return this.file;
	}
    
    @Override
    public String toString(){
        String out = "---------- FileHandle toString ----------\n";
        out += "Filename: " + this.file.toString() + "\n";
        out += "Size: " + this.size + " Byte\n";
        out += "Chunks: " + this.chunks.size() + " pieces\n";
		for(int i = 0; i < this.chunks.size(); i++){
			out += "\t" + i + ": " + toHexHash(this.chunks.get(i).getHash()) + ", " + this.chunks.get(i).getSize() + " Bytes\n";
		}
        out += "SHA-256: " + this.getHexHash() + "\n";
		out += "Complete: " + this.isComplete() + "\n";
		out += "------------ End toString -------------\n";
        return out;
    }
}
