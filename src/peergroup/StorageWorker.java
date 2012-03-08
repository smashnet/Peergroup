/*
* Peergroup - StorageWorkers.java
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
import java.io.*;
import name.pachler.nio.file.*;

/**
 * This is the thread running all local storage related things.
 *
 * @author Nicolas Inden
 */
public class StorageWorker extends Thread {
	
	private volatile Storage myStorage;
	
	/**
	* Creates a StorageWorker.
	*/
	public StorageWorker(){
	}
	
	public void stopStorageWorker(){
		try{
			this.myStorage.getWatcher().close();
			this.interrupt();
			}catch(IOException ioe){
				this.interrupt();
				Constants.log.addMsg("Error: " + ioe,4);
			}
	}
	
	/**
	* The run() method
	*/
	public void run(){
		Constants.log.addMsg("Storage thread started...",2);
		this.myStorage = new Storage();
		
		while(!isInterrupted()){
		    // take() will block until a file has been created/deleted
		    WatchKey signalledKey;
		    try {
		        signalledKey = this.myStorage.getWatcher().take();
		    } catch (InterruptedException ix){
		        interrupt();
		        break;
		    } catch (ClosedWatchServiceException cwse){
		        interrupt();
		        break;
		    }

		    // get list of events from key
		    List<WatchEvent<?>> list = signalledKey.pollEvents();

		    // VERY IMPORTANT! call reset() AFTER pollEvents() to allow the
		    // key to be reported again by the watch service
		    signalledKey.reset();

		    // we'll simply print what has happened; real applications
		    // will do something more sensible here
		    for(WatchEvent e : list){
		        String message = "";
		        if(e.kind() == StandardWatchEventKind.ENTRY_CREATE){
		            Path context = (Path)e.context();
		            message = context.toString() + " created";
		        } else if(e.kind() == StandardWatchEventKind.ENTRY_DELETE){
		            Path context = (Path)e.context();
		            message = context.toString() + " deleted";
		        } else if(e.kind() == StandardWatchEventKind.ENTRY_MODIFY){
		            Path context = (Path)e.context();
		            message = context.toString() + " modified";
		        } else if(e.kind() == StandardWatchEventKind.OVERFLOW){
		            message = "OVERFLOW: more changes happened than we could retreive";
		        }
		        System.out.println(message);
		    }
		}
		Constants.log.addMsg("Storage thread interrupted. Closing...",4);
	}
}
