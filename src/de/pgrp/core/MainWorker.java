/*
 * Peergroup - MainWorker.java
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

import de.pgrp.thrift.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import org.jivesoftware.smack.packet.*;

/**
 * The MainWorker processes the requests enqueued by the StorageWorker and the
 * NetworkWorker in the requestQueue.
 * 
 * @author Nicolas Inden
 */
public class MainWorker extends Thread {

	private Storage myStorage;
	private Network myNetwork;

	/**
	 * Creates a MainWorker.
	 */
	public MainWorker() {
		this.myStorage = Storage.getInstance();
		this.myNetwork = Network.getInstance();
	}

	/**
	 * The run() method
	 */
	public void run() {
		this.setName("Main Thread");
		Constants.log.addMsg("Main thread started...");

		/*
		 * Main loop, takes requests from the queue and processes them
		 */
		while (!isInterrupted()) {
			try {
				Request nextRequest = Constants.requestQueue.take();
				switch (nextRequest.getID()) {
				case Constants.LOCAL_FILE_CREATE:
					Constants.log
					.addMsg("MainWorker: Handling LOCAL_FILE_CREATE");
					handleLocalFileCreate((FSRequest) nextRequest);
					break;
				case Constants.LOCAL_DIR_CREATE:
					Constants.log
					.addMsg("MainWorker: Handling LOCAL_DIR_CREATE");
					handleLocalDirCreate((FSRequest) nextRequest);
					break;
				case Constants.LOCAL_FILE_DELETE:
					Constants.log
					.addMsg("MainWorker: Handling LOCAL_FILE_DELETE");
					handleLocalFileDelete((FSRequest) nextRequest);
					break;
				case Constants.LOCAL_DIR_DELETE:
					Constants.log
					.addMsg("MainWorker: Handling LOCAL_DIR_DELETE");
					handleLocalDirDelete((FSRequest) nextRequest);
					break;
				case Constants.LOCAL_FILE_MODIFY:
					Constants.log
					.addMsg("MainWorker: Handling LOCAL_FILE_MODIFY");
					handleLocalFileModify((FSRequest) nextRequest);
					break;
				case Constants.REMOTE_FILE_CREATE:
					Constants.log
					.addMsg("MainWorker: Handling REMOTE_FILE_CREATE");
					handleRemoteFileCreate((XMPPRequest) nextRequest);
					break;
				case Constants.REMOTE_DIR_CREATE:
					Constants.log
					.addMsg("MainWorker: Handling REMOTE_DIR_CREATE");
					handleRemoteDirCreate((XMPPRequest) nextRequest);
					break;
				case Constants.REMOTE_ITEM_DELETE:
					Constants.log
					.addMsg("MainWorker: Handling REMOTE_ITEM_DELETE");
					handleRemoteItemDelete((XMPPRequest) nextRequest);
					break;
				case Constants.REMOTE_DIR_DELETE:
					Constants.log
					.addMsg("MainWorker: Handling REMOTE_DIR_DELETE");
					handleRemoteDirDelete((XMPPRequest) nextRequest);
					break;
				case Constants.REMOTE_FILE_MODIFY:
					Constants.log
					.addMsg("MainWorker: Handling REMOTE_FILE_MODIFY");
					handleRemoteFileModify((XMPPRequest) nextRequest);
					break;
				case Constants.REMOTE_CHUNK_COMPLETE:
					// Constants.log.addMsg("MainWorker: Handling REMOTE_CHUNK_COMPLETE");
					handleRemoteChunkComplete((XMPPRequest) nextRequest);
					break;
				case Constants.REMOTE_FILE_COMPLETE:
					Constants.log
					.addMsg("MainWorker: Handling REMOTE_FILE_COMPLETE");
					handleRemoteFileComplete((XMPPRequest) nextRequest);
					break;
				case Constants.REMOTE_JOINED_CHANNEL:
					Constants.log
					.addMsg("MainWorker: Handling REMOTE_JOINED_CHANNEL");
					handleRemoteJoinedChannel((XMPPRequest) nextRequest);
					break;
				case Constants.START_THREADS:
					handleStartThreads();
					break;
				case Constants.LOCAL_FILE_INITSCAN:
					Constants.log
					.addMsg("MainWorker: Handling LOCAL_FILE_INITSCAN");
					handleLocalFileInitScan((FSRequest) nextRequest);
					break;
				case Constants.STH_EVIL_HAPPENED:
					handleEvilEvents((FSRequest) nextRequest);
				default:
				}
			} catch (InterruptedException ie) {
				interrupt();
			}
		}
		Constants.log.addMsg("Main thread interrupted. Closing...", 4);
	}

	/**
	 * Add new local file to file-list and propagate via XMPP
	 * 
	 * @param request
	 *            The request containing the new filename
	 */
	private void handleLocalFileCreate(FSRequest request) {
		if (myStorage.fileExists(request.getContent()) != null) {
			Constants.log.addMsg("MainWorker: File already exists, ignoring!",
					4);
			return;
		}
		FileHandle newFile = this.myStorage.newFileFromLocal(request
				.getContent());
		if (newFile != null)
			this.myNetwork.sendMUCNewFile(newFile.getPath(), newFile.getSize(),
					newFile.getByteHash(), newFile.getBlockIDwithHash());
	}

	/**
	 * Check new directory for included files. If directory contains files, pipe
	 * them to handleLocalFileCreate If directory emtpy -> send createDir via
	 * XMPP
	 * 
	 * @param request
	 *            The request containing the directory name
	 */
	private void handleLocalDirCreate(FSRequest request) {
		this.myNetwork.sendMUCNewDir(request.getContent());
	}

	/**
	 * Removes file from file-list and propagates deletion via XMPP
	 * 
	 * @param request
	 *            The request containing the filename of the deleted file
	 */
	private void handleLocalFileDelete(FSRequest request) {
		// Only handle existing files
		FileHandle tmp;
		if ((tmp = myStorage.fileExists(request.getContent())) == null) {
			Constants.log
			.addMsg("Cannot delete file: File does not exist in datastructure.");
			return;
		}
		// Only handle files that are currently stable
		if (tmp.isUpdating()) {
			Constants.log
			.addMsg("Cannot delete file: File is currently updating.");
			return;
		}

		this.myStorage.removeFile(request.getContent());
		this.myNetwork.sendMUCDeleteItem(request.getContent(), false);
	}

	/**
	 * Delete directory and all contained files/directories and send via XMPP
	 * 
	 * @param request
	 *            The request containing the deleted directory
	 */
	private void handleLocalDirDelete(FSRequest request) {
		LinkedList<String> deletedItems = myStorage
				.deletedLocalFolderAndSubs(request.getContent());
		for (String item : deletedItems) {
			this.myNetwork.sendMUCDeleteItem(item, false);
		}
		this.myNetwork.sendMUCDeleteItem(request.getContent(), true);
	}

	/**
	 * Checks a local file for changes and modifies its FileHandle
	 * appropriately. Afterwards the change is published via XMPP.
	 * 
	 * String format: "id:version:hash:size"
	 * 
	 * @param request
	 *            The request containing the filename of the changed file
	 */
	private void handleLocalFileModify(FSRequest request) {
		if (myStorage.fileExists(request.getContent()) == null) {
			handleLocalFileCreate(request);
			return;
		}
		FileHandle newFile = this.myStorage.modifyFileFromLocal(request
				.getContent());
		if (newFile != null) {
			LinkedList<Integer> updated = newFile.getUpdatedBlocks();
			LinkedList<String> updatedWithHash = new LinkedList<String>();
			for (Integer i : updated) {
				String tmp = "";
				tmp += i.intValue() + ":";
				tmp += newFile.getVersion() + ":";
				tmp += newFile.getChunkHash(i.intValue()) + ":";
				tmp += newFile.getChunkSize(i.intValue());
				updatedWithHash.add(tmp);
			}
			// Only send update, if updated blocks available
			if (updatedWithHash.size() > 0) {
				this.myNetwork.sendMUCUpdateFile(newFile.getPath(),
						newFile.getVersion(), newFile.getSize(),
						updatedWithHash, newFile.getByteHash(),
						newFile.getNoOfChunks());
			}

			newFile.clearUpdatedBlocks();
		}
	}

	/**
	 * Process a new remotely created file
	 * 
	 * @param request
	 *            The request containing the XMPP Message object, including its
	 *            properties
	 */
	private void handleRemoteFileCreate(XMPPRequest request) {
		/*
		 * Someone announced a new file via XMPP Available information:
		 * "JID","IP","name","size","blocks","sha256"
		 */

		Message in = request.getContent();

		String jid = (String) in.getProperty("JID");
		String ip = (String) in.getProperty("IP");
		int port = ((Integer) in.getProperty("Port")).intValue();
		String name = (String) in.getProperty("name");
		long size = ((Long) in.getProperty("size")).longValue();
		LinkedList<String> blocks = (LinkedList<String>) in
				.getProperty("blocks");
		byte[] hash = (byte[]) in.getProperty("sha256");

		P2Pdevice remoteNode = P2Pdevice.getDevice(jid, ip, port);

		myStorage.newFileFromXMPP(name, hash, size, blocks,
				Constants.chunkSize, remoteNode);
		Network.getInstance().sendMUCmessage(
				"Start downloading >> " + name + " (" + size + "Bytes) <<");
	}

	/**
	 * Create empty directory received via XMPP
	 * 
	 * @param request
	 *            The name of the directory to be created
	 */
	private void handleRemoteDirCreate(XMPPRequest request) {
		/*
		 * Someone announced a directory via XMPP Available information:
		 * "JID","name"
		 */

		Message in = request.getContent();

		String jid = (String) in.getProperty("JID");
		String dirname = (String) in.getProperty("name");

		myStorage.newDirFromXMPP(dirname);
	}

	/**
	 * Process a remotely deleted item
	 * 
	 * @param request
	 *            The request containing the XMPP Message object, including its
	 *            properties
	 */
	private void handleRemoteItemDelete(XMPPRequest request) {
		/*
		 * Someone announced a delete via XMPP Available information:
		 * "JID","name"
		 */

		Message in = request.getContent();
		Network.getInstance().sendMUCmessage(
				"Deleting >> " + (String) in.getProperty("name") + " <<");
		myStorage.remoteRemoveItem((String) in.getProperty("name"));
	}

	/**
	 * Delete the directory (and including files) received via XMPP
	 * 
	 * @param request
	 *            The name of the directory to be deleted
	 */
	private void handleRemoteDirDelete(XMPPRequest request) {
		// TODO
	}

	/**
	 * Process a remotely modified file
	 * 
	 * @param request
	 *            The request containing the XMPP Message object, including its
	 *            properties
	 */
	private void handleRemoteFileModify(XMPPRequest request) {
		/*
		 * Someone announced a fileupdate via XMPP Available information:
		 * "JID","IP","name","version","size","blocks","sha256"
		 */

		Message in = request.getContent();

		String jid = (String) in.getProperty("JID");
		String ip = (String) in.getProperty("IP");
		int port = ((Integer) in.getProperty("Port")).intValue();
		String name = (String) in.getProperty("name");
		int vers = ((Integer) in.getProperty("version")).intValue();
		long size = ((Long) in.getProperty("size")).longValue();
		LinkedList<String> blocks = (LinkedList<String>) in
				.getProperty("blocks");
		byte[] hash = (byte[]) in.getProperty("sha256");
		int noOfChunks = ((Integer) in.getProperty("noOfChunks")).intValue();

		P2Pdevice remoteNode = P2Pdevice.getDevice(jid, ip, port);

		myStorage.modifiedFileFromXMPP(name, vers, size, blocks, hash,
				noOfChunks, remoteNode);
		Network.getInstance().sendMUCmessage(
				"Updating >> " + name + " (" + size + "Bytes) <<");
	}

	private void handleRemoteChunkComplete(XMPPRequest request) {
		// Available: "JID","IP","Port","name","chunkID","chunkVers"
		Message in = request.getContent();

		String jid = (String) in.getProperty("JID");
		String ip = (String) in.getProperty("IP");
		int port = ((Integer) in.getProperty("Port")).intValue();
		String name = (String) in.getProperty("name");
		int chunkID = ((Integer) in.getProperty("chunkID")).intValue();
		int chunkVers = ((Integer) in.getProperty("chunkVers")).intValue();

		P2Pdevice remoteNode = P2Pdevice.getDevice(jid, ip, port);

		myStorage.addP2PdeviceToBlock(name, chunkID, remoteNode);
	}

	/**
	 * Note that a remote node completed the download of a file. This especially
	 * means, that this node has all recent blocks available for upload.
	 * 
	 * @param request
	 *            The request containing the XMPP Message object, including its
	 *            properties
	 */
	private void handleRemoteFileComplete(XMPPRequest request) {
		// Available: "JID","IP","Port","name","version"
		Message in = request.getContent();

		String jid = (String) in.getProperty("JID");
		String ip = (String) in.getProperty("IP");
		int port = ((Integer) in.getProperty("Port")).intValue();
		String name = (String) in.getProperty("name");
		int vers = ((Integer) in.getProperty("version")).intValue();

		P2Pdevice remoteNode = P2Pdevice.getDevice(jid, ip, port);

		myStorage.addP2PdeviceToFile(name, vers, remoteNode);
	}

	private void handleRemoteJoinedChannel(XMPPRequest request) {
		// Available: "JID"
		Message in = request.getContent();

		String jid = (String) in.getProperty("JID");

		Network.getInstance().sendMUCFileListVersion();
	}

	private void handleStartThreads() {
		if (!Constants.serverMode)
			Constants.storage = new StorageWorker();

		Constants.network = new NetworkWorker();
		Constants.thrift = new ThriftServerWorker();
		Constants.thriftClient = new ThriftClientWorker();

		if (!Constants.serverMode)
			Constants.storage.start();

		Constants.network.start();
		Constants.thrift.start();
		Constants.thriftClient.start();

		if (Constants.enableModQueue) {
			Constants.modQueue = new DelayQueueWorker();
			Constants.modQueue.start();
		}

		try {
			Constants.myBarrier.await();
		} catch (InterruptedException ie) {

		} catch (BrokenBarrierException bbe) {
			Constants.log.addMsg(bbe.toString(), 4);
		}
	}

	/**
	 * Add new local file to file-list and propagate via XMPP
	 * 
	 * @param request
	 *            The request containing the new filename
	 */
	private void handleLocalFileInitScan(FSRequest request) {
		String newEntry = StorageWorker.getPurePath(request.getContent());
		if (myStorage.fileExists(newEntry) != null) {
			Constants.log.addMsg("MainWorker: File already exists, ignoring!",
					4);
			return;
		}
		this.myStorage.newFileFromLocal(newEntry);
	}

	/**
	 * This one is invoked, if something reeeaallly evil happened. The program
	 * is shut down.
	 * 
	 * @param request
	 *            The request containing error information
	 */
	private void handleEvilEvents(FSRequest request) {
		Constants.log.addMsg(
				"Something evil happened: " + request.getContent(), 1);

		if (Constants.storage != null)
			Constants.storage.stopStorageWorker();
		if (Constants.network != null)
			Constants.network.stopNetworkWorker();
		if (Constants.thriftClient != null)
			Constants.thriftClient.stopPoolExecutor();
		if (Constants.enableModQueue) {
			if (Constants.modQueue != null)
				Constants.modQueue.interrupt();
		}
		if (Constants.main != null)
			Constants.main.interrupt();

		Peergroup.quit(666);
	}
}
