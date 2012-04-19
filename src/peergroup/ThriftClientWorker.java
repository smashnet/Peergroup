/*
* Peergroup - ThriftClientWorker.java
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

import java.util.*;

/**
 * This thread requests blocks or FileList information from other peers.
 *
 * @author Nicolas Inden
 */
public class ThriftClientWorker extends Thread {
	
	private int id;
	
	/**
	* Creates a ThriftClientWorker.
	*/
	public ThriftClientWorker(int newID){
		this.id = newID;
	}
	
	public void stopThriftClientWorker(){
		this.interrupt();
	}
	
	/**
	* The run() method
	*/
	public void run(){
		
		Constants.log.addMsg("ThriftClient-Thread " + this.id + " interrupted/finished. Closing...",4);
	}   
}
