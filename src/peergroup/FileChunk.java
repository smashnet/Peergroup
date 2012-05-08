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
	private boolean downloading;
    private LinkedList<P2Pdevice> peers;
    
    public FileChunk(){
        
    }
	
	public FileChunk(String name, int no, int size, int vers, String hash, P2Pdevice node, boolean comp){
		this.file = name;
		this.id = no;
		this.size = size;
		this.offset = id*Constants.chunkSize;
		this.version = vers;
		this.chunkHash = toByteHash(hash);
		this.complete = comp;
		this.downloading = false;
		this.peers = new LinkedList<P2Pdevice>();
		this.peers.add(node);
	}
	
	public FileChunk(String name, int no, int size, int vers, String hash, LinkedList<P2Pdevice> nodes, boolean comp){
		this.file = name;
		this.id = no;
		this.size = size;
		this.offset = id*Constants.chunkSize;
		this.version = vers;
		this.chunkHash = toByteHash(hash);
		this.complete = comp;
		this.downloading = false;
		this.peers = nodes;
	}
	
    public FileChunk(String name, int no, byte[] digest, int s, long off, boolean compl){
		this.file = name;
        this.id = no;
		this.version = 0;
        this.chunkHash = digest;
		this.size = s;
		this.offset = off;
		this.complete = compl;
		this.downloading = false;
		this.peers = new LinkedList<P2Pdevice>();
		this.peers.add(new P2Pdevice(Constants.getJID(),Constants.ipAddress,Constants.p2pPort));
    }
	
    public FileChunk(String name, int no, int vers, byte[] digest, int s, long off, boolean compl){
		this.file = name;
        this.id = no;
		this.version = vers;
        this.chunkHash = digest;
		this.size = s;
		this.offset = off;
		this.complete = compl;
		this.downloading = false;
		this.peers = new LinkedList<P2Pdevice>();
		this.peers.add(new P2Pdevice(Constants.getJID(),Constants.ipAddress,Constants.p2pPort));
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
	
	public static byte[] toByteHash(String s){
		HexBinaryAdapter adapter = new HexBinaryAdapter();
		return adapter.unmarshal(s);
	}
	
	public void deletePeer(String jid){
		for(int i = 0; i < this.peers.size();i++){
			if(this.peers.get(i).getJID().equals(jid))
				this.peers.remove(i);
		}
	}
	
	public int getID(){
		return this.id;
	}
	
	public void decrVersion(){
		this.version--;
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
	
	public int getSize(){
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
	
	public void setSize(int size){
		this.size = size;
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
	
	public boolean isDownloading(){
		return this.downloading;
	}
	
	public void setDownloading(boolean bool){
		this.downloading = bool;
	}
	
}
