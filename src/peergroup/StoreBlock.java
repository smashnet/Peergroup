/*
* Peergroup - StoreBlock.java
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

/**
 *
 * @author Nicolas Inden
 */
public class StoreBlock {
    
	private FileHandle handle;
    private int blockID;
	private String hash;
	private P2Pdevice node;
	private byte[] buffer;
    
    public StoreBlock(FileHandle handle, int id, String hash, P2Pdevice device, byte[] bytes){
        this.handle = handle;
		this.blockID = id;
		this.hash = hash;
		this.node = device;
		this.buffer = bytes;
    }
	
	public FileHandle getFileHandle(){
		return this.handle;
	}
	
	public String getName(){
		return this.handle.getPath();
	}
	
	public int getVersion(){
		return this.handle.getVersion();
	}
	
	public int getID(){
		return this.blockID;
	}
	
	public String getHexHash(){
		return this.hash;
	}
	
	public P2Pdevice getDevice(){
		return this.node;
	}
	
	public byte[] getData(){
		return this.buffer;
	}
}
