/*
* Peergroup - FileHandle.java
* 
* Peergroup is a P2P Shared Storage System using XMPP for data- and 
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
import java.security.NoSuchAlgorithmException;

/**
 * A FileHandle includes all information needed to work with
 * a file.
 *
 * @author Nicolas Inden
 */
public class FileHandle {
    
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
	* A linked list of FileChunk objects which this file consists of
	*/
    private LinkedList<Integer> updatedBlocks;
	/**
	* The size of the filechunks in bytes
	*/
    private int chunkSize;
	/**
	* This boolean is set true while the file is being updated from network
	*/
	private boolean updating;
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
		this.updatedBlocks = new LinkedList<Integer>();
		Constants.log.addMsg("FileHandle: New file from storage: " + this.getPath() 
								+ " (Size: " + this.size + ", Hash: " + this.getHexHash() + ")", 3);
		
        if(this.size <= 512000){								// size <= 500kByte 				-> 100kByte Chunks
			this.chunkSize = 102400;
		}else if(this.size > 512000 && this.size <= 5120000){	// 500kByte < size <= 5000kByte 	-> 200kByte Chunks
			this.chunkSize = 204800;
		}else if(this.size > 5120000 && this.size <= 51200000){	// 5000kByte < size <= 50000kByte 	-> 1000kByte Chunks
			this.chunkSize = 1024000;
		}else if(this.size > 51200000){							// 50000kByte < size 				-> 2000kByte Chunks
			this.chunkSize = 2048000;
		}
		this.createChunks(this.chunkSize);
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
		this.updatedBlocks = new LinkedList<Integer>();
		Constants.log.addMsg("FileHandle: New file from storage: " + this.getPath() 
								+ " (Size: " + this.size + ", Hash: " + this.getHexHash() + ")", 3);
		
        if(this.size <= 512000){								// size <= 500kByte 				-> 100kByte Chunks
			this.chunkSize = 102400;
		}else if(this.size > 512000 && this.size <= 5120000){	// 500kByte < size <= 5000kByte 	-> 200kByte Chunks
			this.chunkSize = 204800;
		}else if(this.size > 5120000 && this.size <= 51200000){	// 5000kByte < size <= 50000kByte 	-> 1000kByte Chunks
			this.chunkSize = 1024000;
		}else if(this.size > 51200000){							// 50000kByte < size 				-> 2000kByte Chunks
			this.chunkSize = 2048000;
		}
		this.createChunks(this.chunkSize);
    }
    
	/**
	* Use this constructor for files to be received via network
	*/
    public FileHandle(String filename, byte[] fileHash, long fileSize, LinkedList<FileChunk> chunks, int chunkSize) 
    throws Exception{ 
        this.file = new File(Constants.rootDirectory + filename);
		this.fileVersion = 0;
        this.hash = fileHash;
        this.size = fileSize;
		this.chunks = chunks;
		this.chunkSize = chunkSize;
		this.complete = false;
		this.updatedBlocks = new LinkedList<Integer>();
        Constants.log.addMsg("FileHandle: New file from network: " + filename
								+ " (Size: " + this.size + ", Hash: " + this.getHexHash() + ")", 3);
    }
    
	/**
	* Creates a linked list of FileChunk for this FileHandle
	* 
	* @param size the size of a chunk (last one might be smaller)
	*/
    private void createChunks(int size){
        if(!(this.chunks == null)){
            Constants.log.addMsg("(" + this.file.getName() + ") Chunklist not empty!", 4);
            return;
        }
        Constants.log.addMsg("FileHandle: Creating chunks for " + this.file.getName(), 3);
		try{
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
			
			//On empty file, also create one empty chunk
			if(bytesRead == -1 && id == 0){
	            FileChunk next = new FileChunk(id,calcHash(buffer),0,id*size,true);
	            this.chunks.add(next);
			}
			
			Constants.log.addMsg("FileHandle: Successfully created chunks for " + this.getPath(),2);
		}catch(IOException ioe){
			Constants.log.addMsg("createChunks: IOException: " + ioe,1);
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
    private byte[] calcHash(byte[] in){
		try{
	        MessageDigest sha = MessageDigest.getInstance("SHA-256");
	        sha.update(in);
        
	        return sha.digest();
		}catch(NoSuchAlgorithmException na){
			Constants.log.addMsg("calcHash Error: " + na,1);
			System.exit(1);
		}
        return null;
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
        byte[] buffer = new byte[chunkSize];
		this.fileVersion += 1;
		this.hash = calcHash(this.file);
		this.size = this.file.length();
        
        while((bytesRead = stream.read(buffer)) > 0){
			//change is within existent chunks
            if(id < this.chunks.size()){ 
				// new chunk hash != old chunk hash
				if(!(Arrays.equals(calcHash(buffer),this.chunks.get(id).getHash()))){ 
					Constants.log.addMsg("FileHandle: Chunk " + id + " changed! Updating chunklist...",3);
					FileChunk updated = new FileChunk(id,this.chunks.get(id).getVersion()+1,calcHash(buffer),bytesRead,id*chunkSize,true);
					this.updatedBlocks.add(new Integer(id));
					this.chunks.set(id,updated);
					changed = true;
				}
				// chunk is smaller than others and is not the last chunk -> file got smaller
				if(bytesRead < chunkSize && id < (this.chunks.size()-1)){ 
					Constants.log.addMsg("FileHandle: Smaller chunk is not last chunk! Pruning following chunks...",3);
					int i = this.chunks.size()-1;
					while(i > id){
						this.chunks.removeLast();
						i--;
					}
					changed = true;
				}
				// Last chunk got bigger
				if(bytesRead > this.chunks.get(id).getSize() && id == this.chunks.size()-1){
					Constants.log.addMsg("FileHandle: Chunk " + id + " changed! Updating chunklist...",3);
					FileChunk updated = new FileChunk(id,this.chunks.get(id).getVersion()+1,calcHash(buffer),bytesRead,id*chunkSize,true);
					this.chunks.set(id,updated);
					this.updatedBlocks.add(new Integer(id));
					changed = true;
				}
			}else{
				// file is grown and needs more chunks
				// TODO: What to do with the Chunk Version here?? 
				Constants.log.addMsg("FileHandle: File needs more chunks than before! Adding new chunks...",3);
				FileChunk next = new FileChunk(id,calcHash(buffer),bytesRead,id*chunkSize,true);
				this.chunks.add(next);
				this.updatedBlocks.add(new Integer(id));
				changed = true;
			}
            id++;
        }
		if(!changed)
			Constants.log.addMsg("No changes found...",4);
	}
	
	/**
	* Creates an empty file to reserve space on local storage
	*/
	public void createEmptyLocalFile(){
		// Create empty file on disk
		try{
			this.file.createNewFile();
			/*RandomAccessFile out = new RandomAccessFile(this.file,"rwd");
			out.setLength(this.size);
			out.close();*/
		}catch(IOException ioe){
			Constants.log.addMsg("FileHandle: Cannot create new file from network: " + ioe,4);
		}
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
	* Converts a hash in a byte array into a readable hex string
	*
	* @param in the byte array
	* @return the hex string
	*/
    public static String toHexHash(byte[] in){
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
	
	/**
	* Returns the SHA256 value for the specified block
	*
	* @param no The no of the chunk
	* @return The hash of the chunk as String
	*/
	public String getChunkHash(int no){
		return toHexHash(this.chunks.get(no).getHash());
	}
	
	/**
	* Returns a list of blocks that have updated with the last call of localUpdate()
	*
	* @return A linked list of Integer showing the IDs of updated blocks
	*/
	public LinkedList<Integer> getUpdatedBlocks(){
		return this.updatedBlocks;
	}
	
	/**
	* Clear the list of updated blocks
	*/
	public void clearUpdatedBlocks(){
		this.updatedBlocks.clear();
	}
	
	/**
	* Returns true if filename and hash match
	*
	* @param compFH The FileHandle to compare with
	* @return true if equal, else false
	*/
	public boolean equals(FileHandle compFH){
		boolean equal = true;
		
		if(!this.getPath().equals(compFH.getPath()))
			equal = false;
		if(!Arrays.equals(this.hash,compFH.getByteHash()))
			equal = false;
		
		return equal;
	}
	
	/**
	* Returns a relative path to this file/directory (e.g. /subdir/file.txt)
	*
	* @return a path to the file/directory relative to the document root (e.g. /subdir/file.txt)
	*/
	public String getPath(){
		return this.file.getPath().substring(Constants.rootDirectory.length());
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
	
	public long getSize(){
		return this.size;
	}
	
	public int getVersion(){
		return this.fileVersion;
	}
	
	public void setUpdating(boolean up){
		this.updating = up;
	}
	
	public boolean getUpdating(){
		return this.updating;
	}
    
    @Override
    public String toString(){
        String out = "\n---------- FileHandle toString ----------\n";
        out += "Filename: \t" + this.getPath() + "\n";
        out += "Size: \t\t" + this.size + " Byte\n";
        out += "Chunks: \t" + this.chunks.size() + " pieces\n";
		for(int i = 0; i < this.chunks.size(); i++){
			out += "\t" + i + ": \t" + toHexHash(this.chunks.get(i).getHash()) + ", " + this.chunks.get(i).getSize() + " Bytes\n";
		}
        out += "SHA-256: \t" + this.getHexHash() + "\n";
		out += "Complete: \t" + this.isComplete() + "\n";
		out += "------------ End toString -------------";
        return out;
    }
}
