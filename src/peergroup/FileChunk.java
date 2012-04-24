/*
* Peergroup - FileChunk.java
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

import java.util.LinkedList;

/**
 *
 * @author Nicolas Inden
 */
public class FileChunk {
    
    private int id;
	private int version;
    private byte[] chunkHash;
	private long offset;
	private long size;
	private boolean complete;
    private LinkedList<P2Pdevice> peers;
    
    public FileChunk(){
        
    }
    
    public FileChunk(int no, byte[] digest, long s, long off, boolean compl){
        this.id = no;
		this.version = 0;
        this.chunkHash = digest;
		this.size = s;
		this.offset = off;
		this.complete = compl;
		this.peers = new LinkedList<P2Pdevice>();
    }
	
	public FileChunk(int no, String hash, P2Pdevice node){
		this.id = no;
		this.chunkHash = toByteHash(hash);
		this.peers = new LinkedList<P2Pdevice>();
		this.peers.add(node);
	}
	
    public FileChunk(int no, int vers, byte[] digest, long s, long off, boolean compl){
        this.id = no;
		this.version = vers;
        this.chunkHash = digest;
		this.size = s;
		this.offset = off;
		this.complete = compl;
		this.peers = new LinkedList<P2Pdevice>();
    }
	
	public FileChunk(int no, byte[] digest, long s, long off, boolean compl, LinkedList<P2Pdevice> peers){
		this.id = no;
		this.version = 0;
		this.chunkHash = digest;
		this.size = s;
		this.offset = off;
		this.peers = peers;
		this.complete = compl;
	}
	
	public FileChunk(int no, int vers, byte[] digest, long s, long off, boolean compl, LinkedList<P2Pdevice> peers){
		this.id = no;
		this.version = vers;
		this.chunkHash = digest;
		this.size = s;
		this.offset = off;
		this.peers = peers;
		this.complete = compl;
	}
	
	/**
	* Returns the hash of this chunk as readable hex string
	* @return the hex string
	*/
    public String getHexHash(){
        StringBuilder hexString = new StringBuilder();
    	for (int i=0;i<this.chunkHash.length;i++) {
    	  hexString.append(Integer.toHexString(0xFF & this.chunkHash[i]));
    	}
        
        return hexString.toString();
    }
	
	/**
	* Converts the hex string to byte[] and sets variable.
	*/
	public void setHexHash(String s){
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
		    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
		                         + Character.digit(s.charAt(i+1), 16));
		}
		
		this.chunkHash = data;
	}
	
	/**
	* Converts the hex string to a byte[].
	*/
	public static byte[] toByteHash(String s){
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
		    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
		                         + Character.digit(s.charAt(i+1), 16));
		}
		
		return data;
	}
	
	public int getID(){
		return this.id;
	}
	
	public void setVersion(int vers){
		this.version = vers;
	}
	
	public int getVersion(){
		return this.version;
	}
	
	public byte[] getHash(){
		return this.chunkHash;
	}

	public long getOffset(){
		return this.offset;
	}
	
	public long getSize(){
		return this.size;
	}
	
	public LinkedList<P2Pdevice> getPeers(){
		return this.peers;
	}
	
	public void setPeers(LinkedList<P2Pdevice> newPeers){
		this.peers = newPeers;
	}
	
	public void addPeer(P2Pdevice node){
		this.peers.add(node);
	}
	
	public void clearPeers(){
		this.peers.clear();
	}
	
	public boolean isComplete(){
		return this.complete;
	}
}
