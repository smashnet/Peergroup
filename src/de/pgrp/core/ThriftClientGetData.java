/*
 * Peergroup - ThriftClientGetData.java
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

import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * This thread requests blocks or FileList information from other peers.
 * 
 * @author Nicolas Inden
 */
public class ThriftClientGetData implements Runnable {

	private FileChunk chunk;

	public ThriftClientGetData(FileChunk chunk) {
		this.chunk = chunk;
	}

	/**
	 * The run() method
	 */
	@Override
	public void run() {
		FileHandle tmp;
		if ((tmp = Storage.getInstance().getFileHandle(chunk.getName())) == null) {
			return;
		} else {
			P2Pdevice device = chunk.getRandomPeer();
			if (device == null) {
				return;
			}
			//Globals.log.addMsg("DOWNLOAD_BLOCK: " + chunk.getName() + " - Block " + chunk.getID() + " from " + device.getUsedIP() + ":" + device.getPort());

			if (!tmp.getTimeBool()) {
				tmp.setDLTime(System.currentTimeMillis());
				tmp.setTimeBool(true);
			}

			//Get chunk from peer
			byte[] swap = getBlock(chunk.getName(), chunk.getID(), chunk.getHexHash(), device);
			if (swap != null) {

				if(Globals.encryptDataTransfers){
					// Seperate encrypted data and IV
					byte[] data = new byte[swap.length - 16];
					byte[] iv = new byte[16];
					System.arraycopy(swap, 0, iv, 0, 16);
					System.arraycopy(swap, 16, data, 0, swap.length - 16);
				
					swap = null;

					try {
						// Init AES cipher
						Cipher ciph = Cipher.getInstance("AES/CBC/PKCS5Padding");
						ciph.init(Cipher.DECRYPT_MODE, Globals.secKey, new IvParameterSpec(iv));
						// Decrypt data block
						data = ciph.doFinal(data);

						// If hash does not match after transmission and decryption,
						// set as failed, and try again
						if (!chunk.checkHash(data)) {
							chunk.setComplete(false);
							chunk.setDownloading(false);
							chunk.setFailed(true);

							return;
						}

						//Globals.log.addMsg(chunk.getName() + " Block " + chunk.getID() + ": Hash OK!");
						Globals.log.addMsg("DOWNLOADED_BLOCK: " + chunk.getName()
								+ " - Block " + chunk.getID() + " from " + device.getUsedIP()
								+ ":" + device.getPort() + " - Hash OK!");

						chunk.setDownloading(false);
						chunk.setComplete(true);
						chunk.setFailed(false);
					
						removeChunkFromDownloadsList(chunk);
					
						Globals.storeQueue.offer(new StoreBlock(tmp, chunk.getID(), chunk.getHexHash(), device, data));
						if (!tmp.isDownloading() && !tmp.hasFailed()) {
							tmp.setTimeBool(false);
							long dlTime = System.currentTimeMillis() - tmp.getDLTime();
							double res = ((double) dlTime) / 1000;
							Network.getInstance().sendMUCmessage(tmp.getPath() + "," + tmp.getSize() + "," + res);
						}

					} catch (Exception e) {
						Globals.log.addMsg("Wrong password: " + e.toString());
						chunk.setComplete(false);
						chunk.setDownloading(false);
						chunk.setFailed(true);
					}
				} else {
					// If hash does not match after transmission and decryption,
					// set as failed, and try again
					if (!chunk.checkHash(swap)) {
						chunk.setComplete(false);
						chunk.setDownloading(false);
						chunk.setFailed(true);

						return;
					}

					Globals.log.addMsg(chunk.getName() + " Block " + chunk.getID() + ": Hash OK!");

					chunk.setDownloading(false);
					chunk.setComplete(true);
					chunk.setFailed(false);
				
					removeChunkFromDownloadsList(chunk);
				
					Globals.storeQueue.offer(new StoreBlock(tmp, chunk.getID(), chunk.getHexHash(), device, swap));
					if (!tmp.isDownloading() && !tmp.hasFailed()) {
						tmp.setTimeBool(false);
						long dlTime = System.currentTimeMillis() - tmp.getDLTime();
						double res = ((double) dlTime) / 1000;
						Network.getInstance().sendMUCmessage(tmp.getPath() + "," + tmp.getSize() + "," + res);
					}
				}
			} else {
				chunk.setComplete(false);
				chunk.setDownloading(false);
				chunk.setFailed(true);
			}
		}
	}

	private void removeChunkFromDownloadsList(FileChunk chunk2) {
		synchronized(Globals.downloadsList){
			DLULItem tmp = new DLULItem(chunk2.getName(),chunk2.getID());
			Globals.downloadsList.remove(tmp);
		}
	}

	private byte[] getBlock(String name, int id, String hash, P2Pdevice node) {
		return node.getDataBlock(name, id, hash);
	}
}
