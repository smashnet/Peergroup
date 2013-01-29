/*
 * Peergroup - FileHandle.java
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
 * Copyright (c) 2013 Nicolas Inden
 */

package de.pgrp.core;

import java.io.*;
import java.util.LinkedList;
import java.util.Arrays;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 * A FileHandle includes all information needed to work with a file.
 * 
 * @author Nicolas Inden
 */
public class FileHandle {

	/**
	 * The Java File object
	 */
	private File file;
	/**
	 * The fileversion is used to maintain the most recent filecontent via
	 * lamport clocks
	 */
	private int fileVersion;
	/**
	 * The hash of the file as byte array
	 */
	private byte[] hash;
	/**
	 * The size of the file in bytes
	 */
	private long size;
	/**
	 * A linked list of FileChunk objects which this file consists of
	 */
	private LinkedList<FileChunk> chunks;
	/**
	 * A list of the blocks that changed in the last localUpdate() invocation
	 */
	private LinkedList<Integer> updatedBlocks;
	/**
	 * The size of the filechunks in bytes
	 */
	private int chunkSize;
	/**
	 * This boolean is set true while the file is being updated from network
	 */
	private boolean updating;
	/**
	 * True if chunks have successfully been created, else false
	 */
	private boolean valid;
	/**
	 * True if the time this file started to download is set, else false
	 */
	private boolean timeBool;
	/**
	 * System time in milliseconds when the download of this file started
	 */
	private long dlTime;

	/**
	 * Use this constructor for complete files located on your device
	 * 
	 * @param filename
	 *            A String showing the path to the file relative to the share
	 *            directory
	 */
	public FileHandle(String filename) throws Exception {
		this.file = new File(filename);
		this.updating = true;
		this.fileVersion = 1;
		this.size = this.file.length();
		this.updatedBlocks = new LinkedList<Integer>();
		this.chunkSize = Constants.chunkSize;
		Constants.log.addMsg("FileHandle: New file from storage: "
				+ this.getPath() + " (Size: " + this.size + " Bytes)");

		int res = this.createChunks(this.chunkSize, 1);
		if (res == 0) {
			this.valid = true;
		} else {
			this.valid = false;
		}
		this.timeBool = false;
		this.updating = false;
	}

	/**
	 * Use this constructor for merging
	 * 
	 * @param filename
	 *            A String showing the path to the file relative to the share
	 *            directory
	 * @param vers
	 *            The version of the file
	 * @param fileSize
	 *            The size of the file in bytes
	 * @param hexHash
	 *            The SHA256 hash of the file represented in a hex string
	 * @param cSize
	 *            The size of the chunks
	 * @param chunks
	 *            The list of chunks this file consists of
	 */
	public FileHandle(String filename, int vers, long fileSize, String hexHash,
			int cSize, LinkedList<FileChunk> chunks) throws Exception {
		this.file = new File(Constants.rootDirectory + filename);
		this.fileVersion = vers;
		this.hash = toByteHash(hexHash);
		this.size = fileSize;
		this.chunks = chunks;
		this.chunkSize = cSize;
		this.updatedBlocks = new LinkedList<Integer>();
		this.updating = false;
		this.timeBool = false;
	}

	/**
	 * Use this constructor for files to be received via network
	 * 
	 * @param filename
	 *            A String showing the path to the file relative to the share
	 *            directory
	 * @param fileHash
	 *            The SHA256 hash of this file as byte array
	 * @param fileSize
	 *            The size of this file in bytes
	 * @param chunks
	 *            The list of chunks this file consists of
	 * @param chunkSize
	 *            The size of the chunks
	 */
	public FileHandle(String filename, byte[] fileHash, long fileSize,
			LinkedList<FileChunk> chunks, int chunkSize) throws Exception {
		this.file = new File(Constants.rootDirectory + filename);
		this.updating = true;
		this.fileVersion = 1;
		this.hash = fileHash;
		this.size = fileSize;
		this.chunks = chunks;
		this.chunkSize = chunkSize;
		this.updatedBlocks = new LinkedList<Integer>();
		Constants.log
		.addMsg("FileHandle: New file from network: " + filename
				+ " (Size: " + this.size + ", Hash: "
				+ this.getHexHash() + ")");
		this.updating = false;
		this.timeBool = false;
	}

	/**
	 * Converts the hex-string representation of a hash into a byte array
	 * 
	 * @param s
	 *            The hash as string
	 */
	public static byte[] toByteHash(String s) {
		HexBinaryAdapter adapter = new HexBinaryAdapter();
		return adapter.unmarshal(s);
	}

	/**
	 * Creates a linked list of FileChunk for this FileHandle
	 * 
	 * @param size
	 *            the size of a chunk (last one might be smaller)
	 */
	private synchronized int createChunks(int size, int vers) {
		if (!(this.chunks == null)) {
			Constants.log.addMsg("(" + this.file.getName()
					+ ") Chunklist not empty!", 4);
			return 1;
		}
		try {
			FileInputStream stream = new FileInputStream(this.file);
			MessageDigest sha = MessageDigest.getInstance(Constants.hashAlgo);
			this.chunks = new LinkedList<FileChunk>();
			int bytesRead = 0;
			int id = 0;
			byte[] buffer = new byte[size];

			while ((bytesRead = stream.read(buffer)) != -1) {
				FileChunk next = new FileChunk(this.getPath(), id, vers,
						calcHash(buffer, bytesRead), bytesRead, id * size, true);
				sha.update(buffer, 0, bytesRead);
				this.chunks.add(next);
				id++;
			}

			// On empty file, also create one empty chunk
			if (bytesRead == -1 && id == 0) {
				FileChunk next = new FileChunk(this.getPath(), id, vers,
						calcHash(buffer, bytesRead), 0, id * size, true);
				this.chunks.add(next);
			}

			this.hash = sha.digest();

			stream.close();

			Constants.log.addMsg("FileHandle: " + this.file.getPath() + " has "
					+ this.chunks.size() + " chunks");
			return 0;
		} catch (IOException ioe) {
			Constants.log.addMsg("createChunks: IOException: " + ioe, 1);
			return 2;
		} catch (NoSuchAlgorithmException alge) {
			Constants.log.addMsg("calcHash Error: " + alge, 1);
			System.exit(1);
		}
		return 3;
	}

	/**
	 * General function to calculate the hash of a given file
	 * 
	 * @param in
	 *            the file
	 * @return hash as byte array
	 */
	private byte[] calcHash(File in) {
		try {
			FileInputStream stream = new FileInputStream(in);
			MessageDigest sha = MessageDigest.getInstance(Constants.hashAlgo);
			int bytesRead = 0;
			byte[] buffer = new byte[1024];

			while ((bytesRead = stream.read(buffer)) != -1) {
				sha.update(buffer, 0, bytesRead);
			}

			stream.close();

			return sha.digest();
		} catch (IOException ioe) {
			Constants.log.addMsg("calcHash Error: " + ioe, 1);
		} catch (NoSuchAlgorithmException na) {
			Constants.log.addMsg("calcHash Error: " + na, 1);
			System.exit(1);
		}
		return null;
	}

	/**
	 * General function to calculate the hash of a given byte array
	 * 
	 * @param in The byte array
	 * @param bytes Amount of bytes form the beginning to consider
	 * @return hash as byte array
	 */
	protected static byte[] calcHash(byte[] in, int bytes) {
		try {
			MessageDigest sha = MessageDigest.getInstance(Constants.hashAlgo);
			sha.update(in, 0, bytes);

			return sha.digest();
		} catch (NoSuchAlgorithmException na) {
			Constants.log.addMsg("calcHash Error: " + na, 1);
			System.exit(1);
		}
		return null;
	}

	/**
	 * This updates all parameters and increments the fileversion, if a local
	 * filechange is detected.
	 * 
	 * @return true if file has changed, else false
	 */
	public synchronized boolean localUpdate() throws Exception {
		Constants.log.addMsg("FileHandle: Local update triggered for " + this.file.getName() + ". Scanning for changes!");
		boolean changed = false;
		FileInputStream stream = new FileInputStream(this.file);
		int bytesRead = 0;
		int id = 0;
		MessageDigest hash = MessageDigest.getInstance(Constants.hashAlgo);
		byte[] buffer = new byte[chunkSize];
		this.fileVersion += 1;
		this.size = this.file.length();

		while ((bytesRead = stream.read(buffer)) > 0) {
			// update hash
			hash.update(buffer, 0, bytesRead);
			
			// change is within existent chunks
			if (id < this.chunks.size()) {
				// new chunk hash != old chunk hash
				if (!(Arrays.equals(calcHash(buffer, bytesRead), this.chunks.get(id).getHash()))) {
					Constants.log.addMsg("FileHandle: Chunk " + id	+ " changed! (Different Hashes) Updating chunklist...");
					FileChunk updated = 
						new FileChunk(
							this.getPath(), id, this.chunks.get(id).getVersion() + 1, calcHash(buffer, bytesRead), bytesRead, id * chunkSize, true
								);
					this.updatedBlocks.add(new Integer(id));
					this.chunks.set(id, updated);
					changed = true;
				}
				// chunk is smaller than others and is not the last chunk ->
				// file got smaller
				if (bytesRead < chunkSize && id < (this.chunks.size() - 1)) {
					Constants.log.addMsg("FileHandle: Smaller chunk is not last chunk! Pruning following chunks...");
					int i = this.chunks.size() - 1;
					while (i > id) {
						this.chunks.removeLast();
						i--;
					}
					changed = true;
				}
				// Last chunk got bigger
				if (bytesRead > this.chunks.get(id).getSize() && id == this.chunks.size() - 1) {
					Constants.log.addMsg("FileHandle: Chunk " + id + " changed! Updating chunklist...");
					FileChunk updated = 
						new FileChunk(
							this.getPath(), id,this.chunks.get(id).getVersion() + 1, calcHash(buffer, bytesRead), bytesRead, id * chunkSize, true
								);
					this.chunks.set(id, updated);
					this.updatedBlocks.add(new Integer(id));
					changed = true;
				}
			} else {
				// file is grown and needs more chunks
				Constants.log.addMsg("FileHandle: File needs more chunks than before! Adding new chunk: " + id);
				FileChunk next = 
					new FileChunk(this.getPath(), id, this.fileVersion, calcHash(buffer, bytesRead), bytesRead, id * chunkSize, true);
				this.chunks.add(next);
				this.updatedBlocks.add(new Integer(id));
				changed = true;
			}
			id++;
		}
		
		// Less chunks than before -> file got smaller while remaining a multiple of
		// the chunk size
		if(id < this.chunks.size()){
			int i = this.chunks.size() - 1;
			while (i > id) {
				this.chunks.removeLast();
				i--;
			}
			changed = true;
		}
		
		if (!Arrays.equals(this.hash, hash.digest())) {
			this.hash = calcHash(this.file);
			changed = true;
		}

		stream.close();

		if (!changed)
			Constants.log.addMsg("No changes found...", 4);
		return changed;
	}

	/**
	 * Adds a P2Pdevice to a specific chunk of this file, indicating that this
	 * chunk can now be downloaded from this device
	 * 
	 * @param id
	 *            The id of the corresponding chunk
	 * @param node
	 *            The P2Pdevice
	 */
	public synchronized void addP2PdeviceToBlock(int id, P2Pdevice node) {
		if (id >= this.chunks.size()) {
			Constants.log.addMsg("Cannot add node to not existing chunk! (ID: " + id + " Size: " + this.chunks.size() + ")", 4);
			return;
		}
		this.chunks.get(id).addPeer(node);
	}

	/**
	 * Adds a P2Pdevice to all chunks of this file, indicating that they can now
	 * be downloaded from this device
	 * 
	 * @param node
	 *            The P2Pdevice
	 */
	public synchronized void addP2PdeviceToAllBlocks(P2Pdevice node) {
		for (FileChunk f : this.chunks) {
			f.addPeer(node);
		}
	}

	/**
	 * Clears the list of P2Pdevices for all chunks of this file
	 */
	public synchronized void clearP2Pdevices() {
		for (FileChunk f : this.chunks) {
			f.clearPeers();
		}
	}

	/**
	 * Creates an empty file to allocate the file on local storage. Also creates
	 * all neccessary folders.
	 */
	public void createEmptyLocalFile() {
		// Create empty file on disk
		try {
			String parent = this.file.getParent();
			File parentDir = new File(parent);
			parentDir.mkdirs();
			this.file.createNewFile();
		} catch (IOException ioe) {
			Constants.log.addMsg("FileHandle: Cannot create dummy file: " + ioe, 4);
		}
	}

	/**
	 * Returns the data of the requested file chunk
	 * 
	 * @param id
	 *            the id of the chunk
	 * @return data of the chunk as byte array
	 */
	public byte[] getChunkData(int id) {
		if (this.chunks == null) {
			Constants.log.addMsg("Cannot return chunkData -> no chunk list available", 1);
			return null;
		}

		if (id >= this.chunks.size()) {
			Constants.log.addMsg("Cannot return chunkData -> ID exceeds list: ID: " + id + " List: " + this.chunks.size(), 1);
			return null;
		}
		FileChunk recent = this.chunks.get(id);
		if (!recent.isComplete()) {
			Constants.log.addMsg("Cannot return chunkData -> chunk not complete (chunk" + id + ")", 1);
			return null;
		}

		try {
			FileInputStream stream = new FileInputStream(this.file);
			long bytesSkipped, bytesRead;
			byte[] buffer = new byte[recent.getSize()];

			bytesSkipped = stream.skip(recent.getOffset()); // Jump to correct
			// part of the file
			if (bytesSkipped != recent.getOffset())
				Constants.log.addMsg("FileHandle: Skipped more or less bytes than offset",
						4);

			bytesRead = stream.read(buffer);

			if (bytesRead == -1)
				Constants.log.addMsg(
						"FileHandle: getChunkData EOF - ID: " + id, 4);

			stream.close();

			return buffer;
		} catch (IOException ioe) {
			Constants.log.addMsg("Error skipping bytes in chunk:" + ioe, 1);
			return null;
		}
	}

	/**
	 * Writes a chunk of data to the local storage
	 * 
	 * @param id
	 *            The chunk ID
	 * @param data
	 *            The data as byte array
	 */
	public synchronized void setChunkData(int id, String hash, P2Pdevice node,
			byte[] data) {
		if (this.chunks == null) {
			Constants.log.addMsg(
					"Cannot set chunkData -> no chunk list available", 1);
			return;
		}
		FileChunk recent;
		if (id >= this.chunks.size()) {
			recent = new FileChunk(this.getPath(), id, Constants.chunkSize, this.fileVersion, hash, node, true);
			this.chunks.add(recent);
		} else {
			recent = this.chunks.get(id);
		}

		try {
			RandomAccessFile stream = new RandomAccessFile(this.file, "rwd");
			stream.seek(recent.getOffset()); // Jump to correct part of the file
			stream.write(data);
			stream.close();

			recent.setHexHash(hash);
			recent.setComplete(true);
			recent.setDownloading(false);
		} catch (IOException ioe) {
			Constants.log.addMsg("Error writing to file:" + ioe, 1);
		}
	}

	/**
	 * Converts a hash in a byte array into a readable hex string
	 * 
	 * @param in
	 *            the byte array
	 * @return the hex string
	 */
	public static String toHexHash(byte[] in) {
		HexBinaryAdapter adapter = new HexBinaryAdapter();
		String hash = adapter.marshal(in);
		return hash;
	}

	/**
	 * Returns the hash of this file as readable hex string
	 * 
	 * @return the hex string
	 */
	public String getHexHash() {
		HexBinaryAdapter adapter = new HexBinaryAdapter();
		String hash = adapter.marshal(this.hash);
		return hash;
	}

	/**
	 * Returns the hash value for the specified block where the
	 * hash algorithm is defined in Constants.java
	 * 
	 * @param no
	 *            The no of the chunk
	 * @return The hash of the chunk as String
	 */
	public String getChunkHash(int no) {
		return toHexHash(this.chunks.get(no).getHash());
	}

	/**
	 * Returns a list of blocks that have updated with the last call of
	 * localUpdate()
	 * 
	 * @return A linked list of Integer showing the IDs of updated blocks
	 */
	public LinkedList<Integer> getUpdatedBlocks() {
		return this.updatedBlocks;
	}

	/**
	 * Returns the list of chunks of this file
	 * 
	 * @return A LinkedList of FileChunk objects
	 */
	public LinkedList<FileChunk> getChunks() {
		return this.chunks;
	}

	/**
	 * Updates the version of a chunk to the current version of the file
	 * 
	 * @param id
	 *            The chunk id
	 */
	public synchronized void updateChunkVersion(int id) {
		for (FileChunk f : this.chunks) {
			if (id == f.getID()) {
				f.setVersion(this.fileVersion);
				return;
			}
		}
	}

	/**
	 * Updates the version of a chunk to the given version
	 * 
	 * @param id
	 *            The chunk id
	 * @param vers
	 *            The new version for the chunk
	 */
	public void setChunkVersion(int id, int vers) {
		for (FileChunk f : this.chunks) {
			if (id == f.getID()) {
				f.setVersion(vers);
				return;
			}
		}
	}

	/**
	 * Clear the list of updated blocks
	 */
	public void clearUpdatedBlocks() {
		this.updatedBlocks.clear();
	}

	/**
	 * Returns true if filename and hash match
	 * 
	 * @param compFH
	 *            The FileHandle to compare with
	 * @return true if equal, else false
	 */
	public boolean equals(FileHandle compFH) {
		if (!this.getPath().equals(compFH.getPath()))
			return false;
		if (!Arrays.equals(this.hash, compFH.getByteHash()))
			return false;

		return true;
	}

	/**
	 * Returns a list of strings looking like this: "id:version:hash"
	 * 
	 * @return the list
	 */
	public LinkedList<String> getBlockIDwithHash() {
		LinkedList<String> tmp = new LinkedList<String>();
		for (FileChunk f : this.chunks) {
			String newBlock = f.getID() + ":" + f.getVersion() + ":" + f.getHexHash() + ":" + f.getSize();
			tmp.add(newBlock);
		}
		return tmp;
	}

	/**
	 * Update chunk information after XMPP update
	 * 
	 * @param blocks
	 *            A list of changed chunks as string, formatted as follows:
	 *            "id:version:hash:size"
	 * @param vers
	 *            The new file version
	 * @param node
	 *            P2Pdevice that is in possession of the updated chunks
	 */
	public synchronized void updateBlocks(LinkedList<String> blocks, int vers, int noOfChunks, P2Pdevice node) {
		
		// Before invoking updateBlocks, we already set the new size of the file, so we can trim
		// unneccessary FileChunks, and the file on the storage device
		this.trimFile();
		
		if(blocks.size() == 0){
			for (FileChunk f : this.chunks) {
				f.setVersion(vers);
			}
			return;
		}

		for (String s : blocks) {
			System.out.println(s);
			String tmp[] = s.split(":");
			int index = Integer.valueOf(tmp[0]);
			if (index > this.chunks.size() - 1) {
				FileChunk tmp1 = new FileChunk(this.getPath(), index, Integer.valueOf(tmp[3]), vers - 1, tmp[2], node, false);
				this.chunks.add(tmp1);
			} else if (0 <= index && index < this.chunks.size()) {
				FileChunk tmp1 = this.chunks.get(index);
				tmp1.setHexHash(tmp[2]);
				tmp1.setSize(Integer.valueOf(tmp[3]));
				tmp1.clearPeers();
				tmp1.addPeer(node);
				tmp1.setComplete(false);
			}
		}

		if (blocks.size() == this.chunks.size()) {
			// Finished if all blocks have changed
			return;
		}

		// Set versions of unchanged blocks to current version
		int i = 0;
		for (FileChunk f : this.chunks) {
			int id = Integer.valueOf(blocks.get(i).split(":")[0]);
			if (f.getID() != id) {
				f.setVersion(vers);
			} else {
				if (i < blocks.size() - 1)
					i++;
			}
		}
	}

	/**
	 * Trim filesize on storage device after update and remove unnecessary
	 * chunks.
	 */
	public void trimFile() {
		try {
			RandomAccessFile thisFile = new RandomAccessFile(this.file, "rws");
			thisFile.setLength(this.size);
			thisFile.close();

			double fileSize = this.size;
			double cSize = this.chunkSize;
			int blocks = (int) Math.ceil(fileSize / cSize);

			int diff = this.chunks.size() - blocks;
			if (diff > 0) {
				for (int i = 0; i < diff; i++) {
					this.chunks.removeLast();
				}
			}
		} catch (FileNotFoundException e) {
			Constants.log.addMsg("No file to trim, this should not happen!! (" + e + ")", 1);
		} catch (IOException ioe) {
			Constants.log.addMsg("Error trimming file, this should not happen!! (" + ioe + ")", 1);
		}
	}

	/**
	 * Returns a relative path to this file/directory (e.g. /subdir/file.txt)
	 * 
	 * @return a path to the file/directory relative to the document root (e.g.
	 *         /subdir/file.txt)
	 */
	public String getPath() {
		return this.file.getPath().substring(Constants.rootDirectory.length());
	}

	/**
	 * Returns if blocks of this file are currently downloaded
	 */
	public boolean isDownloading() {
		for (FileChunk f : this.chunks) {
			if (f.isDownloading()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns if all blocks are stored to the local storage
	 */
	public boolean isComplete() {
		for (FileChunk f : this.chunks) {
			if (f.getVersion() != this.getVersion()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns if all blocks are stored to the local storage
	 */
	public boolean hasFailed() {
		for (FileChunk f : this.chunks) {
			if (f.hasFailed()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a list of chunks that need to be downloaded
	 * 
	 * @return the list of chunks
	 */
	public LinkedList<FileChunk> getIncomplete() {
		LinkedList<FileChunk> incomplete = new LinkedList<FileChunk>();
		for (FileChunk f : this.chunks) {
			if (f.getVersion() != this.getVersion()) {
				incomplete.add(f);
			}
		}
		return incomplete;
	}

	public LinkedList<FileChunk> getChunkList() {
		return this.chunks;
	}

	public byte[] getByteHash() {
		return this.hash;
	}

	public void setByteHash(byte[] newHash) {
		this.hash = newHash;
	}

	public File getFile() {
		return this.file;
	}

	public long getSize() {
		return this.size;
	}

	public int getChunkSize() {
		return this.chunkSize;
	}

	public int getChunkSize(int i) {
		return this.chunks.get(i).getSize();
	}

	public int getNoOfChunks() {
		return this.chunks.size();
	}

	public void setSize(long newSize) {
		this.size = newSize;
	}

	public int getVersion() {
		return this.fileVersion;
	}

	public void setVersion(int newVers) {
		this.fileVersion = newVers;
	}

	public void setUpdating(boolean up) {
		this.updating = up;
	}

	public void setValid(boolean val) {
		this.valid = val;
	}

	public boolean isUpdating() {
		return this.updating;
	}

	public boolean isValid() {
		return this.valid;
	}

	public boolean getTimeBool() {
		return this.timeBool;
	}

	public long getDLTime() {
		return this.dlTime;
	}

	public void setTimeBool(boolean value) {
		this.timeBool = value;
	}

	public void setDLTime(long value) {
		this.dlTime = value;
	}

	@Override
	public String toString() {
		String out = "\n---------- FileHandle toString ----------\n";
		out += "Filename: \t" + this.getPath() + "\n";
		out += "Size: \t\t" + this.size + " Byte\n";
		out += "Chunks: \t" + this.chunks.size() + " pieces\n";
		/*for (int i = 0; i < this.chunks.size(); i++) {
			out += "\t" + i + ": \t" + toHexHash(this.chunks.get(i).getHash()) + ", " + this.chunks.get(i).getSize() + " Bytes\n";
		}*/
		out += "SHA-256: \t" + this.getHexHash() + "\n";
		out += "Complete: \t" + this.isComplete() + "\n";
		out += "------------ End toString -------------";
		return out;
	}
}
