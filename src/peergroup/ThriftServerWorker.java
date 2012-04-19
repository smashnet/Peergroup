/*
* Peergroup - ThriftServerWorker.java
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
 * This thread listens for thrift requests and processes them.
 *
 * @author Nicolas Inden
 */
public class ThriftServerWorker extends Thread {
	
	public void start(){
	
	}
	
	/**
	* The run() method
	*/
	public void run(){
		
		Constants.log.addMsg("ThriftServer thread interrupted. Closing...",4);
	}   
}
