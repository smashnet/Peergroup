/*
* Peergroup - Storage.java
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

import java.util.LinkedList;
import java.util.List;
import java.io.*;
import name.pachler.nio.file.*;

/**
 *
 * @author Nicolas Inden
 */
public class Storage {
    
    private long shareLimit;
	private String rootDirectory;
	private String tmpDirectory;
	private WatchService watcher;
	private int fileListVersion;
	private LinkedList<FileHandle> files;
	
	public Storage(){
		this.fileListVersion = 0;
		this.shareLimit = Constants.shareLimit;
		this.rootDirectory = Constants.rootDirectory;
		this.tmpDirectory = Constants.tmpDirectory;
		this.files = new LinkedList<FileHandle>();
		
		File root = new File(this.rootDirectory);
		root.mkdirs();
		
		this.watcher = FileSystems.getDefault().newWatchService();
		Path path = Paths.get(this.rootDirectory);
		
		WatchKey key = null;
		try {
		    key = path.register(this.watcher, StandardWatchEventKind.ENTRY_CREATE, 
				StandardWatchEventKind.ENTRY_DELETE, StandardWatchEventKind.ENTRY_MODIFY);
		} catch (UnsupportedOperationException uox){
		    System.err.println("file watching not supported!");
		    // handle this error here
		}catch (IOException iox){
		    System.err.println("I/O errors");
		    // handle this error here
		}
	}
	
	/**
	* Adds a local file to the storage list
	*
	* @param filename the filename
	*/
	public void addFileFromLocal(String filename){
		FileHandle newFile = new FileHandle(this.rootDirectory + filename);
		this.files.add(newFile);
		this.fileListVersion++;
	}
	
	/**
	* Adds a remote file to the storage list
	*
	* @param filename the filename
	* @param vers the fileversion
	* @param fileHash the SHA-256 value of the file
	* @param fileSize the size of the file in bytes
	* @param chunks the list of chunks for this file
	*/
	public void addFileFromXMPP(String filename, int vers, byte[] fileHash, long fileSize, LinkedList<FileChunk> chunks){
		FileHandle newFile = new FileHandle(this.rootDirectory + filename,vers,fileHash,fileSize,chunks);
		this.files.add(newFile);
		this.fileListVersion++;
	}
	
	/**
	* Removes a file from the storage list
	*
	* @param filename the filename
	*/
	public void removeFile(String filename){
		
		this.fileListVersion++;
	}
	
	/**
	* Adds a local file to the storage list
	*
	* @param filename the filename
	*/
	public void modifyFile(){
		
		this.fileListVersion++;
	}
	
	public int getVersion(){
		return this.fileListVersion;
	}
	
	public void setVersion(int v){
		this.version = v;
	}
	
	public long getShareLimit(){
		return this.shareLimit;
	}
	
	public void setShareLimit(long s){
		this.shareLimit = s;
	}
    
	public String getDocumentRootDirectory(){
		return this.rootDirectory;
	}
	
	public WatchService getWatcher(){
		return this.watcher;
	}
	
	public LinkedList<FileHandle> getFileList(){
		return this.files;
	}
	
	public void setFileList(LinkedList<FileHandle> newList){
		this.files = newList;
	}
}
