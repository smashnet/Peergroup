/*
* Peergroup - ThriftDataHandler.java
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
import org.apache.thrift.*;

/**
 * The ThriftDataHandler implements the DataTransfer interface
 * and defines how the getDataBlock and getFileList requests
 * are handled.
 *
 * @author Nicolas Inden
 */
public class ThriftDataHandler implements DataTransfer.Iface {
	
    public ThriftStorage getStorage() throws org.apache.thrift.TException{
		return toThriftStorage(Storage.getInstance());
	}

    public ByteBuffer getDataBlock(String filename, int blockID, String hash) throws org.apache.thrift.TException{
		FileHandle tmp;
		if((tmp = Storage.getInstance().getFileHandle(filename)) == null){
			return null;
		}else{
			byte[] swap = tmp.getChunkData(blockID);
			ByteBuffer buffer = ByteBuffer.wrap(swap);
			return buffer;
		}
	}
	
	/**
	* Converts a Storage object into a thrift-sendable ThriftStorage object
	*
	* @param localStorage the Storage object
	* @return the ThriftStorage object
	*/
	private static ThriftStorage toThriftStorage(Storage localStorage){
		LinkedList<ThriftFileHandle> newList = new LinkedList<ThriftFileHandle>();
		for(FileHandle f : localStorage.getFileList()){
			ThriftFileHandle newHandle = toThriftFileHandle(f);
			newList.add(newHandle);
		}
		ThriftStorage thriftStorage = new ThriftStorage(
														localStorage.getVersion(),
														newList
														);
		return thriftStorage;
	}
	
	private static ThriftFileHandle toThriftFileHandle(FileHandle localHandle){
		LinkedList<ThriftFileChunk> newList = new LinkedList<ThriftFileChunk>();
		for(FileChunk f : localHandle.getChunks()){
			ThriftFileChunk newChunk = toThriftFileChunk(f);
			newList.add(newChunk);
		}
		ThriftFileHandle thriftHandle = new ThriftFileHandle(
																localHandle.getPath(),
																localHandle.getVersion(),
																localHandle.getSize(),
																localHandle.getHexHash(),
																localHandle.getChunkSize(),
																newList
																);
		return thriftHandle;
	}
	
	private static ThriftFileChunk toThriftFileChunk(FileChunk localChunk){
		LinkedList<ThriftP2PDevice> newList = new LinkedList<ThriftP2PDevice>();
		for(P2Pdevice d : localChunk.getPeers()){
			ThriftP2PDevice newDev = toThriftP2PDevice(d);
			newList.add(newDev);
		}
		ThriftFileChunk thriftChunk = new ThriftFileChunk(
															localChunk.getID(),
															localChunk.getHexHash(),
															localChunk.getVersion(),
															newList
															);
		return thriftChunk;
	}
	 
	private static ThriftP2PDevice toThriftP2PDevice(P2Pdevice localDevice){
		return new ThriftP2PDevice(
									localDevice.getIP(),
									localDevice.getPort(),
									localDevice.getJID());
	}  
}
