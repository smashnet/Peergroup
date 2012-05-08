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
		this.myStorage = Storage.getInstance();
		this.myNetwork = Network.getInstance();
	}
	
	/**
	* The run() method
	*/
	public void run(){
		this.setName("Main Thread");
		Constants.log.addMsg("Main thread started...");
		
		/*
		* Main loop, takes requests from the queue and processes them
		*/
		while(!isInterrupted()){
			try{
				Request nextRequest = Constants.requestQueue.take();
				switch(nextRequest.getID()){
					case Constants.LOCAL_ENTRY_CREATE:
						Constants.log.addMsg("MainWorker: Handling LOCAL_ENTRY_CREATE");
						handleLocalEntryCreate((FSRequest)nextRequest);
						Constants.log.addMsg(myStorage.toString());
						break;
					case Constants.LOCAL_ENTRY_DELETE:
						Constants.log.addMsg("MainWorker: Handling LOCAL_ENTRY_DELETE");
						handleLocalEntryDelete((FSRequest)nextRequest);
						Constants.log.addMsg(myStorage.toString());
						break;
					case Constants.LOCAL_ENTRY_MODIFY:
						Constants.log.addMsg("MainWorker: Handling LOCAL_ENTRY_MODIFY");
						handleLocalEntryModify((FSRequest)nextRequest);
						Constants.log.addMsg(myStorage.toString());
						break;
					case Constants.REMOTE_ENTRY_CREATE:
						Constants.log.addMsg("MainWorker: Handling REMOTE_ENTRY_CREATE");
						handleRemoteEntryCreate((XMPPRequest)nextRequest);
						Constants.log.addMsg(myStorage.toString());
						break;
					case Constants.REMOTE_ENTRY_DELETE:
						Constants.log.addMsg("MainWorker: Handling REMOTE_ENTRY_DELETE");
						handleRemoteEntryDelete((XMPPRequest)nextRequest);
						Constants.log.addMsg(myStorage.toString());
						break;
					case Constants.REMOTE_ENTRY_MODIFY:
						Constants.log.addMsg("MainWorker: Handling REMOTE_ENTRY_MODIFY");
						handleRemoteEntryModify((XMPPRequest)nextRequest);
						Constants.log.addMsg(myStorage.toString());
						break;
					case Constants.REMOTE_CHUNK_COMPLETE:
						//Constants.log.addMsg("MainWorker: Handling REMOTE_CHUNK_COMPLETE");
						handleRemoteChunkComplete((XMPPRequest)nextRequest);
						break;
					case Constants.REMOTE_ENTRY_COMPLETE:
						Constants.log.addMsg("MainWorker: Handling REMOTE_ENTRY_COMPLETE");
						handleRemoteEntryComplete((XMPPRequest)nextRequest);
						break;
					case Constants.REMOTE_JOINED_CHANNEL:
						Constants.log.addMsg("MainWorker: Handling REMOTE_JOINED_CHANNEL");
						handleRemoteJoinedChannel((XMPPRequest)nextRequest);
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
		Constants.thrift.stopThriftWorker();
		Constants.thriftClient.stopPoolExecutor();
		if(Constants.enableModQueue){
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
		if(myStorage.fileExists(request.getContent()) != null){
			Constants.log.addMsg("MainWorker: File already exists, ignoring!",4);
			return;
		}
		FileHandle newFile = this.myStorage.newFileFromLocal(request.getContent());
		if(newFile != null)
			this.myNetwork.sendMUCNewFile(newFile.getPath(),newFile.getSize(),newFile.getByteHash(),newFile.getBlockIDwithHash());
	}
	
	/**
	* Removes file from file-list and propagates deletion via XMPP
	*
	* @param request The request containing the filename of the deleted file
	*/
	private void handleLocalEntryDelete(FSRequest request){
		// Only apply local deletes
		/*for(int i = 0; i < Constants.remoteAffectedItems.size(); i++){
			if(Constants.remoteAffectedItems.get(i).equals(request.getContent())){
				Constants.remoteAffectedItems.remove(i);
				return;
			}
		}*/
		
		// Only handle existing files
		FileHandle tmp;
		if((tmp = myStorage.fileExists(request.getContent())) == null){
			Constants.log.addMsg("Cannot delete file: File does not exist in datastructure.");
			return;
		}
		// Only handle files that are currently stable
		if(tmp.isUpdating()){
			Constants.log.addMsg("Cannot delete file: File is currently updating.");
			return;
		}
		
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
			LinkedList<Integer> updated = newFile.getUpdatedBlocks();
			LinkedList<String> updatedWithHash = new LinkedList<String>();
			for(Integer i : updated){
				String tmp = "";
				tmp += i.intValue() + ":";
				tmp += newFile.getVersion() + ":";
				tmp += newFile.getChunkHash(i.intValue()) + ":";
				tmp += newFile.getChunkSize(i.intValue());
				updatedWithHash.add(tmp);
			}
			// Only send update, if updated blocks available
			if(updatedWithHash.size() > 0){
				this.myNetwork.sendMUCUpdateFile(newFile.getPath(),newFile.getVersion(),
					newFile.getSize(),updatedWithHash,newFile.getByteHash());
			}
			
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
		* "JID","IP","name","size","blocks","sha256"
		*/
		
		Message in = request.getContent();
		
		String jid 	= (String)in.getProperty("JID");
		String ip 	= (String)in.getProperty("IP");
		int port 	= ((Integer)in.getProperty("Port")).intValue();
		String name = (String)in.getProperty("name");
		long size 	= ((Long)in.getProperty("size")).longValue();
		LinkedList<String> blocks = (LinkedList<String>)in.getProperty("blocks");
		byte[] hash = (byte[])in.getProperty("sha256");
		
		P2Pdevice remoteNode = P2Pdevice.getDevice(jid,ip,port);
		
		myStorage.newFileFromXMPP(name,hash,size,blocks,Constants.chunkSize,remoteNode);
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
		myStorage.remoteRemoveFile((String)in.getProperty("name"));
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
		
		String jid 	= (String)in.getProperty("JID");
		String ip 	= (String)in.getProperty("IP");
		int port 	= ((Integer)in.getProperty("Port")).intValue();
		String name = (String)in.getProperty("name");
		int vers	= ((Integer)in.getProperty("version")).intValue();
		long size 	= ((Long)in.getProperty("size")).longValue();
		LinkedList<String> blocks = (LinkedList<String>)in.getProperty("blocks");
		byte[] hash = (byte[])in.getProperty("sha256");
		
		P2Pdevice remoteNode = P2Pdevice.getDevice(jid,ip,port);
		
		myStorage.modifiedFileFromXMPP(name, vers, size, blocks, hash, remoteNode);
	}
	
	private void handleRemoteChunkComplete(XMPPRequest request){
		//Available: "JID","IP","Port","name","chunkID","chunkVers"
		Message in = request.getContent();
		
		String jid 		= (String)in.getProperty("JID");
		String ip 		= (String)in.getProperty("IP");
		int port 		= ((Integer)in.getProperty("Port")).intValue();
		String name 	= (String)in.getProperty("name");
		int chunkID		= ((Integer)in.getProperty("chunkID")).intValue();
		int chunkVers	= ((Integer)in.getProperty("chunkVers")).intValue();
		
		P2Pdevice remoteNode = P2Pdevice.getDevice(jid,ip,port);
		
		myStorage.addP2PdeviceToBlock(name,chunkID,remoteNode);
	}
	
	/**
	* Note that a remote node completed the download of a file. This especially means,
	* that this node has all recent blocks available for upload.
	*
	* @param request The request containing the XMPP Message object, including its properties
	*/
	private void handleRemoteEntryComplete(XMPPRequest request){
		//Available: "JID","IP","Port","name","version"
		Message in = request.getContent();
		
		String jid 	= (String)in.getProperty("JID");
		String ip 	= (String)in.getProperty("IP");
		int port 	= ((Integer)in.getProperty("Port")).intValue();
		String name = (String)in.getProperty("name");
		int vers	= ((Integer)in.getProperty("version")).intValue();
		
		P2Pdevice remoteNode = P2Pdevice.getDevice(jid,ip,port);
		
		myStorage.addP2PdeviceToFile(name,vers,remoteNode);
	}
	
	private void handleRemoteJoinedChannel(XMPPRequest request){
		//Available: "JID"
		Message in = request.getContent();
		
		String jid = (String)in.getProperty("JID");
		
		Network.getInstance().sendMUCFileListVersion();
	}
}
