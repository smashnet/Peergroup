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
import java.util.Random;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 *
 * @author Nicolas Inden
 */
public class FileChunk {
    
	private String file;
    private int id;
	private int version;
    private byte[] chunkHash;
	private long offset;
	private int size;
	private boolean complete;
    private LinkedList<P2Pdevice> peers;
    
    public FileChunk(){
        
    }
	
	public FileChunk(String name, int no, int size, int vers, String hash, P2Pdevice node, boolean comp){
		this.file = name;
		this.id = no;
		this.size = size;
		this.offset = id*size;
		this.version = vers;
		this.chunkHash = toByteHash(hash);
		this.complete = comp;
		this.peers = new LinkedList<P2Pdevice>();
		this.peers.add(node);
	}
	
    public FileChunk(String name, int no, byte[] digest, int s, long off, boolean compl){
		this.file = name;
        this.id = no;
		this.version = 0;
        this.chunkHash = digest;
		this.size = s;
		this.offset = off;
		this.complete = compl;
		this.peers = new LinkedList<P2Pdevice>();
    }
	
    public FileChunk(String name, int no, int vers, byte[] digest, int s, long off, boolean compl){
		this.file = name;
        this.id = no;
		this.version = vers;
        this.chunkHash = digest;
		this.size = s;
		this.offset = off;
		this.complete = compl;
		this.peers = new LinkedList<P2Pdevice>();
    }
	
	/**
	* Returns the hash of this chunk as readable hex string
	* @return the hex string
	*/
    public String getHexHash(){
		HexBinaryAdapter adapter = new HexBinaryAdapter();
	    String hash = adapter.marshal(this.chunkHash);
	    return hash;
    }
	
	/**
	* Converts the hex string to byte[] and sets variable.
	*/
	public void setHexHash(String s){
		HexBinaryAdapter adapter = new HexBinaryAdapter();
		this.chunkHash = adapter.unmarshal(s);
	}
	
	/**
	* Converts the hex string to a byte[].
	*/
	public static byte[] toByteHash(String s){
		HexBinaryAdapter adapter = new HexBinaryAdapter();
		return adapter.unmarshal(s);
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
	
	public String getName(){
		return this.file;
	}
	
	public LinkedList<P2Pdevice> getPeers(){
		return this.peers;
	}
	
	public int noOfPeers(){
		return this.peers.size();
	}
	
	public void setPeers(LinkedList<P2Pdevice> newPeers){
		this.peers = newPeers;
	}
	
	public void addPeer(P2Pdevice node){
		this.peers.add(node);
	}
	
	public P2Pdevice getRandomPeer(){
		Random gen = new Random(System.currentTimeMillis());
		return this.peers.get(gen.nextInt(this.peers.size()));
	}
	
	public void clearPeers(){
		this.peers.clear();
	}
	
	public boolean isComplete(){
		return this.complete;
	}
	
	public void setComplete(boolean bool){
		this.complete = bool;
	}
}
