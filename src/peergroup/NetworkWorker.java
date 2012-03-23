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

import org.jivesoftware.smack.*;

/**
 * This thread listens for new information on the XMPP side
 * and enqueues actions in the request queue.
 *
 * @author Nicolas Inden
 */
public class NetworkWorker extends Thread {
	
	private Network myNetwork;
	
	/**
	* Creates a NetworkWorker.
	*/
	public NetworkWorker(){
	}
	
	public void stopNetworkWorker(){
		this.myNetwork.xmppDisconnect();
		this.interrupt();
	}
	
	/**
	* The run() method
	*/
	public void run(){
		Constants.log.addMsg("Networking thread started...",2);
		this.myNetwork = Network.getInstance();
		this.myNetwork.joinMuc(Constants.user, Constants.pass, Constants.conference_channel + "@" + Constants.conference_server);
		while(true){
			try{
				Thread.sleep(1000);
			}catch(InterruptedException ie){
				interrupt();
				break;
			}
		}
		Constants.log.addMsg("Networking thread interrupted. Closing...",4);
	}
    
}
