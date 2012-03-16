/*
* Peergroup - FileChunk.java
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

import java.util.LinkedList;

/**
 *
 * @author Nicolas Inden
 */
public class FileChunk {
    
    private int id;
    private byte[] chunkHash;
	private long offset;
	private long size;
	private boolean complete;
    private LinkedList<P2Pdevice> peers;
    
    public FileChunk(){
        
    }
    
    public FileChunk(int no, byte[] digest, long s, long off, boolean compl){
        this.id = no;
        this.chunkHash = digest;
		this.size = s;
		this.offset = off;
		this.complete = compl;
    }
	
	public FileChunk(int no, byte[] digest, long s, long off, boolean compl, LinkedList<P2Pdevice> peers){
		this.id = no;
		this.chunkHash = digest;
		this.size = s;
		this.offset = off;
		this.peers = peers;
		this.complete = compl;
	}
	
	public int getID(){
		return this.id;
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
	
	public boolean isComplete(){
		return this.complete;
	}
}