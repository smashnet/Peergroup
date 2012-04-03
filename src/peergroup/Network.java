/*
* Peergroup - Network.java
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

import java.util.LinkedList;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smackx.muc.*;

/**
 * This class is a singelton managing the XMPP connection
 *
 * @author Nicolas Inden
 */
public class Network {
	
	private static Network instance = new Network();
	private Connection xmppCon;
	private MultiUserChat muc;
	private boolean joinedAChannel = false;
	private long lamportTime;
	
	/**
	* The default constructor. It initializes the Connection object with the XMPP server
	* supplied in the JID from the commandline args. Then it tries to establish a connection
	* and logs in the user.
	*/
	private Network(){
		this.xmppCon = new XMPPConnection(Constants.server);
		this.lamportTime = 0;
		if(this.xmppConnect()){
			this.xmppLogin();
		}else{
			try{
				Thread.sleep(1000);
			}catch(InterruptedException ie){
				
			}finally{
				Constants.requestQueue.offer(new FSRequest(Constants.STH_EVIL_HAPPENED,"Coudln't create Network object"));
			}
		}
	}
	
	/**
	* Network is a singleton, this returns the instance of Network (or creates one, if none exists)
	*/
	public static Network getInstance(){
		return instance;
	}
	
	/**
	* Once the connection is successfully established, this logs in the user
	* specified in the commandline arguments
	*/
	private void xmppLogin(){
		try{
			this.xmppCon.login(Constants.user,Constants.pass,Constants.resource);
			Constants.log.addMsg("Successfully logged into XMPP Server as: " +
				Constants.user + "@" + Constants.server + "/" + Constants.resource,2);
		}catch(XMPPException xe){
			Constants.log.addMsg("Failed logging into XMPP Server: " + xe,4);
		}
	}
	
	
	/**
	* Connects to the XMPP Server the xmppCon object was initialized with
	*
	* @return Returns true, if the connection was successfully established, else false
	*/
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
	
	/**
	* Gets the Connection object from the Network instance
	*
	* @return The Connection object managing the connection to the XMPP Server
	*/
	public Connection getConnection(){
		return this.getInstance().xmppCon;
	}
	
	/**
	* Joins the MuC room specified by the supplied information
	*
	* @param user The username you want to use in the room
	* @param pass The password necessary to join the corresponding room (not the one for your JID)
	* @param roomAndServer The string representing the exact room to join (e.g. foo@conference.bar.com)
	*/
	public void joinMUC(String user, String pass, String roomAndServer){
		this.muc = new MultiUserChat(getConnection(),roomAndServer);
		DiscussionHistory history = new DiscussionHistory();
		history.setMaxStanzas(0);
		try{
			this.muc.join(user, pass, history, SmackConfiguration.getPacketReplyTimeout());
			this.joinedAChannel = true;
			Constants.log.addMsg("Successfully joined conference: " + roomAndServer,2);
		}catch(XMPPException xe){
			Constants.log.addMsg("Failed joining conference: " + roomAndServer + " " + xe,4);
			this.joinedAChannel = false;
		}
	}
	
	/**
	* Sends a visible message in the currently joined MuC room
	*
	* @param text The text to be sent
	*/
	public void sendMUCmessage(String text){
		if(!this.joinedAChannel){
			Constants.log.addMsg("Sorry, cannot send message, we are not connected to a room!",4);
			return;
		}
		Message newMessage = this.muc.createMessage();
		newMessage.setType(Message.Type.groupchat);
		
		newMessage.setBody(text);
		
		try{
			this.muc.sendMessage(newMessage);		
		}catch(XMPPException xe){
			Constants.log.addMsg("Couldn't send XMPP message: " + newMessage.toXML() + "\n" + xe,4);
		}
	}
	
	/**
	* Returns the next message received from the XMPP server. This blocks until
	* a message is there.
	*
	* --- Maybe we should do all message handling in this class, and only return a String? ---
	*
	* @return the Message object
	*/
	public Message getNextMessage(){
	    return this.muc.nextMessage();
	}
	
	/**
	* Leave the current Multi-User-Chat room (recommended before quitting the program)
	*/
	public void leaveMUC(){
		this.muc.leave();
		Constants.log.addMsg("Left conference room: " + Constants.conference_channel,4);
	}
	
	/**
	* Disconnect from the XMPP server (recommended before quitting the program)
	*/
	public void xmppDisconnect(){
		this.getInstance().xmppCon.disconnect();
		Constants.log.addMsg("Disconnected from XMPP Server: " + Constants.server,4);
	}
	
	/**
	* Creates a custom Message object with all always present properties
	*
	* @return The Message object
	*/
	private Message createMessageObject(){
		incrementLamportTime();
		Message newMessage = this.muc.createMessage();
		newMessage.setType(Message.Type.groupchat);
		newMessage.setProperty("LamportTime",this.lamportTime);
		
		return newMessage;
	}
	
	/**
	* Sets the lamport clock to the given value
	*
	* @param value The value
	*/
	public void setLamportTime(long value){
		this.lamportTime = value;
	}
	
	/**
	* Updates the local lamport time: If the given value is greater-equal
	* to the current local lamport time, the local lamport time is set to
	* value+1.
	*
	* @param value The value
	*/
	public void updateLamportTime(long value){
		if(value >= this.lamportTime){
			this.lamportTime = value+1;
		}else{
			this.lamportTime++;
		}
	}
	
	/**
	* Increments the local lamport time by one
	*/
	public void incrementLamportTime(){
		this.lamportTime++;
	}
	
	/**
	* Gets the current lamport time
	*
	* @return The value
	*/
	public long getLamportTime(){
		return this.lamportTime;
	}
	
	/* -------- Primitives for sending and receiving XMPP packets ----------
	* Type:
	*	1: new file
	*	2: delete file
	*	3: update file
	*	4: completed file
	*/
	
	/**
	* This sends new-file information to other participants
	*
	* @param filename The filename of the new file
	* @param size The filesize of the new file
	* @param hash The new SHA256 value of the file
	*/
	public void sendMUCNewFile(String filename, long size, byte[] hash){
		if(!this.joinedAChannel){
			Constants.log.addMsg("Sorry, cannot send message, we are not connected to a room!",4);
			return;
		}
		Message newMessage = this.createMessageObject();
		
		/*
		* Set message properties
		*/
		newMessage.setProperty("Type",1);
		newMessage.setProperty("JID",Constants.getJID());
		newMessage.setProperty("IP",Constants.ipAddress);
		newMessage.setProperty("name",filename);
		newMessage.setProperty("size",size);
		newMessage.setProperty("sha256",hash);
		
		try{
			this.muc.sendMessage(newMessage);
			Constants.log.addMsg("Sending XMPP: -NEW- " + filename + " - " + size + "Bytes - " 
				+ FileHandle.toHexHash(hash),2);	
		}catch(XMPPException xe){
			Constants.log.addMsg("Couldn't send XMPP message: " + newMessage.toXML() + "\n" + xe,4);
		}
	}
	
	/**
	* This sends delete-file information to other participants
	*
	* @param filename The filename of the deleted file
	*/	
	public void sendMUCDeleteFile(String filename){
		if(!this.joinedAChannel){
			Constants.log.addMsg("Sorry, cannot send message, we are not connected to a room!",4);
			return;
		}
		Message newMessage = this.createMessageObject();
		
		/*
		* Set message properties
		*/
		newMessage.setProperty("Type",2);
		newMessage.setProperty("JID",Constants.getJID());
		newMessage.setProperty("name",filename);
		
		try{
			this.muc.sendMessage(newMessage);	
			Constants.log.addMsg("Sending XMPP: -DELETE- " + filename,2);	
		}catch(XMPPException xe){
			Constants.log.addMsg("Couldn't send XMPP message: " + newMessage.toXML() + "\n" + xe,4);
		}
	}
	
	/**
	* This sends update-file information to other participants
	*
	* @param filename The filename of the updated file
	* @param vers The fileversion after the update
	* @param size The filesize of the updated file
	* @param list A list of the blocks that changed with this update (only IDs)
	* @param hash The new SHA256 value of the file
	*/
	public void sendMUCUpdateFile(String filename, int vers, long size, LinkedList<Integer> list, byte[] hash){
		if(!this.joinedAChannel){
			Constants.log.addMsg("Sorry, cannot send message, we are not connected to a room!",4);
			return;
		}
		Message newMessage = this.createMessageObject();
		
		/*
		* Set message properties
		*/
		newMessage.setProperty("Type",3);
		newMessage.setProperty("JID",Constants.getJID());
		newMessage.setProperty("IP",Constants.ipAddress);
		newMessage.setProperty("name",filename);
		newMessage.setProperty("version",vers);
		newMessage.setProperty("size",size);
		newMessage.setProperty("blocks",list);
		newMessage.setProperty("sha256",hash);
		
		try{
			this.muc.sendMessage(newMessage);
			Constants.log.addMsg("Sending XMPP: -UPDATE- " + filename + " - Version " + vers + " - " + size + "Bytes - " 
				+ FileHandle.toHexHash(hash),2);
		}catch(XMPPException xe){
			Constants.log.addMsg("Couldn't send XMPP message: " + newMessage.toXML() + "\n" + xe,4);
		}
	}
	
	/**
	* This sends completed-file information to other participants
	*
	* @param filename The filename of the completed file
	* @param vers The fileversion of the completed file
	* @param size The filesize of the completed file
	* @param hash The new SHA256 value of the file
	*/
	public void sendMUCCompletedFile(String filename, int vers, int size, String hash){
		if(!this.joinedAChannel){
			Constants.log.addMsg("Sorry, cannot send message, we are not connected to a room!",4);
			return;
		}
		Message newMessage = this.createMessageObject();
		
		/*
		* Set message properties
		*/
		newMessage.setProperty("Type",4);
		newMessage.setProperty("JID",Constants.getJID());
		newMessage.setProperty("IP",Constants.ipAddress);
		newMessage.setProperty("name",filename);
		newMessage.setProperty("version",vers);
		newMessage.setProperty("size",size);
		newMessage.setProperty("sha256",hash);
		
		try{
			this.muc.sendMessage(newMessage);
			Constants.log.addMsg("Sending XMPP: -COMPLETED- " + filename + " - Version " + vers + " - " + size + "Bytes - " + hash,2);	
		}catch(XMPPException xe){
			Constants.log.addMsg("Couldn't send XMPP message: " + newMessage.toXML() + "\n" + xe,4);
		}
	}
	
}
