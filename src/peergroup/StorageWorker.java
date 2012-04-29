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

import java.util.*;
import java.util.concurrent.*;
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
		this.watcher = FileSystems.getDefault().newWatchService();
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
	* The run() method uses the WatchService to monitor our share
	* directory for changes. Any kind of changes (create/delete/modify)
	* are packed into a request and are enqueued to be processed by
	* the main thread.
	*/
	public void run(){
		this.setName("Storage Thread");
		Constants.log.addMsg("Storage thread started...");
		String os = System.getProperty("os.name");
		
		//Init WatchService
		
		Path path = Paths.get(Constants.rootDirectory);
		
		WatchKey key = null;
		try {
		    key = path.register(this.watcher, StandardWatchEventKind.ENTRY_CREATE, 
				StandardWatchEventKind.ENTRY_DELETE, StandardWatchEventKind.ENTRY_MODIFY);
		}catch(UnsupportedOperationException uox){
		    Constants.log.addMsg("No file-watching supported! Exiting...",1);
			System.exit(2);
		}catch(IOException iox){
			Constants.log.addMsg("Error accessing device for file-watching! Exiting...",1);
			System.exit(2);
		}catch(ClosedWatchServiceException cwse){
			Constants.log.addMsg("WatchService was closed! Exiting...",1);
			interrupt();
		}
		
		while(!isInterrupted()){
		    WatchKey signalledKey;
		    try {
				//here we are waiting for fs activities
		        signalledKey = this.watcher.take(); 
		    }catch(InterruptedException ix){
		        interrupt();
		        break;
		    }catch(ClosedWatchServiceException cwse){
		        interrupt();
		        break;
		    }

		    // get list of events from key
		    List<WatchEvent<?>> list = signalledKey.pollEvents();

		    // VERY IMPORTANT! call reset() AFTER pollEvents() to allow the
		    // key to be reported again by the watch service
		    signalledKey.reset();

		    for(WatchEvent e : list){
				if(e.kind() == StandardWatchEventKind.ENTRY_CREATE){
					Path context = (Path)e.context();
					if(Constants.enableModQueue){
						insertElement(Constants.modifyQueue,new ModifyEvent(Constants.LOCAL_ENTRY_CREATE,context.toString()));
					}else{
						Constants.requestQueue.offer(new FSRequest(Constants.LOCAL_ENTRY_CREATE,context.toString()));
					}
				} else if(e.kind() == StandardWatchEventKind.ENTRY_DELETE){
					Path context = (Path)e.context();
					Constants.requestQueue.offer(new FSRequest(Constants.LOCAL_ENTRY_DELETE,context.toString()));
				} else if(e.kind() == StandardWatchEventKind.ENTRY_MODIFY){
					Path context = (Path)e.context();
					/*
					* Linux and Windows support instant events on file changes. Copying a big file into the share folder
					* will result in one "create" event and loooots of "modify" events. So we will handle this here to
					* reduce update events to one per file. The ModifyQueueWorker checks the modifyQueue regularily
					* if there are files that haven't got modified in the last second, these are then enqueued in the
					* request queue.
					*/
					if(Constants.enableModQueue){
						insertElement(Constants.modifyQueue,new ModifyEvent(context.toString()));						
					}else{
						Constants.requestQueue.offer(new FSRequest(Constants.LOCAL_ENTRY_MODIFY,context.toString()));
					}
				} else if(e.kind() == StandardWatchEventKind.OVERFLOW){
					Constants.log.addMsg("OVERFLOW: more changes happened than we could retreive",4);
				}
			}
		}
		Constants.log.addMsg("Storage thread interrupted. Closing...",4);
	}
	
	private void insertElement(ConcurrentLinkedQueue<ModifyEvent> list, ModifyEvent me){
		for(ModifyEvent e : list){
			if(e.getName().equals(me.getName())){
				e.setTime(me.getTime());
				return;
			}
		}
		list.add(me);
	}
	
	/**
	* Registers a new (additional) path at the WatchService
	* (Not in use yet)
	*
	* @param newPath the new path relative to the share directory to be registered for watching changes
	*/
	private void registerNewPath(String newPath){
		Path path = Paths.get(Constants.rootDirectory + newPath);
		
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