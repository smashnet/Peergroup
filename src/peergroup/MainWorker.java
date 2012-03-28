/*
* Peergroup - MainWorker.java
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

import java.util.List;
import java.io.*;
import name.pachler.nio.file.*;

/**
 * The MainWorker processes the requests enqueued by the StorageWorker
 * and the NetworkWorker in the requestQueue.
 *
 * @author Nicolas Inden
 */
public class MainWorker extends Thread {
	
	private Storage myStorage;
	private Network myNetwork;
	
	/**
	* Creates a MainWorker.
	*/
	public MainWorker(){
		this.myStorage = new Storage();
		this.myNetwork = Network.getInstance();
	}
	
	/**
	* The run() method
	*/
	public void run(){
		Constants.log.addMsg("Main thread started...",2);
		
		//Do initial scan of share directory
		Constants.log.addMsg("Doing initial scan of share directory...",2);
		File test = this.myStorage.getDirHandle();
		for(File newFile : test.listFiles() ){
			if(newFile.isFile()){
				Constants.log.addMsg("Found: " + newFile.getName(),2);
				Constants.requestQueue.offer(new Request(Constants.LOCAL_ENTRY_CREATE,newFile.getName()));
			}
		}
		
		/*
		* Main loop, takes requests from the queue and processes them
		*/
		while(!isInterrupted()){
			try{
				Request nextRequest = Constants.requestQueue.take();
				switch(nextRequest.getID()){
					case Constants.LOCAL_ENTRY_CREATE:
						handleLocalEntryCreate(nextRequest);
						break;
					case Constants.LOCAL_ENTRY_DELETE:
						handleLocalEntryDelete(nextRequest);
						break;
					case Constants.LOCAL_ENTRY_MODIFY:
						handleLocalEntryModify(nextRequest);
						break;
					case Constants.STH_EVIL_HAPPENED:
						handleEvilEvents(nextRequest);
					default:
				}
			}catch(InterruptedException ie){
				interrupt();
			}			
		}
		Constants.log.addMsg("Main thread interrupted. Closing...",4);
	}
	
	private void handleEvilEvents(Request request){
		Constants.log.addMsg("Something evil happened: " + request.getContent(),1);
		
		Constants.storage.stopStorageWorker();
		Constants.network.stopNetworkWorker();
		if(System.getProperty("os.name").equals("Linux") 
			|| System.getProperty("os.name").equals("Windows")){
			Constants.modQueue.interrupt();
		}
		Constants.main.interrupt();
	}
	
	/**
	* Add new local file to file list and propagate via XMPP
	*/
	private void handleLocalEntryCreate(Request request){
		FileHandle newFile = this.myStorage.addFileFromLocal(request.getContent());
		if(newFile != null){
			this.myNetwork.sendMUCNewFile(newFile.getPath(),newFile.getSize(),newFile.getHexHash());
		}
	}
	
	private void handleLocalEntryDelete(Request request){
		this.myStorage.removeFile(request.getContent());
	}
	
	private void handleLocalEntryModify(Request request){
		this.myStorage.modifyFileFromLocal(request.getContent());
	}
    
}
