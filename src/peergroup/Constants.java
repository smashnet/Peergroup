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
	* Request queue for thread communication
	*/
    public static ConcurrentLinkedQueue<Request> requestQueue = new ConcurrentLinkedQueue<Request>();
	
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
    public static long shareLimit = 2097152;                //2GB in Bytes
}
