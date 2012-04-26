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
				Request nextRequest = Constants.downloadQueue.take();
				switch(nextRequest.getID()){
					case Constants.DOWNLOAD_BLOCK:
						Constants.log.addMsg("Thrift: Handling DOWNLOAD_BLOCK");
						handleDownloadBlock((DLRequest)nextRequest);
						break;
					default:
					
				}
			}catch(InterruptedException ie){
				interrupt();
			}
		}
		
		Constants.log.addMsg("ThriftClient-Thread " + this.id + " interrupted/finished. Closing...",4);
	}
	
	private void handleDownloadBlock(DLRequest request){
		FileHandle tmp;
		if((tmp = Storage.getInstance().getFileHandle(request.getName())) == null){
			return;
		}else{
			byte[] swap = getBlock(request.getName(),request.getBlockID(),request.getHash(),request.getNode());
			Constants.log.addMsg("Downloaded block " + request.getBlockID() + " - " + request.getName(),2);
			tmp.setChunkData(request.getBlockID(),swap);
			tmp.setChunkVersion(request.getBlockID(),request.getVersion());
		}
	}
	
	private byte[] getBlock(String name, int id, String hash, P2Pdevice node){
		TTransport transport;
		try{
			transport = new TSocket(node.getIP(), node.getPort());
			TProtocol protocol = new TBinaryProtocol(transport);
			DataTransfer.Client client = new DataTransfer.Client(protocol);
			transport.open();
			
			ByteBuffer block = client.getDataBlock(name,id,hash);
			
			transport.close();
			
			return block.array();
		}catch(TTransportException e){
		
		}catch(TException e){
		
		}
		return null;
	}
	
	public void stopThriftWorker(){
		this.interrupt();
	}
	
}
