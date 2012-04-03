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
	
	/*
	* Request queue where StorageWorker and NetworkWorker push their
	* requests to, which are then processed by the MainWorker.
	*/
	public static LinkedBlockingQueue<Request> requestQueue = new LinkedBlockingQueue<Request>();
	
	/*
	* Use extra queue for modify events to prevent modify-flooding on large files
	* under Windows and Linux
	*/
	public static ConcurrentLinkedQueue<ModifyEvent> modifyQueue = new ConcurrentLinkedQueue<ModifyEvent>();
	
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
	
	public final static int REMOTE_ENTRY_CREATE = 20;
	public final static int REMOTE_ENTRY_DELETE = 21;
	public final static int REMOTE_ENTRY_MODIFY = 22;
	public final static int REMOTE_ENTRY_COMPLETE = 23;
		
	public final static int STH_EVIL_HAPPENED = 666;
	
	/*
	* Stuff
	*/
	public static String ipAddress = "";
	public static int p2pPort = 43334;
	public static boolean caughtSignal = false;
	
	public static String getJID(){
		return user + "@" + server + "/" + resource;
	}
}
