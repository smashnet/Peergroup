/*
* Peergroup - NetworkWorker.java
* 
* This file is part of Peergroup.
*
* Peergroup is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Peergroup is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* Copyright (c) 2012 Nicolas Inden
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
		this.myNetwork.sendMUCleave();
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
		Constants.log.addMsg("Networking thread started...");
		this.myNetwork = Network.getInstance();
		int listsReceived = 0;
		int maxListVersion = -1;
		String ip;
		int vers, port;
		P2Pdevice maxListNode = new P2Pdevice();
		myNetwork.sendMUCjoin();
		
		while(!isInterrupted()){
			// read next message from XMPP
			Message newMessage = this.myNetwork.getNextMessage();
			
			// catch message stanzas announcing a new channel subject
			if(newMessage.getSubject() != null){
				Constants.log.addMsg("Subject: " + newMessage.getSubject(),2);
				continue;
			}
						
			// messages with body are not from peergroup clients and are only displayed
			if(newMessage.getBody() != null){
				String from[] = newMessage.getFrom().split("/");
				if(from.length > 1){
					Constants.log.addMsg(from[1] + ": " + newMessage.getBody(),2);
				}else{
					Constants.log.addMsg(newMessage.getFrom() + ": " + newMessage.getBody(),2);
				}
				
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
			String filename, jid;
			
			switch(type){
				case 1: 
					/*
					* Someone announced a new file via XMPP
					* Available information:
					* "JID","IP","Port","name","size","blocks","sha256"
					*/
						
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("New file discovered via XMPP: " + filename);
					Constants.requestQueue.offer(new XMPPRequest(Constants.REMOTE_ENTRY_CREATE,newMessage));
					break;
				case 2: 
					/*
					* Someone announced a delete via XMPP
					* Available information:
					* "JID","name"
					*/
					
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("File deletion discovered via XMPP: " + filename);
					//Constants.remoteAffectedItems.add(filename);
					Constants.requestQueue.offer(new XMPPRequest(Constants.REMOTE_ENTRY_DELETE,newMessage));
					break;
				case 3: 
					/*
					* Someone announced a fileupdate via XMPP
					* Available information:
					* "JID","IP","Port","name","version","size","blocks","sha256"
					*/
					
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("File update discovered via XMPP: " + filename);
					//Constants.remoteAffectedItems.add(filename);
					Constants.requestQueue.offer(new XMPPRequest(Constants.REMOTE_ENTRY_MODIFY,newMessage));
					break;
				case 4:
					/*
					* Someone announced that he completed the download of a chunk
					* Available information:
					* "JID","IP","Port","name","chunkID","chunkVers"
					*/
				
					filename = (String)newMessage.getProperty("name");
					//Constants.log.addMsg("Completed chunk download discovered via XMPP: " + filename + " Lamport: " + msgLamp);
					Constants.requestQueue.offer(new XMPPRequest(Constants.REMOTE_CHUNK_COMPLETE,newMessage));
					break;
				case 5: 
					/*
					* Someone announced that a file download is completed
					* Available information:
					* "JID","IP","Port","name","version"
					*/
					
					filename = (String)newMessage.getProperty("name");
					Constants.log.addMsg("Completed file download discovered via XMPP: " + filename);
					Constants.requestQueue.offer(new XMPPRequest(Constants.REMOTE_ENTRY_COMPLETE,newMessage));
					break;
				case 6:
					/*
					* Someone joined the channel
					* Available information:
					* "JID"
					*/
			
					jid = (String)newMessage.getProperty("JID");
					Constants.log.addMsg(jid + " joined the channel.");
					myNetwork.sendMUCFileListVersion();
					break;
				case 7:
					/* 
					* Someone posted his fileListVersion
					* Available information:
					* "JID","IP","Port","FileListVersion"
					*/
					
					jid = (String)newMessage.getProperty("JID");
					vers = ((Integer)newMessage.getProperty("FileListVersion")).intValue();
					ip = (String)newMessage.getProperty("IP");
					port = ((Integer)newMessage.getProperty("Port")).intValue();
					if(Constants.syncingFileList){
						if(vers > maxListVersion){
							maxListVersion = vers;
							maxListNode = new P2Pdevice(jid,ip,port);
						}else if(vers == -1){
							break;
						}
						listsReceived++;
						Constants.log.addMsg("Received file list version " + vers + " from " + jid);
						if(listsReceived == myNetwork.getUserCount()-1){
							Constants.log.addMsg("Found newest file list: " + maxListVersion + " from " + maxListNode.getJID());
							ThriftClientGetFileList getFileListThread = new ThriftClientGetFileList(maxListVersion,maxListNode);
							getFileListThread.start();
							listsReceived = 0;
							Constants.syncingFileList = false;
						}
					}
					break;
				case 8:
					// Someone left the channel (Available: "JID")
					// Inefficient!!
					jid = (String)newMessage.getProperty("JID");
					Constants.log.addMsg(jid + " left the channel.");
					for(FileHandle fh : Storage.getInstance().getFileList()){
						for(FileChunk fc : fh.getChunkList()){
							fc.deletePeer(jid);
						}
					}
					break;
				case 9:
					// Someone reannounced a file (came back online after incomplete upload)
					// Available: "JID","IP","Port","name","size","sha256"
					jid = (String)newMessage.getProperty("JID");
					filename = (String)newMessage.getProperty("name");
					ip = (String)newMessage.getProperty("IP");
					port = ((Integer)newMessage.getProperty("Port")).intValue();
					Constants.log.addMsg(jid + " reannounced: " + filename);
					FileHandle reannounced = Storage.getInstance().getFileHandle(filename);
					P2Pdevice reannouncer = P2Pdevice.getDevice(jid,ip,port);
					reannounced.addP2PdeviceToAllBlocks(reannouncer);
					if(!reannounced.isComplete()){
						LinkedList<FileChunk> incomplete = reannounced.getIncomplete();
						for(FileChunk fc : incomplete){
							fc.setDownloading(false);
							fc.setComplete(false);
						} 
					}
				default:
			}
		}
		
		Constants.log.addMsg("Networking thread interrupted. Closing...",4);
	}   
}
