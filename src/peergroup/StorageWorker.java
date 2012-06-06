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
	private Map<WatchKey,Path> keys;
	
	/**
	* Creates a StorageWorker.
	*/
	public StorageWorker(){
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey,Path>();
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
		LinkedList<String> folders = new LinkedList<String>();
		
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
		    WatchKey signaledKey;
		    try {
				//here we are waiting for fs activities
		        signaledKey = this.watcher.take(); 
		    }catch(InterruptedException ix){
		        interrupt();
		        break;
		    }catch(ClosedWatchServiceException cwse){
		        interrupt();
		        break;
		    }

		    // get list of events from key
		    List<WatchEvent<?>> list = signaledKey.pollEvents();
			
			Path dir = keys.get(signaledKey);
			if(dir == null){
				System.out.println("WatchKey not recognized!!");
				continue;
			}
			
			//TODO: Complete Directory Watching!!

		    // VERY IMPORTANT! call reset() AFTER pollEvents() to allow the
		    // key to be reported again by the watch service
		    signaledKey.reset();

		    for(WatchEvent e : list){
				if(e.kind() == StandardWatchEventKind.ENTRY_CREATE){
					// Entry created
					Path context = (Path)e.context();
					// Ignore hidden files and directories
					if(context.toString().charAt(0) == '.'){
						continue;
					}
					File newEntry = new File(Constants.rootDirectory + context.toString());
					System.out.print("New: " + newEntry.getPath());
					if(newEntry.isFile()){
						System.out.println(" -- is a file!");
						if(Constants.enableModQueue){
							insertElement(Constants.modifyQueue,new ModifyEvent(Constants.LOCAL_ENTRY_CREATE,context.toString()));
						}else{
							Constants.requestQueue.offer(new FSRequest(Constants.LOCAL_ENTRY_CREATE,context.toString()));
						}
					}else if(newEntry.isDirectory()){
						System.out.println(" -- is a directory!");
						folders.add(newEntry.getPath());
						registerNewPath(newEntry.getPath());
						if(Constants.enableModQueue){
							insertElement(Constants.modifyQueue,new ModifyEvent(Constants.LOCAL_FOLDER_CREATE,context.toString()));
						}else{
							Constants.requestQueue.offer(new FSRequest(Constants.LOCAL_FOLDER_CREATE,context.toString()));
						}
					}
				} else if(e.kind() == StandardWatchEventKind.ENTRY_DELETE){
					// Entry deleted
					Path context = (Path)e.context();
					if(context.toString().charAt(0) == '.'){
						continue;
					}
					File delEntry = new File(Constants.rootDirectory + context.toString());
					System.out.println("Modified: " + delEntry.getPath());
					if(delEntry.isFile()){
						Constants.requestQueue.offer(new FSRequest(Constants.LOCAL_ENTRY_DELETE,context.toString()));
					}else if(delEntry.isDirectory()){
						Constants.requestQueue.offer(new FSRequest(Constants.LOCAL_FOLDER_DELETE,context.toString()));
					}
				} else if(e.kind() == StandardWatchEventKind.ENTRY_MODIFY){
					// Entry modified
					Path context = (Path)e.context();
					if(context.toString().charAt(0) == '.'){
						continue;
					}
					File modEntry = new File(Constants.rootDirectory + context.toString());
					System.out.println("Modified: " + modEntry.getPath());
					if(modEntry.isFile()){
						if(Constants.enableModQueue){
							insertElement(Constants.modifyQueue,new ModifyEvent(context.toString()));						
						}else{
							Constants.requestQueue.offer(new FSRequest(Constants.LOCAL_ENTRY_MODIFY,context.toString()));
						}
					}
				} else if(e.kind() == StandardWatchEventKind.OVERFLOW){
					Constants.log.addMsg("OVERFLOW: more changes happened than we could retrieve",4);
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
		Path path = Paths.get(newPath);
		
		WatchKey key = null;
		try {
		    key = path.register(this.watcher, StandardWatchEventKind.ENTRY_CREATE, 
				StandardWatchEventKind.ENTRY_DELETE, StandardWatchEventKind.ENTRY_MODIFY);
			Path prev = keys.get(key);
			if (prev == null) {
				System.out.format("register: %s\n", path);
			} else {
				if (!path.equals(prev)) {
					System.out.format("update: %s -> %s\n", prev, path);
				}
			}
			keys.put(key, path);
		} catch (UnsupportedOperationException uox){
		    System.err.println("file watching not supported!");
		    // handle this error here
		}catch (IOException iox){
		    System.err.println("I/O errors");
		    // handle this error here
		}
	}
}
