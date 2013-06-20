/*
 * Peergroup - Constants.java
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

import java.io.File;
import java.util.concurrent.*;
import java.util.LinkedList;
import java.util.Random;

import net.sbbi.upnp.impls.InternetGatewayDevice;

/**
 * This class is the saving point for all globally used constants and variables.
 * 
 * @author Nicolas Inden
 */
public class Globals {

	public final static String PROGNAME = "Peergroup";
	public final static String VERSION = "0.10dev";
	public static String hiddenDir = "./.pgrp/";
	public static String config = "config.xml";

	public final static Logger log = new Logger(false);

	public static LinkedList<P2Pdevice> p2pDevices = new LinkedList<P2Pdevice>();
	
	//Contains blocks that are waiting to be written do storage (received from other peers)
	public static LinkedBlockingQueue<StoreBlock> storeQueue = new LinkedBlockingQueue<StoreBlock>();

	public static CyclicBarrier bootupBarrier = new CyclicBarrier(2);
	public static CyclicBarrier inputBarrier = new CyclicBarrier(2);
	public static CyclicBarrier shutdownBarrier = new CyclicBarrier(2);

	/*
	 * Request queue where StorageWorker and NetworkWorker push their requests
	 * to, which are then processed by the MainWorker.
	 */
	public static LinkedBlockingQueue<Request> requestQueue = new LinkedBlockingQueue<Request>();
	
	/*
	 * This list contains information about all items that are currently downloaded
	 */
	public static LinkedList<DLULItem> downloadsList = new LinkedList<DLULItem>();
	/*
	 * This list contains information about all items that are currently uploaded
	 */
	public static LinkedList<DLULItem> uploadsList = new LinkedList<DLULItem>();

	/*
	 * Linux and Windows support instant events on file changes. Copying a big
	 * file into the share folder will result in one "create" event and loooots
	 * of "modify" events. So we will handle this here to reduce update events
	 * to one per file. The DelayQueueWorker checks the modifyQueue regularily
	 * if there are files that haven't got modified in the last seconds, these
	 * are then enqueued in the request queue.
	 */
	public static ConcurrentLinkedQueue<FileEvent> delayQueue = new ConcurrentLinkedQueue<FileEvent>();

	/*
	 * A list of files currently causing filesystem activity due to network
	 * updates
	 */
	public static volatile LinkedList<String> remoteAffectedItems = new LinkedList<String>();

	/**
	 * Global ID counters
	 */
	public static volatile int p2pCount = 0;

	/*
	 * Running threads
	 */
	public static MainWorker main;
	public static StorageWorker storage;
	public static NetworkWorker network;
	public static ThriftServerWorker thrift;
	public static ThriftClientWorker thriftClient;
	public static DelayQueueWorker modQueue;

	/*
	 * Storage constants
	 */
	public static String rootDirectory = "./Peergroup/";
	public static InternetGatewayDevice igd;

	/*
	 * XMPP information
	 */
	public static String user = "";
	public static String pass = "";
	public static String resource = "peergroup";
	public static String server = "";
	public static int port = 5222;
	public static String conference_channel = "";
	public static String conference_server = "";
	public static String conference_pass = "";

	/*
	 * Constants defining request-types
	 */
	public final static int LOCAL_FILE_CREATE = 10;
	public final static int LOCAL_FILE_DELETE = 11;
	public final static int LOCAL_FILE_MODIFY = 12;
	public final static int LOCAL_FILE_INITSCAN = 13;

	public final static int LOCAL_DIR_CREATE = 14;
	public final static int LOCAL_DIR_DELETE = 15;
	public final static int LOCAL_DIR_MODIFY = 16;
	public final static int LOCAL_DIR_INITSCAN = 17;

	public final static int REMOTE_FILE_CREATE = 20;
	public final static int REMOTE_ITEM_DELETE = 21;
	public final static int REMOTE_FILE_MODIFY = 22;
	public final static int REMOTE_FILE_COMPLETE = 23;

	public final static int REMOTE_DIR_CREATE = 24;
	public final static int REMOTE_DIR_DELETE = 25;
	public final static int REMOTE_DIR_MODIFY = 26;

	public final static int REMOTE_CHUNK_COMPLETE = 30;
	public final static int REMOTE_JOINED_CHANNEL = 31;
	public final static int REMOTE_FILE_LIST_VERSION = 32;

	public final static int DOWNLOAD_BLOCK = 40;

	public final static int START_THREADS = 300;
	public final static int STH_EVIL_HAPPENED = 666;

	/*
	 * Stuff
	 */
	public static boolean serverMode = false;
	public static boolean useGUI = false;
	public static boolean doUPnP = true;
	public static boolean enableModQueue = true;
	public static String remoteIP4 = "";
	public static String localIP4 = "";
	public static String remoteIP6 = "";
	public static int p2pPort = 50000 + new Random(System.currentTimeMillis()).nextInt(10000);
	public static int chunkSize = 512000; // In bytes
	public static boolean syncingFileList = false;
	public static LinkedList<String> folders;
	public static boolean quitting = false;
	public static String hashAlgo = "MD5";
	public static int guiRefreshRate = 1000; //In milliseconds

	public static String getJID() {
		return user + "@" + server + "/" + resource;
	}
	
	public static String getAbsoluteShareFolderPath(){
		File folder = new File(Globals.rootDirectory);
		return folder.getAbsolutePath();
	}
}
