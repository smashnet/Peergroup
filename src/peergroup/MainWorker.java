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
import java.io.*;
import name.pachler.nio.file.*;

/**
 * This is the main thread.
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
		this.myNetwork = new Network();
	}
	
	/**
	* The run() method
	*/
	public void run(){
		Constants.log.addMsg("Main thread started...",2);
		
		//Do initial scan of shared directory
		Constants.log.addMsg("Doing initial scan of share directory...",2);
		File test = this.myStorage.getDirHandle();
		for(File newFile:test.listFiles()){
			if(newFile.isFile()){
				Constants.log.addMsg("Found: " + newFile.getName(),2);
				Constants.requestQueue.offer(new Request(Constants.LOCAL_ENTRY_CREATE,newFile.getName()));
			}
		}
		
		while(!isInterrupted()){
			try{
				Request nextRequest = Constants.requestQueue.take();
				System.out.println("Items on queue: " + Constants.requestQueue.size());
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
					default:
				}
			}catch(InterruptedException ie){
				interrupt();
			}			
		}
		Constants.log.addMsg("Main thread interrupted. Closing...",4);
	}
	
	private void handleLocalEntryCreate(Request request){
		this.myStorage.addFileFromLocal(request.getContent());
	}
	
	private void handleLocalEntryDelete(Request request){
		this.myStorage.removeFile(request.getContent());
	}
	
	private void handleLocalEntryModify(Request request){
		this.myStorage.modifyFileFromLocal(request.getContent());
	}
    
}