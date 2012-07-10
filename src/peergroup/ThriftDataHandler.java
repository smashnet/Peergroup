/*
* Peergroup - ThriftDataHandler.java
* 
* This file is part of Peergroup.
*
* Peergroup is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Peergroup is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* Copyright (c) 2012 Nicolas Inden
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
			if(swap == null){
				return null;
			}
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
															localChunk.getVersion(),
															localChunk.getSize(),
															localChunk.getHexHash(),
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
