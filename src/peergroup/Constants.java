/*
* Peergroup - Constants.java
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

import java.util.concurrent.*;
import java.util.LinkedList;

/**
 * This class is the saving point for all globally used constants
 * and variables.
 * 
 * @author Nicolas Inden
 */
public class Constants {
    
	public final static String PROGNAME    = "peergroup";
	public final static String VERSION     = "0.01 (development version)";
    
	public final static Logger log = new Logger();
	
	public static LinkedList<P2Pdevice> p2pDevices = new LinkedList<P2Pdevice>();
	public static LinkedBlockingQueue<StoreBlock> storeQueue = new LinkedBlockingQueue<StoreBlock>();
	
	public static CyclicBarrier myBarrier = new CyclicBarrier(2);
	
	/*
	* Request queue where StorageWorker and NetworkWorker push their
	* requests to, which are then processed by the MainWorker.
	*/
	public static LinkedBlockingQueue<Request> requestQueue = new LinkedBlockingQueue<Request>();
	public static LinkedBlockingQueue<Request> downloadQueue = new LinkedBlockingQueue<Request>();
	
	/*
	* Linux and Windows support instant events on file changes. Copying a big file into the share folder
	* will result in one "create" event and loooots of "modify" events. So we will handle this here to
	* reduce update events to one per file. The ModifyQueueWorker checks the modifyQueue regularily
	* if there are files that haven't got modified in the last seconds, these are then enqueued in the
	* request queue.
	*/
	public static ConcurrentLinkedQueue<ModifyEvent> modifyQueue = new ConcurrentLinkedQueue<ModifyEvent>();
	
	/*
	* A list of files currently causing filesystem activity due to network updates
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
	public static ThriftClientBase thriftClient;
	public static ModifyQueueWorker modQueue;
	    
	/*
	* Storage constants
	*/
	public static String rootDirectory = "./share/";
    public static String tmpDirectory = "./tmp/";
    public static long shareLimit = 2048;                //MegaBytes
	
	/*
	* XMPP information
	*/
	public static String user = "test1";
	public static String pass = "test1";
	public static String resource = "peergroup";
	public static String server = "vmhost1";
	public static int port = 5222;
	public static String conference_channel = "peergroup";
	public static String conference_server = "localhost";
	
	/*
	* Constants defining request-types
	*/
	public final static int LOCAL_ENTRY_CREATE = 10;
	public final static int LOCAL_ENTRY_DELETE = 11;
	public final static int LOCAL_ENTRY_MODIFY = 12;
	public final static int LOCAL_ENTRY_INITSCAN = 13;
	
	public final static int LOCAL_FOLDER_CREATE = 14;
	public final static int LOCAL_FOLDER_DELETE = 15;
	public final static int LOCAL_FOLDER_MODIFY = 16;
	public final static int LOCAL_FOLDER_INITSCAN = 17;
	
	public final static int REMOTE_ENTRY_CREATE = 20;
	public final static int REMOTE_ENTRY_DELETE = 21;
	public final static int REMOTE_ENTRY_MODIFY = 22;
	public final static int REMOTE_ENTRY_COMPLETE = 23;
	
	public final static int REMOTE_FOLDER_CREATE = 24;
	public final static int REMOTE_FOLDER_DELETE = 25;
	public final static int REMOTE_FOLDER_MODIFY = 26;
	
	public final static int REMOTE_CHUNK_COMPLETE = 30;
	public final static int REMOTE_JOINED_CHANNEL = 31;
	public final static int REMOTE_FILE_LIST_VERSION = 32;
	
	public final static int DOWNLOAD_BLOCK = 40;
	
	public final static int START_THREADS = 300;
	public final static int STH_EVIL_HAPPENED = 666;
	
	/*
	* Stuff
	*/
	public static boolean enableModQueue = true;
	public static String ipAddress = "";
	public static int p2pPort = 43334;
	public static boolean caughtSignal = false;
	public static final int chunkSize = 512000;	//In bytes
	public static boolean syncingFileList = false;
	
	public static String getJID(){
		return user + "@" + server + "/" + resource;
	}
}
