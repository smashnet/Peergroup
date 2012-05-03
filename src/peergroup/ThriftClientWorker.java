/*
* Peergroup - ThriftClientWorker.java
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
import java.nio.ByteBuffer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.*;

/**
 * This thread requests blocks or FileList information from other peers.
 *
 * @author Nicolas Inden
 */
public class ThriftClientWorker extends Thread {
	
	private int id;
	
	public ThriftClientWorker(){
		
	}
	
	/**
	* Creates a ThriftClientWorker.
	*/
	public ThriftClientWorker(int newID){
		this.id = newID;
	}
	
	public void stopThriftClientWorker(){
		this.interrupt();
	}
	
	/**
	* The run() method
	*/
	public void run(){
		this.setName("Thriftclient " + this.id);
		
		/*
		* Main loop, takes requests from the queue and processes them
		*/
		while(!isInterrupted()){
			try{
				/*Request nextRequest = Constants.downloadQueue.take();
				switch(nextRequest.getID()){
					case Constants.DOWNLOAD_BLOCK:
						Constants.log.addMsg("DOWNLOAD_BLOCK: " + ((DLRequest)nextRequest).getName() + " - Block " 
							+ ((DLRequest)nextRequest).getBlockID());
						handleDownloadBlock((DLRequest)nextRequest);
						break;
					default:
					
				}*/
				FileChunk tmp;
				if((tmp = Storage.getInstance().getRarestChunk()) != null){
					Constants.log.addMsg("DOWNLOAD_BLOCK: " + tmp.getName() + " - Block " 
										+ tmp.getID());
					handleDownloadFileChunk(tmp);
				}else{
					Thread.sleep(1000);
				}
			}catch(InterruptedException ie){
				interrupt();
			}
		}
		
		Constants.log.addMsg("Thrift-Client-Thread interrupted/finished. Closing...",4);
	}
	
	private void handleDownloadFileChunk(FileChunk chunk){
		FileHandle tmp;
		if((tmp = Storage.getInstance().getFileHandle(chunk.getName())) == null){
			return;
		}else{
			P2Pdevice device = chunk.getRandomPeer();
			byte[] swap = getBlock(chunk.getName(),chunk.getID(),chunk.getHexHash(),device);
			//Constants.log.addMsg("Downloaded block " + request.getBlockID() + " - " + request.getName(),2);
			tmp.setChunkData(chunk.getID(),chunk.getHexHash(),device,swap);
			tmp.setChunkVersion(chunk.getID(),chunk.getVersion()+1);
			Network.getInstance().sendMUCCompletedChunk(chunk.getName(),chunk.getID(),chunk.getVersion());
			if(tmp.isComplete()){
				Constants.log.addMsg("Completed download: " + chunk.getName() + " - Version " + chunk.getVersion(),2);
				tmp.trimFile();
				tmp.setUpdating(false);
				//Network.getInstance().sendMUCCompletedFile(request.getName(),request.getVersion());
			}
		}
	}
	
	private void handleDownloadBlock(DLRequest request){
		FileHandle tmp;
		if((tmp = Storage.getInstance().getFileHandle(request.getName())) == null){
			return;
		}else{
			byte[] swap = getBlock(request.getName(),request.getBlockID(),request.getHash(),request.getNode());
			//Constants.log.addMsg("Downloaded block " + request.getBlockID() + " - " + request.getName(),2);
			tmp.setChunkData(request.getBlockID(),request.getHash(),request.getNode(),swap);
			tmp.setChunkVersion(request.getBlockID(),request.getVersion());
			Network.getInstance().sendMUCCompletedChunk(request.getName(),request.getBlockID(),request.getVersion());
			if(tmp.isComplete()){
				Constants.log.addMsg("Completed download: " + request.getName() + " - Version " + request.getVersion(),2);
				tmp.trimFile();
				tmp.setUpdating(false);
				//Network.getInstance().sendMUCCompletedFile(request.getName(),request.getVersion());
			}
		}
	}
	
	private byte[] getBlock(String name, int id, String hash, P2Pdevice node){
		return node.getDataBlock(name,id,hash);
	}
	
	public void stopThriftWorker(){
		for(P2Pdevice d : Constants.p2pDevices){
			d.closeTransport();
		}
		this.interrupt();
	}
	
}
