/*
* Peergroup - MainWorker.java
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

import java.util.List;
import name.pachler.nio.file.*;

/**
 * This is the main thread.
 *
 * @author Nicolas Inden
 */
public class MainWorker extends Thread {
	
	/**
	* Creates a MainWorker.
	*/
	public MainWorker(){
	}
	
	/**
	* The run() method
	*/
	public void run(){
		Constants.log.addMsg("Main thread started...",2);
		while(!isInterrupted()){
			try{
				Thread.sleep(5000);
			}catch(InterruptedException ie){
				interrupt();
			}			
		}
		Constants.log.addMsg("Main thread interrupted. Closing...",4);
	}
    
}
