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
 *
 * @author Nicolas Inden
 */
public class Storage {
    
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
	* Adds a local file to the storage list
	*
	* @param filename the filename
	*/
	public void addFileFromLocal(String filename){
		try{
			FileHandle newFile = new FileHandle(Constants.rootDirectory + filename);
			this.files.add(newFile);
			Constants.log.addMsg("Adding " + newFile.toString(),4);
			this.fileListVersion++;
		}catch(Exception ioe){
			Constants.log.addMsg("Local file does not exist anymore: " + ioe,4);
		}	
		Constants.log.addMsg(this.toString(),4);
	}
	
	/**
	* Adds a remote file to the storage list
	*
	* @param filename the filename+path (e.g. subdir/file.txt)
	* @param vers the fileversion
	* @param fileHash the SHA-256 value of the file
	* @param fileSize the size of the file in bytes
	* @param chunks the list of chunks for this file
	*/
	public void addFileFromXMPP(String filename, int vers, byte[] fileHash, long fileSize, LinkedList<FileChunk> chunks, int cSize) throws Exception{
		FileHandle newFile = new FileHandle(Constants.rootDirectory + filename,vers,fileHash,fileSize,chunks,cSize);
		this.files.add(newFile);
		this.fileListVersion++;
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
	* Applies a change to a file in the file list
	*
	*/
	public void modifyFileFromLocal(String file){
		FileHandle tmp;
		int i = 0;
		while(i < this.files.size()){
			if(this.files.get(i).getPath().equals(file)){
				try{
					this.files.get(i).localUpdate();
					//Constants.log.addMsg("Updating: " + this.files.get(i).toString(),4);
				}catch(Exception ioe){
					Constants.log.addMsg("Error updating file: " + ioe,1);
					break;
				}
				Constants.log.addMsg("Updated " + this.files.get(i).getPath(),4);
				break;
			}
			i++;
		}
		this.fileListVersion++;
		Constants.log.addMsg(this.toString(),4);
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
