/*
* Peergroup - DLRequest.java
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
 * A general request consisting of a type and the content
 *
 * @author Nicolas Inden
 */
public class DLRequest extends Request{
	
	private String name;
	private int blockID;
	private int version;
	private String hash;
	private P2Pdevice node;
    
	/**
	* Constructor to use if no lamport time is given or necessary
	*/
	public DLRequest(int newID, int vers, String newName, int newBlockID, String newHash, P2Pdevice newNode){
		super(newID);
		this.name = newName;
		this.blockID = newBlockID;
		this.version = vers;
		this.hash = newHash;
		this.node = newNode;
	}
	
	/**
	* Constructor to use if lamport timestamps are needed
	*/
	public DLRequest(int newID, int vers, String newName, int newBlockID, String newHash, P2Pdevice newNode, long newLamp){
		super(newID,newLamp);
		this.name = newName;
		this.blockID = newBlockID;
		this.version = vers;
		this.hash = newHash;
		this.node = newNode;
	}
	
	public void setID(int newID){
		super.type = newID;
	}
	
	public void setLamport(long newLamp){
		super.lamportTime = newLamp;
	}
	
	public int getID(){
		return super.type;
	}
	
	public long getLamportTime(){
		return super.lamportTime;
	}
	
	public String getName(){
		return this.name;
	}
	
	public int getBlockID(){
		return this.blockID;
	}
	
	public String getHash(){
		return this.hash;
	}
	
	public P2Pdevice getNode(){
		return this.node;
	}
	
	public int getVersion(){
		return this.version;
	}
	
	public void setVersion(int vers){
		this.version = vers;
	}
}
