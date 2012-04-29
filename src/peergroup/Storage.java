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
	public FileHandle newFileFromLocal(String filename){
		try{
			FileHandle newFile = new FileHandle(Constants.rootDirectory + filename);
			this.files.add(newFile);
			Constants.log.addMsg("Adding " + newFile.toString(),4);
			this.fileListVersion++;

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
	}
	
	/**
	* Applies a change to a file in the file list
	*
	* @return The FileHandle if the file was listed, else null
	*/
	public FileHandle modifyFileFromLocal(String file){
		FileHandle tmp;
		int i = 0;
		while(i < this.files.size()){
			if(this.files.get(i).getPath().equals(file)){
				try{
					tmp = this.files.get(i);
					if(tmp.isUpdating()){
						Constants.log.addMsg("Ignoring FS update event. File gets remote updates!",4);
						return null;
					}
					if(tmp.localUpdate()){
						Constants.log.addMsg("Updated " + this.files.get(i).getPath(),4);
						this.fileListVersion++;
					}else{
						Constants.log.addMsg("No need to update something.",4);
						return null;
					}
					
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
	* @param fileHash the SHA256 value of the file
	* @param fileSize the size of the file in bytes
	* @param chunks the list of chunks for this file
	*/
	public void newFileFromXMPP(String filename, byte[] fileHash, long fileSize, LinkedList<String> blocks, int cSize, P2Pdevice node){
		try{
			LinkedList<FileChunk> chunks = new LinkedList<FileChunk>();
			
			//unparse ID and hashes and handle them
			for(String s : blocks){
				String[] tmp = s.split(":");
				int id = (Integer.valueOf(tmp[0])).intValue();
				//int vers = (Integer.valueOf(tmp[1])).intValue();
				String hash = tmp[2];
				
				chunks.add(new FileChunk(id,cSize,0,hash,node));
			}
			
			FileHandle newFile = new FileHandle(filename,fileHash,fileSize,chunks,cSize);
			newFile.setUpdating(true);
			newFile.createEmptyLocalFile();
			this.files.add(newFile);
			this.fileListVersion++;
			
			for(String s : blocks){
				String[] tmp = s.split(":");
				int id = (Integer.valueOf(tmp[0])).intValue();
				String hash = tmp[2];
				
				Constants.downloadQueue.offer(new DLRequest(Constants.DOWNLOAD_BLOCK,1,filename,id,hash,node));
			}
		}catch(Exception e){
			Constants.log.addMsg("Couldn't create FileHandle for new file from XMPP!",2);
		}
	}
	
	/**
	* Applies a file change received via XMPP
	*
	* @param name The filename of the updated file
	* @param vers The fileversion of the updated file
	* @param size The size of the updated file in bytes
	* @param blocks The list of blocks that need to be downloaded
	* @param hash The SHA256 of the updated file
	*/
	public void modifiedFileFromXMPP(String name, int vers, long size, LinkedList<String> blocks, byte[] hash, P2Pdevice node){
		FileHandle tmp;
		int i = 0;
		while(i < this.files.size()){
			if(this.files.get(i).getPath().equals(name)){
				tmp = this.files.get(i);
				tmp.setUpdating(true);
				tmp.setVersion(vers);
				tmp.setSize(size);
				tmp.setByteHash(hash);
				tmp.incrVersOnUnchangedBlocks(blocks,vers);
								
				this.fileListVersion++;
				
				for(String s : blocks){
					String[] tmp1 = s.split(":");
					int id = (Integer.valueOf(tmp1[0])).intValue();
					String hash1 = tmp1[2];
				
					Constants.downloadQueue.offer(new DLRequest(Constants.DOWNLOAD_BLOCK,vers,name,id,hash1,node));
				}
				
				return;
			}
			i++;
		}
	}
	
	/**
	* Adds the P2Pdevice to all FileChunks of the FileHandle filename
	*
	* @param fileName The filename
	* @param node The P2Pdevice
	*/
	public void addP2PdeviceToFile(String fileName, P2Pdevice node){
		FileHandle tmp;
		int i = 0;
		while(i < this.files.size()){
			if(this.files.get(i).getPath().equals(fileName)){
				tmp = this.files.get(i);
				
				tmp.addP2PdeviceToAllBlocks(node);
				
				return;
			}
			i++;
		}
	}
	
	/**
	* Adds the P2Pdevice to the FileChunks 'id' of the FileHandle 'filename'
	*
	* @param fileName The filename
	* @param node The P2Pdevice
	*/
	public void addP2PdeviceToBlocks(String fileName, LinkedList<Integer> list, P2Pdevice node){
		FileHandle tmp;
		int i = 0;
		while(i < this.files.size()){
			if(this.files.get(i).getPath().equals(fileName)){
				tmp = this.files.get(i);
				
				for(Integer no : list){
					tmp.addP2PdeviceToBlock(no.intValue(),node);
				}
				
				return;
			}
			i++;
		}
	}
	
	/**
	* Checks if a file already exists in the list
	*
	* @param filename The filename (without rootDirectory)
	* @return The FileHandle of the file, or null if doesn't exist
	*/
	public FileHandle fileExists(String filename){
		for(FileHandle f : this.files){
			if(filename.equals(f.getPath())){
				return f;
			}
		}
		return null;
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
	
	public FileHandle getFileHandle(String name){
		for(FileHandle f : this.files){
			if(f.getPath().equals(name)){
				return f;
			}
		}
		return null;
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
