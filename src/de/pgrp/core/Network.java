/*
 * Peergroup - Network.java
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
	 * The default constructor. It initializes the Connection object with the
	 * XMPP server supplied in the JID from the commandline args. Then it tries
	 * to establish a connection and logs in the user.
	 */
	private Network() {
		this.xmppCon = new XMPPConnection(Globals.server);
		this.lamportTime = 0;
		if (this.xmppConnect()) {
			this.xmppLogin();
		} else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {

			} finally {
				Globals.requestQueue.offer(new FSRequest(
						Globals.STH_EVIL_HAPPENED,
						"Coudln't create Network object"));
			}
		}
	}

	/**
	 * Network is a singleton, this returns the instance of Network (or creates
	 * one, if none exists)
	 */
	public static Network getInstance() {
		return instance;
	}

	/**
	 * Once the connection is successfully established, this logs in the user
	 * specified in the commandline arguments
	 */
	private void xmppLogin() {
		try {
			this.xmppCon.login(Globals.user, Globals.pass,
					Globals.resource);
			Globals.log.addMsg("Successfully logged into XMPP Server as: "
					+ Globals.user + "@" + Globals.server + "/"
					+ Globals.resource);
		} catch (XMPPException xe) {
			Globals.log.addMsg("Unable to log into XMPP Server: " + xe, 4);
		}
	}

	/**
	 * Connects to the XMPP Server the xmppCon object was initialized with
	 * 
	 * @return Returns true, if the connection was successfully established,
	 *         else false
	 */
	private boolean xmppConnect() {
		try {
			this.xmppCon.connect();
			Globals.log
			.addMsg("Successfully established connection to XMPP Server: "
					+ Globals.server);
			if (this.xmppCon.isSecureConnection()) {
				Globals.log.addMsg("XMPP connection is secure.", 2);
			} else {
				Globals.log.addMsg("XMPP connection is not secure.", 1);
			}
			return true;
		} catch (XMPPException xe) {
			Globals.log.addMsg("Failed connecting to XMPP Server: " + xe, 4);
			return false;
		}
	}

	/**
	 * Gets the Connection object from the Network instance
	 * 
	 * @return The Connection object managing the connection to the XMPP Server
	 */
	public Connection getConnection() {
		return Network.getInstance().xmppCon;
	}

	/**
	 * Returns if a connection to a server is established
	 * 
	 * @return true if connected, else false
	 */
	public boolean isConnected() {
		return this.xmppCon.isConnected();
	}

	/**
	 * Returns if successfully logged in using login()
	 * 
	 * @return true if logged in, else false
	 */
	public boolean isLoggedIn() {
		return this.xmppCon.isAuthenticated();
	}

	/**
	 * Joins the MuC room specified by the supplied information
	 * 
	 * @param user
	 *            The username you want to use in the room
	 * @param pass
	 *            The password necessary to join the corresponding room (not the
	 *            one for your JID)
	 * @param roomAndServer
	 *            The string representing the exact room to join (e.g.
	 *            foo@conference.bar.com)
	 */
	public void joinMUC(String user, String pass, String roomAndServer) {
		this.muc = new MultiUserChat(getConnection(), roomAndServer);
		DiscussionHistory history = new DiscussionHistory();
		history.setMaxStanzas(0);
		try {
			this.muc.join(user, pass, history,
					SmackConfiguration.getPacketReplyTimeout());
			this.joinedAChannel = true;
			Globals.log.addMsg("Successfully joined conference: "
					+ roomAndServer);
		} catch (XMPPException xe) {
			Globals.requestQueue.offer(new FSRequest(
					Globals.STH_EVIL_HAPPENED,
					"Unable to join conference channel: " + roomAndServer + " "
							+ xe));
			this.joinedAChannel = false;
		}
	}

	/**
	 * Sends a visible message in the currently joined MuC room
	 * 
	 * @param text
	 *            The text to be sent
	 */
	public void sendMUCmessage(String text) {
		if (!this.joinedAChannel || !this.xmppCon.isConnected()) {
			Globals.log
			.addMsg("Sorry, cannot send message, we are not connected to the server!",
					1);
			return;
		}
		Message newMessage = this.muc.createMessage();
		newMessage.setType(Message.Type.groupchat);

		newMessage.setBody(text);

		try {
			this.muc.sendMessage(newMessage);
		} catch (XMPPException xe) {
			Globals.log.addMsg(
					"Couldn't send XMPP message: " + newMessage.toXML() + "\n"
							+ xe, 4);
		}
	}

	/**
	 * Returns the next message received from the XMPP server. This blocks until
	 * a message is there.
	 * 
	 * --- Maybe we should do all message handling in this class, and only
	 * return a String? ---
	 * 
	 * @return the Message object
	 */
	public Message getNextMessage() {
		return this.muc.nextMessage();
	}

	/**
	 * Leave the current Multi-User-Chat room (recommended before quitting the
	 * program)
	 */
	public void leaveMUC() {
		if (!this.xmppCon.isConnected())
			return;
		this.muc.leave();
		Globals.log.addMsg("Left conference room: "
				+ Globals.conference_channel, 4);
	}

	/**
	 * Disconnect from the XMPP server (recommended before quitting the program)
	 */
	public void xmppDisconnect() {
		if (!this.xmppCon.isConnected())
			return;
		Network.getInstance().xmppCon.disconnect();
		Globals.log.addMsg("Disconnected from XMPP Server: "
				+ Globals.server, 4);
	}

	/**
	 * Creates a custom Message object with all always present properties
	 * 
	 * @return The Message object
	 */
	private Message createMessageObject() {
		incrementLamportTime();
		Message newMessage = this.muc.createMessage();
		newMessage.setType(Message.Type.groupchat);
		newMessage.setProperty("LamportTime", this.lamportTime);

		return newMessage;
	}

	public int getUserCount() {
		return this.muc.getOccupantsCount();
	}

	/**
	 * Sets the lamport clock to the given value
	 * 
	 * @param value
	 *            The value
	 */
	public void setLamportTime(long value) {
		this.lamportTime = value;
	}

	/**
	 * Updates the local lamport time: If the given value is greater-equal to
	 * the current local lamport time, the local lamport time is set to value+1.
	 * 
	 * @param value
	 *            The value
	 */
	public void updateLamportTime(long value) {
		if (value >= this.lamportTime) {
			this.lamportTime = value + 1;
		} else {
			this.lamportTime++;
		}
	}

	/**
	 * Increments the local lamport time by one
	 */
	public void incrementLamportTime() {
		this.lamportTime++;
	}

	/**
	 * Gets the current lamport time
	 * 
	 * @return The value
	 */
	public long getLamportTime() {
		return this.lamportTime;
	}

	/*
	 * -------- Primitives for sending and receiving XMPP packets ----------
	 * Type: 1: new file 2: delete file 3: update file 4: completed file 5: ask
	 * for current version (after connecting)
	 */

	/**
	 * This sends new-file information to other participants
	 * 
	 * @param filename
	 *            The filename of the new file
	 * @param size
	 *            The filesize of the new file
	 * @param hash
	 *            The new SHA256 value of the file
	 */
	public void sendMUCNewFile(String filename, long size, byte[] hash,
			LinkedList<String> list) {
		if (!this.joinedAChannel || !this.xmppCon.isConnected()) {
			Globals.log
			.addMsg("Sorry, cannot send message, we are not connected to a room!",
					4);
			return;
		}
		Message newMessage = this.createMessageObject();

		/*
		 * Set message properties
		 */
		newMessage.setProperty("Type", 1);
		newMessage.setProperty("JID", Globals.getJID());
		newMessage.setProperty("IP", Globals.ipAddress);
		newMessage.setProperty("Port", Globals.p2pPort);
		newMessage.setProperty("name", filename);
		newMessage.setProperty("size", size);
		newMessage.setProperty("sha256", hash);
		newMessage.setProperty("blocks", list);

		try {
			this.muc.sendMessage(newMessage);
			Globals.log.addMsg("Sending XMPP: -NEW_FILE- " + filename + " - "
					+ size + "Bytes - " + FileHandle.toHexHash(hash), 2);
		} catch (XMPPException xe) {
			Globals.log.addMsg(
					"Couldn't send XMPP message: " + newMessage.toXML() + "\n"
							+ xe, 4);
		}
	}

	/**
	 * This informs other peers to create a directory of the provided name
	 * 
	 * @param dir
	 *            The directory sub/sub1
	 */
	public void sendMUCNewDir(String dir) {
		if (!this.joinedAChannel || !this.xmppCon.isConnected()) {
			Globals.log
			.addMsg("Sorry, cannot send message, we are not connected to a room!",
					4);
			return;
		}
		Message newMessage = this.createMessageObject();

		newMessage.setProperty("Type", 10);
		newMessage.setProperty("JID", Globals.getJID());
		newMessage.setProperty("name", dir);

		try {
			this.muc.sendMessage(newMessage);
			Globals.log.addMsg("Sending XMPP: -NEW_DIR- " + dir, 2);
		} catch (XMPPException xe) {
			Globals.log.addMsg(
					"Couldn't send XMPP message: " + newMessage.toXML() + "\n"
							+ xe, 4);
		}
	}

	/**
	 * This sends delete-item information to other participants
	 * 
	 * @param filename
	 *            The name of the deleted item
	 */
	public void sendMUCDeleteItem(String filename, boolean dir) {
		if (!this.joinedAChannel || !this.xmppCon.isConnected()) {
			Globals.log
			.addMsg("Sorry, cannot send message, we are not connected to a room!",
					4);
			return;
		}
		Message newMessage = this.createMessageObject();

		/*
		 * Set message properties
		 */
		newMessage.setProperty("Type", 2);
		newMessage.setProperty("JID", Globals.getJID());
		newMessage.setProperty("name", filename);
		newMessage.setProperty("isDir", dir);

		try {
			this.muc.sendMessage(newMessage);
			Globals.log.addMsg("Sending XMPP: -DELETE- " + filename, 2);
		} catch (XMPPException xe) {
			Globals.log.addMsg(
					"Couldn't send XMPP message: " + newMessage.toXML() + "\n"
							+ xe, 4);
		}
	}

	/**
	 * This sends update-file information to other participants
	 * 
	 * @param filename
	 *            The filename of the updated file
	 * @param vers
	 *            The fileversion after the update
	 * @param size
	 *            The filesize of the updated file
	 * @param list
	 *            A list of the blocks that changed with this update (only IDs)
	 * @param hash
	 *            The new SHA256 value of the file
	 */
	public void sendMUCUpdateFile(String filename, int vers, long size,
			LinkedList<String> list, byte[] hash, int noOfChunks) {
		if (!this.joinedAChannel || !this.xmppCon.isConnected()) {
			Globals.log
			.addMsg("Sorry, cannot send message, we are not connected to a room!",
					4);
			return;
		}
		Message newMessage = this.createMessageObject();

		/*
		 * Set message properties
		 */
		newMessage.setProperty("Type", 3);
		newMessage.setProperty("JID", Globals.getJID());
		newMessage.setProperty("IP", Globals.ipAddress);
		newMessage.setProperty("Port", Globals.p2pPort);
		newMessage.setProperty("name", filename);
		newMessage.setProperty("version", vers);
		newMessage.setProperty("size", size);
		newMessage.setProperty("blocks", list);
		newMessage.setProperty("sha256", hash);
		newMessage.setProperty("noOfChunks", noOfChunks);

		try {
			this.muc.sendMessage(newMessage);
			Globals.log.addMsg("Sending XMPP: -UPDATE- " + filename
					+ " - Version " + vers + " - " + size + "Bytes - "
					+ FileHandle.toHexHash(hash), 2);
		} catch (XMPPException xe) {
			Globals.log.addMsg(
					"Couldn't send XMPP message: " + newMessage.toXML() + "\n"
							+ xe, 4);
		}
	}

	public void sendMUCCompletedChunk(String filename, int chunkID,
			int chunkVers) {
		if (!this.joinedAChannel || !this.xmppCon.isConnected()) {
			Globals.log
			.addMsg("Sorry, cannot send message, we are not connected to a room!",
					4);
			return;
		}
		Message newMessage = this.createMessageObject();

		/*
		 * Set message properties
		 */
		newMessage.setProperty("Type", 4);
		newMessage.setProperty("JID", Globals.getJID());
		newMessage.setProperty("IP", Globals.ipAddress);
		newMessage.setProperty("Port", Globals.p2pPort);
		newMessage.setProperty("name", filename);
		newMessage.setProperty("chunkID", chunkID);
		newMessage.setProperty("chunkVers", chunkVers);

		try {
			this.muc.sendMessage(newMessage);
			// Constants.log.addMsg("Sending XMPP: -CHUNK_COMPLETED- " +
			// filename + ": Chunk " + chunkID + " - Version "
			// + chunkVers,2);
		} catch (XMPPException xe) {
			Globals.log.addMsg(
					"Couldn't send XMPP message: " + newMessage.toXML() + "\n"
							+ xe, 4);
		}
	}

	/**
	 * This sends completed-file information to other participants
	 * 
	 * @param filename
	 *            The filename of the completed file
	 * @param vers
	 *            The fileversion of the completed file
	 * @param size
	 *            The filesize of the completed file
	 * @param hash
	 *            The new SHA256 value of the file
	 */
	public void sendMUCCompletedFile(String filename, int vers) {
		if (!this.joinedAChannel || !this.xmppCon.isConnected()) {
			Globals.log
			.addMsg("Sorry, cannot send message, we are not connected to a room!",
					4);
			return;
		}
		Message newMessage = this.createMessageObject();

		/*
		 * Set message properties
		 */
		newMessage.setProperty("Type", 5);
		newMessage.setProperty("JID", Globals.getJID());
		newMessage.setProperty("IP", Globals.ipAddress);
		newMessage.setProperty("Port", Globals.p2pPort);
		newMessage.setProperty("name", filename);
		newMessage.setProperty("version", vers);

		try {
			this.muc.sendMessage(newMessage);
			Globals.log.addMsg("Sending XMPP: -COMPLETED- " + filename
					+ " - Version " + vers, 2);
		} catch (XMPPException xe) {
			Globals.log.addMsg(
					"Couldn't send XMPP message: " + newMessage.toXML() + "\n"
							+ xe, 4);
		}
	}

	/**
	 * This requests others to post their file list version
	 */
	public void sendMUCjoin() {
		if (!this.joinedAChannel || !this.xmppCon.isConnected()) {
			Globals.log
			.addMsg("Sorry, cannot send message, we are not connected to a room!",
					4);
			return;
		}
		Message newMessage = this.createMessageObject();

		/*
		 * Set message properties
		 */
		newMessage.setProperty("Type", 6);
		newMessage.setProperty("JID", Globals.getJID());

		try {
			this.muc.sendMessage(newMessage);
			Globals.log.addMsg("Sending XMPP: -JOIN- ", 2);
			Thread.sleep(500); // Wait for presence messages
			if (this.getUserCount() > 1)
				Globals.syncingFileList = true;
		} catch (XMPPException xe) {
			Globals.log.addMsg(
					"Couldn't send XMPP message: " + newMessage.toXML() + "\n"
							+ xe, 4);
		} catch (InterruptedException ie) {
			Globals.log
			.addMsg("Wait for presence messages interrupted: User count may be inaccurate!",
					4);
		}
	}

	/**
	 * This posts your current file list version
	 */
	public void sendMUCFileListVersion() {
		if (!this.joinedAChannel || !this.xmppCon.isConnected()) {
			Globals.log
			.addMsg("Sorry, cannot send message, we are not connected to a room!",
					4);
			return;
		}
		Message newMessage = this.createMessageObject();

		/*
		 * Set message properties
		 */
		newMessage.setProperty("Type", 7);
		newMessage.setProperty("JID", Globals.getJID());
		newMessage.setProperty("IP", Globals.ipAddress);
		newMessage.setProperty("Port", Globals.p2pPort);
		newMessage.setProperty("FileListVersion", Storage.getInstance()
				.getVersion());

		try {
			this.muc.sendMessage(newMessage);
			Globals.log.addMsg("Sending XMPP: -SENDFILELIST- ", 2);
		} catch (XMPPException xe) {
			Globals.log.addMsg(
					"Couldn't send XMPP message: " + newMessage.toXML() + "\n"
							+ xe, 4);
		}
	}

	/**
	 * This requests others to post their file list version
	 */
	public void sendMUCleave() {
		if (!this.joinedAChannel || !this.xmppCon.isConnected()) {
			Globals.log
			.addMsg("Sorry, cannot send message, we are not connected to a room!",
					4);
			return;
		}
		Message newMessage = this.createMessageObject();

		/*
		 * Set message properties
		 */
		newMessage.setProperty("Type", 8);
		newMessage.setProperty("JID", Globals.getJID());

		try {
			this.muc.sendMessage(newMessage);
			Globals.log.addMsg("Sending XMPP: -LEAVE- ", 2);
			Globals.syncingFileList = true;
		} catch (XMPPException xe) {
			Globals.log.addMsg(
					"Couldn't send XMPP message: " + newMessage.toXML() + "\n"
							+ xe, 4);
		}
	}

	public void sendMUCReannounceFile(String filename, long size, byte[] hash) {
		if (!this.joinedAChannel || !this.xmppCon.isConnected()) {
			Globals.log
			.addMsg("Sorry, cannot send message, we are not connected to a room!",
					4);
			return;
		}
		Message newMessage = this.createMessageObject();

		/*
		 * Set message properties
		 */
		newMessage.setProperty("Type", 9);
		newMessage.setProperty("JID", Globals.getJID());
		newMessage.setProperty("IP", Globals.ipAddress);
		newMessage.setProperty("Port", Globals.p2pPort);
		newMessage.setProperty("name", filename);
		newMessage.setProperty("size", size);
		newMessage.setProperty("sha256", hash);

		try {
			this.muc.sendMessage(newMessage);
			Globals.log
			.addMsg("Sending XMPP: -REANNOUNCE- " + filename + " - "
					+ size + "Bytes - " + FileHandle.toHexHash(hash), 2);
		} catch (XMPPException xe) {
			Globals.log.addMsg(
					"Couldn't send XMPP message: " + newMessage.toXML() + "\n"
							+ xe, 4);
		}
	}

}
