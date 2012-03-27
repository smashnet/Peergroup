/*
* Peergroup - NetworkWorker.java
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

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.muc.*;

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
		this.myNetwork.leaveMUC();
		this.myNetwork.xmppDisconnect();
		Constants.log.addMsg("Networking thread stopped. Closing...",4);
		this.stop();
	}
	
	/**
	* The run() method
	*/
	public void run(){
		Constants.log.addMsg("Networking thread started...",2);
		this.myNetwork = Network.getInstance();
		this.myNetwork.joinMUC(Constants.user, Constants.pass, 
			Constants.conference_channel + "@" + Constants.conference_server);
		this.myNetwork.sendMUCmessage("Hello World!");
		
		while(!isInterrupted()){
			Message newMessage = this.myNetwork.getNextMessage();
			if(newMessage != null)
    			Constants.log.addMsg("Incoming message: " + newMessage.getBody(),3);
		}
		
		Constants.log.addMsg("Networking thread interrupted. Closing...",4);
	}   
}
