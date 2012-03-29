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
		this.myNetwork.joinMUC(Constants.user, Constants.pass, 
			Constants.conference_channel + "@" + Constants.conference_server);
		this.myNetwork.sendMUCmessage("Hi, I'm a peergroup client. I do awesome things :-)");
		this.myNetwork.sendMUCNewFile("foo",123,"bar");
		
		while(!isInterrupted()){
			// read next message from XMPP
			Message newMessage = this.myNetwork.getNextMessage();
			// messages with body are not from a peergroup clients and are only displayed
			if(newMessage.getBody() != null){
				Constants.log.addMsg("Message: " + newMessage.getBody(),3);
				continue;
			}
			// ignore messages sent by yourself
			if(newMessage.getFrom().equals(Constants.getJID())){
				continue;
			}
			// extract message type from message
			int type = ((Integer)newMessage.getProperty("Type")).intValue();
			String filename;
			
			/*
			* TODO: Proper handling of cases
			*/
			switch(type){
				case 1: // someone announced a new file via XMPP
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("New file discovered via XMPP: " + filename);
					break;
				case 2: // someone announced a delete via XMPP
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("File deletion discovered via XMPP: " + filename);
					break;
				case 3: // someone announced a fileupdate via XMPP
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("File update discovered via XMPP: " + filename);
					break;
				case 4: // someone announced that a file download is completed
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("Completed file download discovered via XMPP: " + filename);
				default:
			}
		}
		
		Constants.log.addMsg("Networking thread interrupted. Closing...",4);
	}   
}
