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
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

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
	* A list of the blocks that changed in the last localUpdate() invocation
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
     * Use this constructor for complete files located on your device
     */
    public FileHandle(String filename) throws Exception{ 
        this.file = new File(filename);
		this.updating = true;
		this.fileVersion = 1;
        this.hash = this.calcHash(this.file);
        this.size = this.file.length();
		this.updatedBlocks = new LinkedList<Integer>();
		Constants.log.addMsg("FileHandle: New file from storage: " + this.getPath() 
								+ " (Size: " + this.size + ", Hash: " + this.getHexHash() + ")", 3);
		
		// Use fixed chunk size for testing
		this.chunkSize = 512000;
		/*
        *if(this.size <= 512000){								// size <= 500kByte 				-> 100kByte Chunks
		*	this.chunkSize = 102400;
		*}else if(this.size > 512000 && this.size <= 5120000){	// 500kByte < size <= 5000kByte 	-> 200kByte Chunks
		*	this.chunkSize = 204800;
		*}else if(this.size > 5120000 && this.size <= 51200000){	// 5000kByte < size <= 50000kByte 	-> 1000kByte Chunks
		*	this.chunkSize = 1024000;
		*}else if(this.size > 51200000){							// 50000kByte < size 				-> 2000kByte Chunks
		*	this.chunkSize = 2048000;
		*}
		*/
			
		this.createChunks(this.chunkSize,1);
		this.updating = false;
    }
    
	/**
	* Use this constructor for files to be received via network
	*/
    public FileHandle(String filename, byte[] fileHash, long fileSize, LinkedList<FileChunk> chunks, int chunkSize) 
    throws Exception{ 
        this.file = new File(Constants.rootDirectory + filename);
		this.updating = true;
		this.fileVersion = 1;
        this.hash = fileHash;
        this.size = fileSize;
		this.chunks = chunks;
		this.chunkSize = chunkSize;
		this.updatedBlocks = new LinkedList<Integer>();
        Constants.log.addMsg("FileHandle: New file from network: " + filename
								+ " (Size: " + this.size + ", Hash: " + this.getHexHash() + ")", 3);
		this.updating = false;
    }
    
	/**
	* Creates a linked list of FileChunk for this FileHandle
	* 
	* @param size the size of a chunk (last one might be smaller)
	*/
    private void createChunks(int size, int vers){
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
	            FileChunk next = new FileChunk(this.getPath(),id,vers,calcHash(buffer),bytesRead,id*size,true);
	            this.chunks.add(next);
	            id++;
	        }
			
			//On empty file, also create one empty chunk
			if(bytesRead == -1 && id == 0){
	            FileChunk next = new FileChunk(this.getPath(),id,vers,calcHash(buffer),0,id*size,true);
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
	*
	* @return true if file has changed, else false
	*/
	public boolean localUpdate() throws Exception{
		Constants.log.addMsg("FileHandle: Local update triggered for " + this.file.getName()	+ ". Scanning for changes!",3);
		boolean changed = false;
		FileInputStream stream = new FileInputStream(this.file);
        int bytesRead = 0;
        int id = 0;
        byte[] buffer = new byte[chunkSize];
		this.fileVersion += 1;
		if(!Arrays.equals(this.hash,calcHash(this.file))){
			this.hash = calcHash(this.file);
			changed = true;
		}
		this.size = this.file.length();
        
        while((bytesRead = stream.read(buffer)) > 0){
			//change is within existent chunks
            if(id < this.chunks.size()){ 
				// new chunk hash != old chunk hash
				if(!(Arrays.equals(calcHash(buffer),this.chunks.get(id).getHash()))){
					Constants.log.addMsg("FileHandle: Chunk " + id + " changed! (Different Hashes) Updating chunklist...",3);
					FileChunk updated = new FileChunk(this.getPath(),id,this.chunks.get(id).getVersion()+1,calcHash(buffer),bytesRead,id*chunkSize,true);
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
					FileChunk updated = new FileChunk(this.getPath(),id,this.chunks.get(id).getVersion()+1,calcHash(buffer),bytesRead,id*chunkSize,true);
					this.chunks.set(id,updated);
					this.updatedBlocks.add(new Integer(id));
					changed = true;
				}
			}else{
				// file is grown and needs more chunks
				Constants.log.addMsg("FileHandle: File needs more chunks than before! Adding new chunks...",3);
				FileChunk next = new FileChunk(this.getPath(),id,this.fileVersion,calcHash(buffer),bytesRead,id*chunkSize,true);
				this.chunks.add(next);
				this.updatedBlocks.add(new Integer(id));
				changed = true;
			}
            id++;
        }
		if(!changed)
			Constants.log.addMsg("No changes found...",4);
		return changed;
	}
	
	/**
	* Insert new hashes and version on changed blocks
	*
	* @param blocks The list of blocks that need to be downloaded
	*/
	public void updateChunkList(LinkedList<String> blocks, P2Pdevice node){
		for(String s : blocks){
			updateChunk(s,node);
		}
	}
	
	public void updateChunk(String chunk, P2Pdevice node){
		String[] tmp = chunk.split(":");
		int id = (Integer.valueOf(tmp[0])).intValue();
		int vers = (Integer.valueOf(tmp[1])).intValue();
		String hash = tmp[2];
		if(id < this.chunks.size()){
			this.chunks.get(id).setHexHash(hash);
			this.chunks.get(id).setVersion(vers);
		}else{
			this.chunks.add(new FileChunk(this.getPath(),id,512000,vers,hash,node,false));
		}
	}
	
	public void addP2PdeviceToBlock(int id, P2Pdevice node){
		this.chunks.get(id).addPeer(node);
	}
	
	public void addP2PdeviceToAllBlocks(P2Pdevice node){
		for(FileChunk f : this.chunks){
			f.addPeer(node);
		}
	}
	
	public void clearP2Pdevices(){
		for(FileChunk f : this.chunks){
			f.clearPeers();
		}
	}
	
	/**
	* Creates an empty file to reserve space on local storage
	*/
	public void createEmptyLocalFile(){
		// Create empty file on disk
		try{
			this.file.createNewFile();
		}catch(IOException ioe){
			Constants.log.addMsg("FileHandle: Cannot create new file from network: " + ioe,4);
		}
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
		
		if(id >= this.chunks.size()){
			Constants.log.addMsg("Cannot return chunkData -> ID exceeds list",1);
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
				Constants.log.addMsg("FileHandle: getChunkData EOF - ID: " + id,4);
			
			return buffer;
		}catch(IOException ioe){
			Constants.log.addMsg("Error skipping bytes in chunk:" + ioe, 1);
			return null;
		}
	}
	
	/**
	* Writes a chunk of data to the local storage
	*
	* @param id The chunk ID
	* @param data The data as byte array
	*/
	public void setChunkData(int id, String hash, P2Pdevice node, byte[] data){
		if(this.chunks == null){
			Constants.log.addMsg("Cannot set chunkData -> no chunk list available",1);
			return;
		}
		FileChunk recent;
		if(id >= this.chunks.size()){
			recent = new FileChunk(this.getPath(),id,512000,this.fileVersion,hash,node,true);
		}else{
			recent = this.chunks.get(id);
		}
		
		try{
			RandomAccessFile stream = new RandomAccessFile(this.file,"rwd");
			stream.seek(recent.getOffset()); // Jump to correct part of the file
			stream.write(data);
			stream.close();
			
			recent.setHexHash(hash);
			recent.setComplete(true);
			recent.setDownloading(false);
		}catch(IOException ioe){
			Constants.log.addMsg("Error writing to file:" + ioe, 1);
		}
	}
	
	/**
	* Converts a hash in a byte array into a readable hex string
	*
	* @param in the byte array
	* @return the hex string
	*/
    public static String toHexHash(byte[] in){
		HexBinaryAdapter adapter = new HexBinaryAdapter();
	    String hash = adapter.marshal(in);
	    return hash;
    }
    
	/**
	* Returns the hash of this file as readable hex string
	* @return the hex string
	*/
    public String getHexHash(){
		HexBinaryAdapter adapter = new HexBinaryAdapter();
	    String hash = adapter.marshal(this.hash);
	    return hash;
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
	
	public LinkedList<FileChunk> getChunks(){
		return this.chunks;
	}
	
	public void updateChunkVersion(int id){
		for(FileChunk f : this.chunks){
			if(id == f.getID()){
				f.setVersion(this.fileVersion);
				return;
			}
		}
	}
	
	public void setChunkVersion(int id, int vers){
		for(FileChunk f : this.chunks){
			if(id == f.getID()){
				f.setVersion(vers);
				return;
			}
		}
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
	* Returns a list of strings looking like this: "id:version:hash"
	* @return the list
	*/
	public LinkedList<String> getBlockIDwithHash(){
		LinkedList<String> tmp = new LinkedList<String>();
		for(FileChunk f : this.chunks){
			String newBlock = f.getID() + ":" + f.getVersion() + ":" 
				+ f.getHexHash() + ":" + f.getSize();
			tmp.add(newBlock);
		}
		return tmp;
	}
	
	public void updateBlocks(LinkedList<String> blocks,int vers){
		for(String s : blocks){
			//TODO: Change size
			this.chunks.get(s.charAt(0)-48).setComplete(false);
		}
		if(blocks.size() == this.chunks.size()){
			// Do nothing if all blocks have changed
			return;
		}
		int i = 0;
		for(FileChunk f : this.chunks){
			if(f.getID() != blocks.get(i).charAt(0)-48){
				f.setVersion(vers);
			}else{
				if(i < blocks.size()-1)
					i++;
			}
		}
	}
	
	public void trimFile(){
		try{
			RandomAccessFile thisFile = new RandomAccessFile(this.file,"rws");
			thisFile.setLength(this.size);
			thisFile.close();
			
			double fileSize = (double)this.size;
			double cSize = (double)this.chunkSize;
			int blocks = (int)Math.ceil(fileSize/cSize);
			
			int diff = this.chunks.size() - blocks;
			if(diff > 0){
				for(int i = 0; i < diff; i++){
					this.chunks.removeLast();
				}
			}
		}catch(FileNotFoundException e){
			Constants.log.addMsg("No file to trim, this should not happen!! (" + e + ")",1);
		}catch(IOException ioe){
			Constants.log.addMsg("Error trimming file, this should not happen!! (" + ioe + ")",1);
		}
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
	
	public void setByteHash(byte[] newHash){
		this.hash = newHash;
	}
	
	public boolean isComplete(){
		for(FileChunk f : this.chunks){
			if(f.getVersion() != this.getVersion()){
				return false;
			}
		}
		return true;
	}
	
	public File getFile(){
		return this.file;
	}
	
	public long getSize(){
		return this.size;
	}
	
	public int getChunkSize(){
		return this.chunkSize;
	}
	
	public void setSize(long newSize){
		this.size = newSize;
	}
	
	public int getVersion(){
		return this.fileVersion;
	}
	
	public void setVersion(int newVers){
		this.fileVersion = newVers;
	}
	
	public void setUpdating(boolean up){
		this.updating = up;
	}
	
	public boolean isUpdating(){
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
