/*
* Peergroup - P2Pdevice.java
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
 * This class lets your access information about a participant in your
 * network.
 *
 * @author Nicolas Inden
 */
public class P2Pdevice {
    
	/**
	* The id of the P2Pdevice as int
	*/
	private int id;

	/**
	* The ip of the P2Pdevice as String
	*/	
	private String ip;
	
	/**
	* The port of the P2Pdevice as int
	*/	
	private int port;

	/**
	* The jid of the P2Pdevice as String
	*/
	private String jid;

	/**
	* Default constructor
	*/
	public P2Pdevice(){
		this.id = Constants.p2pCount++;
	}

	/**
	* Use this constructor to add a new P2Pdevice
	*/
	public P2Pdevice(String newJID,String newIP, int newPort){
		this.id = Constants.p2pCount++;
		this.jid = newJID;
		this.ip = newIP;
		this.port = newPort;
	}
	
	public int getID(){
		return this.id;
	}
	
	public String getIP(){
		return this.ip;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public String getJID(){
		return this.jid;
	}
}
