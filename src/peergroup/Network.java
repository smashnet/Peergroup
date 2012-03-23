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
import org.jivesoftware.smackx.muc.*;

/**
 * This class is a singelton managing the XMPP connection
 *
 * @author Nicolas Inden
 */
public class Network {
	
	private static Network instance = new Network();
	private Connection xmppCon;
	private boolean joinedAChannel = false;
	
	private Network(){
		this.xmppCon = new XMPPConnection(Constants.server);
		if(this.xmppConnect()){
			this.xmppLogin();
		}else{
			try{
				Thread.sleep(1000);
			}catch(InterruptedException ie){
				
			}finally{
				Constants.requestQueue.offer(new Request(Constants.STH_EVIL_HAPPENED,"Coudln't create Network object"));
			}
		}
	}
	
	public static Network getInstance(){
		return instance;
	}
	
	private void xmppLogin(){
		try{
			this.xmppCon.login(Constants.user,Constants.pass,Constants.resource);
			Constants.log.addMsg("Successfully logged into XMPP Server as: " +
				Constants.user + "@" + Constants.server + "/" + Constants.resource,2);
		}catch(XMPPException xe){
			Constants.log.addMsg("Failed logging into XMPP Server: " + xe,4);
		}
	}
	
	private boolean xmppConnect(){
		try{
			this.xmppCon.connect();
			Constants.log.addMsg("Successfully established connection to XMPP Server: " + Constants.server,2);
			return true;
		}catch(XMPPException xe){
			Constants.log.addMsg("Failed connecting to XMPP Server: " + xe,4);
			return false;
		}
	}
	
	public Connection getConnection(){
		return this.getInstance().xmppCon;
	}
	
	public void joinMuc(String user, String pass, String room){
		MultiUserChat muc = new MultiUserChat(getConnection(),room);
		try{
			muc.join(user,pass);
			this.joinedAChannel = true;
			Constants.log.addMsg("Successfully joined conference: " + room,2);
		}catch(XMPPException xe){
			Constants.log.addMsg("Failed joining conference: " + room,2);
			this.joinedAChannel = false;
		}
	}
	
	public void sendMUCmessage(String text){
		
	}
	
	public void xmppDisconnect(){
		this.getInstance().xmppCon.disconnect();
		Constants.log.addMsg("Disconnected from XMPP Server: " + Constants.server,4);
	}
}
