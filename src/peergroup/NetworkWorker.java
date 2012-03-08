/*
* Peergroup - NetworkWorker.java
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

import java.util.List;


/**
 * This is the thread running all network related things.
 *
 * @author Nicolas Inden
 */
public class NetworkWorker extends Thread {
	
	/**
	* Creates a NetworkWorker.
	*/
	public NetworkWorker(){
	}
	
	/**
	* The run() method
	*/
	public void run(){
		Constants.log.addMsg("Networking thread started...",2);
		
		Constants.log.addMsg("Networking thread interrupted. Closing...",4);
	}
    
}
