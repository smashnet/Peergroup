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
 * This thread is listening for file system activities and
 * enqueues events in the global queue.
 *
 * @author Nicolas Inden
 */
public class StorageWorker extends Thread {
	
	private WatchService watcher;
	
	/**
	* Creates a StorageWorker.
	*/
	public StorageWorker(){
	}
	
	public void stopStorageWorker(){
		try{
			this.watcher.close();
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
		
		//Init WatchService
		this.watcher = FileSystems.getDefault().newWatchService();
		Path path = Paths.get(Constants.rootDirectory);
		
		WatchKey key = null;
		try {
		    key = path.register(this.watcher, StandardWatchEventKind.ENTRY_CREATE, 
				StandardWatchEventKind.ENTRY_DELETE, StandardWatchEventKind.ENTRY_MODIFY);
		} catch (UnsupportedOperationException uox){
		    System.err.println("file watching not supported!");
		    // handle this error here
		}catch (IOException iox){
		    System.err.println("I/O errors");
		    // handle this error here
		}
		
		
		while(!isInterrupted()){
		    // take() will block until a file has been created/deleted
		    WatchKey signalledKey;
		    try {
		        signalledKey = this.watcher.take();
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
					Constants.requestQueue.offer(new Request(Constants.LOCAL_ENTRY_CREATE,context.toString()));
				} else if(e.kind() == StandardWatchEventKind.ENTRY_DELETE){
					Path context = (Path)e.context();
					Constants.requestQueue.offer(new Request(Constants.LOCAL_ENTRY_DELETE,context.toString()));
				} else if(e.kind() == StandardWatchEventKind.ENTRY_MODIFY){
					Path context = (Path)e.context();
					Constants.requestQueue.offer(new Request(Constants.LOCAL_ENTRY_MODIFY,context.toString()));
				} else if(e.kind() == StandardWatchEventKind.OVERFLOW){
					Constants.log.addMsg("OVERFLOW: more changes happened than we could retreive",4);
				}
			}
		}
		Constants.log.addMsg("Storage thread interrupted. Closing...",4);
	}
	
	private void registerNewPath(String newPath){
		Path path = Paths.get(newPath);
		
		WatchKey key = null;
		try {
		    key = path.register(this.watcher, StandardWatchEventKind.ENTRY_CREATE, 
				StandardWatchEventKind.ENTRY_DELETE, StandardWatchEventKind.ENTRY_MODIFY);
		} catch (UnsupportedOperationException uox){
		    System.err.println("file watching not supported!");
		    // handle this error here
		}catch (IOException iox){
		    System.err.println("I/O errors");
		    // handle this error here
		}
	}
}
