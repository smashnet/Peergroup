/*
 * Peergroup - ThriftClientGetFileList.java
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

import de.pgrp.thrift.*;

import java.util.*;

/**
 * This thread requests blocks or FileList information from other peers.
 * 
 * @author Nicolas Inden
 */
public class ThriftClientGetFileList extends Thread {

	private int vers;
	private P2Pdevice node;

	public ThriftClientGetFileList(int vers, P2Pdevice node) {
		this.vers = vers;
		this.node = node;
	}

	/**
	 * The run() method
	 */
	@Override
	public void run() {
		Constants.log.addMsg("DOWNLOAD_CURRENT_FILE_LIST: Version " + vers
				+ " from " + node.getJID());
		ThriftStorage newStorage = node.getFileList();
		LinkedList<FileHandle> newFiles = new LinkedList<FileHandle>();
		for (ThriftFileHandle fh : newStorage.getFiles()) {
			LinkedList<FileChunk> chunks = new LinkedList<FileChunk>();

			for (ThriftFileChunk fc : fh.getChunks()) {
				LinkedList<P2Pdevice> devices = new LinkedList<P2Pdevice>();

				for (ThriftP2PDevice dev : fc.getDevices()) {
					P2Pdevice newDev = P2Pdevice.getDevice(dev.getJid(), dev.getIp(),
							dev.getPort());
					devices.add(newDev);
				}
				FileChunk newChunk = new FileChunk(fh.getFilename(),
						fc.getChunkID(), fc.getSize(), fc.getBlockVersion(),
						fc.getHash(), devices, false); // Don't forget to change
				// when merging
				chunks.add(newChunk);
			}
			try {
				FileHandle newHandle = new FileHandle(fh.getFilename(),
						fh.getFileVersion(), fh.getSize(), fh.getHash(),
						fh.getChunkSize(), chunks);
				newFiles.add(newHandle);
			} catch (Exception e) {
				Constants.log.addMsg(
						"Error creating FileHandle for " + fh.getFilename()
						+ " while receiving FileList", 2);
			}
		}
		Storage.getInstance().mergeWithRemoteStorage(newStorage.getVersion(),
				newFiles);
	}
}
