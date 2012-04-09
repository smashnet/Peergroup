/*
* Peergroup - Storage.java
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

import java.util.LinkedList;
import java.util.List;
import java.io.*;

/**
 * This class maintains local files, and provides information about the file list version etc.
 *
 * @author Nicolas Inden
 */
public class Storage {
    
	private static Storage instance = new Storage();
	private File sharedDir;
	private int fileListVersion;
	private LinkedList<FileHandle> files;
	
	public Storage(){
		this.fileListVersion = 0;
		this.files = new LinkedList<FileHandle>();
		this.sharedDir = new File(Constants.rootDirectory);
		this.sharedDir.mkdirs();
	}
	
	/**
	* Returns the existing singleton instance of Storage, or creates a new one
	*
	* @return The singleton instance of Storage
	*/
	public static Storage getInstance(){
		return instance;
	}
	
	/**
	* Adds a local file to the storage list
	*
	* @param filename the filename
	* @return The FileHandle added to the list
	*/
	public FileHandle addFileFromLocal(String filename){
		try{
			FileHandle newFile = new FileHandle(Constants.rootDirectory + filename);
			this.files.add(newFile);
			Constants.log.addMsg("Adding " + newFile.toString(),4);
			this.fileListVersion++;
			Constants.log.addMsg(this.toString(),4);
			return newFile;
		}catch(Exception ioe){
			Constants.log.addMsg("Local file does not exist anymore: " + ioe,4);
		}
		return null;
	}
	
	/**
	* Removes a file from the storage list
	*
	* @param file the filename+path (e.g. subdir/file.txt)
	*/
	public void removeFile(String file){
		FileHandle tmp;
		int i = 0;
		while(i < this.files.size()){
			if(this.files.get(i).getPath().equals(file)){
				this.files.remove(i);
				Constants.log.addMsg("Deleted " + file,4);
				break;
			}
			i++;
		}
		this.fileListVersion++;
		Constants.log.addMsg(this.toString(),4);
	}
	
	/**
	* Removes a file from the storage list and from local storage device
	*
	* @param file the filename+path (e.g. subdir/file.txt)
	*/
	public void remoteRemoveFile(String file){
		int i = 0;
		while(i < this.files.size()){
			if(this.files.get(i).getPath().equals(file)){
				this.files.get(i).getFile().delete();
				this.files.remove(i);
				Constants.log.addMsg("Deleted " + file,4);
				break;
			}
			i++;
		}
		this.fileListVersion++;
		Constants.log.addMsg(this.toString(),4);
	}
	
	/**
	* Applies a change to a file in the file list
	*
	*/
	public FileHandle modifyFileFromLocal(String file){
		FileHandle tmp;
		int i = 0;
		while(i < this.files.size()){
			if(this.files.get(i).getPath().equals(file)){
				try{
					tmp = this.files.get(i);
					tmp.localUpdate();
					Constants.log.addMsg("Updated " + this.files.get(i).getPath(),4);
					this.fileListVersion++;
					return tmp;
				}catch(Exception ioe){
					Constants.log.addMsg("Error updating file: " + ioe,1);
					break;
				}
			}
			i++;
		}
		Constants.log.addMsg("Locally modified file not found in file-list.",4);
		
		return null;
	}
	
	/**
	* Adds a remote file to the storage list
	*
	* @param filename the filename+path (e.g. subdir/file.txt)
	* @param fileHash the SHA-256 value of the file
	* @param fileSize the size of the file in bytes
	* @param chunks the list of chunks for this file
	*/
	public void xmppNewFile(String filename, byte[] fileHash, long fileSize, LinkedList<FileChunk> chunks, int cSize){
		try{
			FileHandle newFile = new FileHandle(filename,fileHash,fileSize,chunks,cSize);
			newFile.createEmptyLocalFile();
			this.files.add(newFile);
			this.fileListVersion++;
		}catch(Exception e){
			Constants.log.addMsg("Couldn't create FileHandle for new file from XMPP!",2);
		}
	}
	
	/**
	* Checks if a file already exists in the list
	*
	* @param filename The filename (without rootDirectory)
	* @return true if file exists, else false
	*/
	public boolean fileExists(String filename){
		for(FileHandle f : this.files){
			if(filename.equals(f.getPath())){
				return true;
			}
		}
		return false;
	}
	
	/**
	* This performs a merge of the locally found files after the initial scan
	* with the list of files received from network
	*
	* @param remoteStorage The Storage object received from the network
	*/
	public void mergeWithRemoteStorage(int remoteVersion, LinkedList<FileHandle> newList){
		// Update FileList version number
		if(remoteVersion > this.fileListVersion){
			this.fileListVersion = remoteVersion+1;
		}else{
			this.fileListVersion++;
		}
		
		// Merge file lists (naive approach, better improve it!)
		LinkedList<FileHandle> localOnlyFiles = new LinkedList<FileHandle>();
		LinkedList<FileHandle> remoteOnlyFiles = new LinkedList<FileHandle>();
		
		for(FileHandle localFH : this.files){
			boolean exists = false;
			for(FileHandle remoteFH : newList){
				if(localFH.equals(remoteFH)){
					exists = true;
					// Update file version number for existing files
					if(localFH.getVersion() < remoteFH.getVersion()){
						localFH.setVersion(remoteFH.getVersion());
					}
				}
			}
			if(!exists){
				localOnlyFiles.add(localFH);
			}
		}
		
		for(FileHandle remoteFH : newList){
			boolean exists = false;
			for(FileHandle localFH : this.files){
				if(remoteFH.equals(localFH)){
					exists = true;
				}else{
				
				}
			}
			if(!exists){
				remoteOnlyFiles.add(remoteFH);
			}
		}
		
		//TODO Rest
	}
	
	public File getDirHandle(){
		return this.sharedDir;
	}
	
	public int getVersion(){
		return this.fileListVersion;
	}
	
	public void setVersion(int v){
		this.fileListVersion = v;
	}
	
	public LinkedList<FileHandle> getFileList(){
		return this.files;
	}
	
	public void setFileList(LinkedList<FileHandle> newList){
		this.files = newList;
	}
	
	public String toString(){
		int i = 0;
		String out = "\n--- Storage ---\n";
		out += "Version:\t" + this.fileListVersion + "\n";
		while(i < this.files.size()){
			FileHandle tmp = this.files.get(i);
			out += "- " + tmp.getPath() + "\t-\t" + tmp.getHexHash() + "\n";
			i++;
		}
		out += "--- End ---";
		return out;
	}
}
