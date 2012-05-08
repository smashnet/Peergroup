/*
* Peergroup - ThriftClientGetData.java
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
public class ThriftClientGetData implements Runnable {
	
	private FileChunk chunk;
	
	public ThriftClientGetData(FileChunk chunk){
		this.chunk = chunk;
	}
	
	/**
	* The run() method
	*/
	public void run(){
		FileHandle tmp;
		if((tmp = Storage.getInstance().getFileHandle(chunk.getName())) == null){
			return;
		}else{
			P2Pdevice device = chunk.getRandomPeer();
			Constants.log.addMsg("DOWNLOAD_BLOCK: " + chunk.getName() + " - Block " 
								+ chunk.getID() + " from " + device.getIP() + ":" + device.getPort());
			byte[] swap = getBlock(chunk.getName(),chunk.getID(),chunk.getHexHash(),device);
			Constants.storeQueue.offer(new StoreBlock(tmp,chunk.getID(),chunk.getHexHash(),device,swap));
		}
	}
	
	private byte[] getBlock(String name, int id, String hash, P2Pdevice node){
		return node.getDataBlock(name,id,hash);
	}	
}
