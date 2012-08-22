/*
* Peergroup - FileChunk.java
* 
* This file is part of Peergroup.
*
* Peergroup is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Peergroup is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* Copyright (c) 2012 Nicolas Inden
*/

package peergroup;

import java.util.LinkedList;
import java.util.Random;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

 /**
 * Defines a chunk of a files including the position and size in the 
 * file and further attributes.
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
	private boolean failed;
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
		this.failed = false;
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
		this.failed = false;
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
		this.failed = false;
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
		this.failed = false;
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
		for(P2Pdevice dev : this.peers){
			if(node.equals(dev))
				return;
		}
		this.peers.add(node);
	}
	
	public P2Pdevice getRandomPeer(){
		if(this.peers.size() > 0){
			Random gen = new Random(System.currentTimeMillis());
			return this.peers.get(gen.nextInt(this.peers.size()));
		}else{
			return null;
		}
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
	
	public void setFailed(boolean val){
		this.failed = val;
	}
	
	public boolean hasFailed(){
		return this.failed;
	}
	
}
