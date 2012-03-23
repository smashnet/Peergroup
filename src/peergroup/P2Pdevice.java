/*
* Peergroup - P2Pdevice.java
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
	* The user of the P2Pdevice as String
	*/
	private String user;

	/**
	* Default constructor
	*/
	public P2Pdevice(){
		this.id = Constants.p2pCount++;
	}

	/**
	* Use this constructor to add a new P2Pdevice
	*/
	public P2Pdevice(String newUser,String newIP){
		this.id = Constants.p2pCount++;
		this.user = newUser;
		this.ip = newIP;
	}
}
