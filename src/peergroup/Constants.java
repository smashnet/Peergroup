/*
* Peergroup - Constants.java
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
	* Running threads
	*/
	public static MainWorker main;
	public static StorageWorker storage;
	public static NetworkWorker network;
    
	/*
	* Storage constants
	*/
	public static String rootDirectory = "./share/";
    public static String tmpDirectory = "./tmp/";
    public static long shareLimit = 2048;                //MegaBytes
	
	/*
	* XMPP information
	*/
	public static String user = "";
	public static String server = "";
	public static int port = 5222;
	public static String conference_channel = "";
	public static String conference_server = "";
	
	/*
	* Constants defining request-types
	*/
	public final static int LOCAL_ENTRY_CREATE = 10;
	public final static int LOCAL_ENTRY_DELETE = 11;
	public final static int LOCAL_ENTRY_MODIFY = 12;
}
