/*
* Peergroup - ThriftClientGetFileList.java
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
public class ThriftClientGetFileList extends Thread {
	
	private int vers;
	private P2Pdevice node;
	
	public ThriftClientGetFileList(int vers, P2Pdevice node){
		this.vers = vers;
		this.node = node;
	}
	
	/**
	* The run() method
	*/
	public void run(){
		Constants.log.addMsg("DOWNLOAD_CURRENT_FILE_LIST: Version " + vers + " from " + node.getJID());
		ThriftStorage newStorage = node.getFileList();
		LinkedList<FileHandle> newFiles = new LinkedList<FileHandle>();
		for(ThriftFileHandle fh: newStorage.getFiles()){
			LinkedList<FileChunk> chunks = new LinkedList<FileChunk>();
			
			for(ThriftFileChunk fc : fh.getChunks()){
				LinkedList<P2Pdevice> devices = new LinkedList<P2Pdevice>();
				
				for(ThriftP2PDevice dev : fc.getDevices()){
					P2Pdevice newDev = new P2Pdevice(dev.getJid(),dev.getIp(),dev.getPort());
					devices.add(newDev);
				}
				FileChunk newChunk = new FileChunk(fh.getFilename(),fc.getChunkID(),fc.getSize(),
											fc.getBlockVersion(),fc.getHash(),devices,false); // Don't forget to change when merging
				chunks.add(newChunk);
			}
			try{
				FileHandle newHandle = new FileHandle(fh.getFilename(),fh.getFileVersion(),fh.getSize(),fh.getHash(),fh.getChunkSize(),chunks);
				newFiles.add(newHandle);
			}catch(Exception e){
				Constants.log.addMsg("Error creating FileHandle for " + fh.getFilename() + " while receiving FileList",2);
			}	
		}
		Storage.getInstance().mergeWithRemoteStorage(newStorage.getVersion(),newFiles);
	}
}
