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
import java.security.spec.*;

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
			Globals.log.addMsg("DOWNLOAD_BLOCK: " + chunk.getName()
					+ " - Block " + chunk.getID() + " from " + device.getIP()
					+ ":" + device.getPort());

			if (!tmp.getTimeBool()) {
				tmp.setDLTime(System.currentTimeMillis());
				tmp.setTimeBool(true);
			}

			byte[] swap = getBlock(chunk.getName(), chunk.getID(),
					chunk.getHexHash(), device);
			if (swap != null) {

				// Seperate encrypted data and IV
				byte[] data = new byte[swap.length - 16];
				byte[] iv = new byte[16];
				System.arraycopy(swap, 0, iv, 0, 16);
				System.arraycopy(swap, 16, data, 0, swap.length - 16);

				String plainkey = "P33rgr0up";
				byte[] salt = { 0x12, 0x78, 0x4F, 0x33, 0x13, 0x4B, 0x6B, 0x2F };
				// If we use a password for our channel, use it to decrypt the
				// data
				if (!Globals.conference_pass.equals(""))
					plainkey = Globals.conference_pass;

				try {
					SecretKeyFactory fac = SecretKeyFactory
							.getInstance("PBKDF2WithHmacSHA1");
					KeySpec spec = new PBEKeySpec(plainkey.toCharArray(), salt,
							65536, 128);
					SecretKey tmp1 = fac.generateSecret(spec);
					SecretKey secret = new SecretKeySpec(tmp1.getEncoded(),
							"AES");
					// Init AES cipher
					Cipher ciph = Cipher.getInstance("AES/CBC/PKCS5Padding");
					ciph.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(
							iv));
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

					Globals.log.addMsg(chunk.getName() + " Block " + chunk.getID() + ": Hash OK!");

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
