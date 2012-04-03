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

import java.util.*;
import java.io.*;
import name.pachler.nio.file.*;
import org.jivesoftware.smack.packet.*;

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
				Constants.requestQueue.offer(new FSRequest(Constants.LOCAL_ENTRY_CREATE,newFile.getName()));
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
						handleLocalEntryCreate((FSRequest)nextRequest);
						break;
					case Constants.LOCAL_ENTRY_DELETE:
						handleLocalEntryDelete((FSRequest)nextRequest);
						break;
					case Constants.LOCAL_ENTRY_MODIFY:
						handleLocalEntryModify((FSRequest)nextRequest);
						break;
					case Constants.REMOTE_ENTRY_CREATE:
						handleRemoteEntryCreate((XMPPRequest)nextRequest);
						break;
					case Constants.REMOTE_ENTRY_DELETE:
						handleRemoteEntryDelete((XMPPRequest)nextRequest);
						break;
					case Constants.REMOTE_ENTRY_MODIFY:
						handleRemoteEntryModify((XMPPRequest)nextRequest);
						break;
					case Constants.REMOTE_ENTRY_COMPLETE:
						handleRemoteEntryComplete((XMPPRequest)nextRequest);
						break;
					case Constants.STH_EVIL_HAPPENED:
						handleEvilEvents((FSRequest)nextRequest);
					default:
				}
			}catch(InterruptedException ie){
				interrupt();
			}			
		}
		Constants.log.addMsg("Main thread interrupted. Closing...",4);
	}
	
	/**
	* This one is invoked, if something reeeaallly evil happened. The program is shut down.
	*
	* @param request The request containing error information
	*/
	private void handleEvilEvents(FSRequest request){
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
	* Add new local file to file-list and propagate via XMPP
	*
	* @param request The request containing the new filename
	*/
	private void handleLocalEntryCreate(FSRequest request){
		if(myStorage.fileExists(request.getContent())){
			return;
		}
		FileHandle newFile = this.myStorage.addFileFromLocal(request.getContent());
		if(newFile != null)
			this.myNetwork.sendMUCNewFile(newFile.getPath(),newFile.getSize(),newFile.getByteHash());
	}
	
	/**
	* Removes file from file-list and propagates deletion via XMPP
	*
	* @param request The request containing the filename of the deleted file
	*/
	private void handleLocalEntryDelete(FSRequest request){
		this.myStorage.removeFile(request.getContent());
		this.myNetwork.sendMUCDeleteFile(request.getContent());
	}
	
	/**
	* Checks a local file for changes and modifies its FileHandle appropriately.
	* Afterwards the change is published via XMPP.
	*
	* @param request The request containing the filename of the changed file
	*/
	private void handleLocalEntryModify(FSRequest request){
		FileHandle newFile = this.myStorage.modifyFileFromLocal(request.getContent());
		if(newFile != null){
			this.myNetwork.sendMUCUpdateFile(newFile.getPath(),newFile.getVersion(),
				newFile.getSize(),newFile.getUpdatedBlocks(),newFile.getByteHash());
			newFile.clearUpdatedBlocks();
		}
	}
	
	/**
	* Process a new remotely created file
	*
	* @param request The request containing the XMPP Message object, including its properties
	*/
	private void handleRemoteEntryCreate(XMPPRequest request){
		/*
		* Someone announced a new file via XMPP
		* Available information:
		* "JID","IP","name","size","sha256"
		*/
		
		Message in = request.getContent();
		
		String jid 	= (String)in.getProperty("JID");
		String ip 	= (String)in.getProperty("IP");
		String name = (String)in.getProperty("name");
		long size 	= ((Long)in.getProperty("size")).longValue();
		byte[] hash = (byte[])in.getProperty("sha256");
		
		// TODO: Change linked list and cSize!
		myStorage.xmppNewFile(name,hash,size,new LinkedList<FileChunk>(), 0);
	}
	
	/**
	* Process a remotely deleted file
	*
	* @param request The request containing the XMPP Message object, including its properties
	*/
	private void handleRemoteEntryDelete(XMPPRequest request){
		/*
		* Someone announced a delete via XMPP
		* Available information:
		* "JID","name"
		*/
		
		Message in = request.getContent();
	}
	
	/**
	* Process a remotely modified file
	*
	* @param request The request containing the XMPP Message object, including its properties
	*/
	private void handleRemoteEntryModify(XMPPRequest request){
		/*
		* Someone announced a fileupdate via XMPP
		* Available information:
		* "JID","IP","name","version","size","blocks","sha256"
		*/
		
		Message in = request.getContent();
	}
	
	/**
	* Note that a remote node completed the download of a file. This especially means,
	* that this node has all recent blocks available for upload.
	*
	* @param request The request containing the XMPP Message object, including its properties
	*/
	private void handleRemoteEntryComplete(XMPPRequest request){
		/*
		* Someone announced that a file download is completed
		* Available information:
		* "JID","IP","name","version","size","sha256"
		*/
		
		Message in = request.getContent();
	}
}
