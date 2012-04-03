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

import java.util.*;
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
		this.setName("XMPP Thread");
		Constants.log.addMsg("Networking thread started...",2);
		this.myNetwork = Network.getInstance();
		
		while(!isInterrupted()){
			// read next message from XMPP
			Message newMessage = this.myNetwork.getNextMessage();
			
			// messages with body are not from peergroup clients and are only displayed
			if(newMessage.getBody() != null){
				Constants.log.addMsg("Message: " + newMessage.getBody(),3);
				continue;
			}
			
			// adjust lamport time
			long msgLamp = ((Long)newMessage.getProperty("LamportTime")).longValue();
			this.myNetwork.updateLamportTime(msgLamp);
			
			// ignore messages sent by yourself
			if(newMessage.getProperty("JID").equals(Constants.getJID())){
				continue;
			}
			
			// extract message type from message
			int type = ((Integer)newMessage.getProperty("Type")).intValue();
			String filename;
			
			/*
			* TODO: Proper handling of cases
			*/
			switch(type){
				case 1: 
					/*
					* Someone announced a new file via XMPP
					* Available information:
					* "JID","IP","name","size","sha256"
					*/
						
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("New file discovered via XMPP: " + filename + " Lamport: " + msgLamp);
					Constants.requestQueue.offer(new XMPPRequest(Constants.REMOTE_ENTRY_CREATE,newMessage));
					break;
				case 2: 
					/*
					* Someone announced a delete via XMPP
					* Available information:
					* "JID","name"
					*/
					
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("File deletion discovered via XMPP: " + filename + " Lamport: " + msgLamp);
					Constants.requestQueue.offer(new XMPPRequest(Constants.REMOTE_ENTRY_DELETE,newMessage));
					break;
				case 3: 
					/*
					* Someone announced a fileupdate via XMPP
					* Available information:
					* "JID","IP","name","version","size","blocks","sha256"
					*/
					
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("File update discovered via XMPP: " + filename + " Lamport: " + msgLamp);
					Constants.requestQueue.offer(new XMPPRequest(Constants.REMOTE_ENTRY_MODIFY,newMessage));
					break;
				case 4: 
					/*
					* Someone announced that a file download is completed
					* Available information:
					* "JID","IP","name","version","size","sha256"
					*/
					
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("Completed file download discovered via XMPP: " + filename + " Lamport: " + msgLamp);
					Constants.requestQueue.offer(new XMPPRequest(Constants.REMOTE_ENTRY_COMPLETE,newMessage));
				default:
			}
		}
		
		Constants.log.addMsg("Networking thread interrupted. Closing...",4);
	}   
}
