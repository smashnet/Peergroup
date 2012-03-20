/*
* Peergroup - Network.java
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
 * This class is a singelton
 *
 * @author Nicolas Inden
 */
public class Network {
	
	private static Network instance = new Network();
    private Connection xmppCon;
	
	private Network(){
		this.xmppCon = new XMPPConnection(Constants.server);
	}
	
	public static Network getInstance(){
		return instance;
	}
	
	public void XMPPlogin(){
		try{
			this.getInstance().xmppCon.login(Constants.user,Constants.pass,Constants.resource);
			Constants.log.addMsg("Successfully logged into XMPP Server as: " +
				Constants.user + "@" + Constants.server + "/" + Constants.resource,2);
		}catch(XMPPException xe){
			Constants.log.addMsg("Failed logging into XMPP Server: " + xe,4);
		}
	}
	
	public void XMPPconnect(){
		try{
			this.getInstance().xmppCon.connect();
			Constants.log.addMsg("Successfully established connection to XMPP Server: " + Constants.server,2);
		}catch(XMPPException xe){
			Constants.log.addMsg("Failed connecting to XMPP Server: " + xe,4);
		}
	}
	
	public void XMPPdisconnect(){
		this.getInstance().xmppCon.disconnect();
		Constants.log.addMsg("Disconnected from XMPP Server: " + Constants.server,4);
	}
}
