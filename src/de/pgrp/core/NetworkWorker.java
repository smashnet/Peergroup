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
 * Copyright (c) 2013 Nicolas Inden
 */

package de.pgrp.core;

import java.util.*;
import org.jivesoftware.smack.packet.*;

/**
 * This thread listens for new information on the XMPP side and enqueues actions
 * in the request queue.
 * 
 * @author Nicolas Inden
 */
public class NetworkWorker extends Thread {

	private Network myNetwork;
	private boolean run;

	/**
	 * Creates a NetworkWorker.
	 */
	public NetworkWorker() {
	}

	public void stopNetworkWorker() {
		this.run = false;
		this.myNetwork.sendMUCleave();
		this.myNetwork.leaveMUC();
		this.myNetwork.xmppDisconnect();
		Globals.log.addMsg("Networking thread stopped. Closing...", 4);
	}

	/**
	 * The run() method
	 */
	@Override
	public void run() {
		this.setName("XMPP Thread");
		Globals.log.addMsg("Networking thread started...");
		this.myNetwork = Network.getInstance();
		this.run = true;
		int listsReceived = 0;
		int maxListVersion = -1;
		String filename, jid, remoteIP, localIP;
		int vers, port;
		P2Pdevice maxListNode = new P2Pdevice();
		myNetwork.sendMUCjoin();

		while (this.run) {
			
			Message newMessage = this.myNetwork.getNextMessage();
			if(newMessage == null)
				continue;

			// catch message stanzas announcing a new channel subject
			if (newMessage.getSubject() != null) {
				Globals.log.addMsg("Subject: " + newMessage.getSubject(), 2);
				continue;
			}

			// messages with body are not from peergroup clients and are only
			// displayed
			if (newMessage.getBody() != null) {
				String from[] = newMessage.getFrom().split("/");
				if (from.length > 1) {
					Globals.log.addMsg(from[1] + ": " + newMessage.getBody(), 2);
				} else {
					Globals.log.addMsg(newMessage.getFrom() + ": " + newMessage.getBody(), 2);
				}

				continue;
			}

			// adjust lamport time
			long msgLamp = ((Long) newMessage.getProperty("LamportTime")).longValue();
			this.myNetwork.updateLamportTime(msgLamp);

			// ignore messages sent by yourself
			if (newMessage.getProperty("JID").equals(Globals.getJID())) {
				continue;
			}
			
			//Maintain list of P2Pdevices
			jid = (String) newMessage.getProperty("JID");
			remoteIP = (String) newMessage.getProperty("remoteIP");
			localIP = (String) newMessage.getProperty("localIP");
			port = ((Integer) newMessage.getProperty("Port")).intValue();
			//This checks if this device is known, if not it is created
			P2Pdevice.getDevice(jid, remoteIP, localIP, port);

			// extract message type from message
			int type = ((Integer) newMessage.getProperty("Type")).intValue();

			switch (type) {
			case 1:
				/*
				 * Someone announced a new file via XMPP Available information:
				 * "JID","remoteIP","Port","name","size","blocks","sha256"
				 */

				filename = (String) newMessage.getProperty("name");
				Globals.log.addMsg("New file via XMPP: " + filename);
				Globals.requestQueue.offer(new XMPPRequest(Globals.REMOTE_FILE_CREATE, newMessage));
				break;
			case 10:
				/*
				 * Someone announced a new directory via XMPP Available
				 * information: "JID","name"
				 */

				filename = (String) newMessage.getProperty("name");
				Globals.log.addMsg("New directory via XMPP: " + filename);
				Globals.requestQueue.offer(new XMPPRequest(Globals.REMOTE_DIR_CREATE, newMessage));
				break;
			case 2:
				/*
				 * Someone announced a delete via XMPP Available information:
				 * "JID","name"
				 */

				filename = (String) newMessage.getProperty("name");
				Globals.log.addMsg("Deletion discovered via XMPP: "
						+ filename);
				// Constants.remoteAffectedItems.add(filename);
				Globals.requestQueue.offer(new XMPPRequest(Globals.REMOTE_ITEM_DELETE, newMessage));
				break;
			case 3:
				/*
				 * Someone announced a fileupdate via XMPP Available
				 * information:
				 * "JID","remoteIP","Port","name","version","size","blocks","sha256"
				 */

				filename = (String) newMessage.getProperty("name");
				Globals.log.addMsg("File update discovered via XMPP: " + filename);
				// Constants.remoteAffectedItems.add(filename);
				Globals.requestQueue.offer(new XMPPRequest(Globals.REMOTE_FILE_MODIFY, newMessage));
				break;
			case 4:
				/*
				 * Someone announced that he completed the download of a chunk
				 * Available information:
				 * "JID","remoteIP","Port","name","chunkID","chunkVers"
				 */

				filename = (String) newMessage.getProperty("name");
				Globals.requestQueue.offer(new XMPPRequest(Globals.REMOTE_CHUNK_COMPLETE, newMessage));
				break;
			case 5:
				/*
				 * Someone announced that a file download is completed Available
				 * information: "JID","remoteIP","Port","name","version"
				 */

				filename = (String) newMessage.getProperty("name");
				Globals.log.addMsg("Completed file download discovered via XMPP: " + filename);
				Globals.requestQueue.offer(new XMPPRequest(Globals.REMOTE_FILE_COMPLETE, newMessage));
				break;
			case 6:
				/*
				 * Someone joined the channel Available information: "JID"
				 */

				Globals.log.addMsg(jid + " joined the channel.");
				myNetwork.sendMUCFileListVersion();
				break;
			case 7:
				/*
				 * Someone posted his fileListVersion Available information:
				 * "JID","remoteIP","Port","FileListVersion"
				 */

				vers = ((Integer) newMessage.getProperty("FileListVersion")).intValue();
				if (Globals.syncingFileList) {
					if (vers > maxListVersion) {
						maxListVersion = vers;
						maxListNode = P2Pdevice.getDevice(jid, remoteIP, localIP, port);
					} else if (vers == -1) {
						break;
					}
					listsReceived++;
					Globals.log.addMsg("Received file list version " + vers + " from " + jid);
					if (listsReceived == myNetwork.getUserCount() - 1) {
						Globals.log.addMsg("Found newest file list: "
								+ maxListVersion + " from "
								+ maxListNode.getJID());
						ThriftClientGetFileList getFileListThread = new ThriftClientGetFileList(
								maxListVersion, maxListNode);
						getFileListThread.start();
						listsReceived = 0;
						Globals.syncingFileList = false;
					}
				}
				break;
			case 8:
				// Someone left the channel (Available: "JID")
				// Inefficient!!
				Globals.log.addMsg(jid + " left the channel.");
				for (FileHandle fh : Storage.getInstance().getFileList()) {
					for (FileChunk fc : fh.getChunkList()) {
						fc.deletePeer(jid);
					}
				}
				break;
			case 9:
				// Someone reannounced a file (came back online after incomplete
				// upload)
				// Available: "JID","remoteIP","Port","name","size","sha256"
				filename = (String) newMessage.getProperty("name");
				Globals.log.addMsg(jid + " reannounced: " + filename);
				FileHandle reannounced = Storage.getInstance().getFileHandle(filename);
				P2Pdevice reannouncer = P2Pdevice.getDevice(jid, remoteIP, localIP, port);
				reannounced.addP2PdeviceToAllBlocks(reannouncer);
				if (!reannounced.isComplete()) {
					LinkedList<FileChunk> incomplete = reannounced.getIncomplete();
					for (FileChunk fc : incomplete) {
						fc.setDownloading(false);
						fc.setComplete(false);
					}
				}
			default:
			}
		}

		Globals.log.addMsg("Networking thread interrupted. Closing...", 4);
	}
}
