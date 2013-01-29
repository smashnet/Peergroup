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
 * Copyright (c) 2013 Nicolas Inden
 */

package de.pgrp.core;

import de.pgrp.thrift.*;

import java.util.*;
import java.nio.ByteBuffer;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.spec.*;
import java.security.AlgorithmParameters;

/**
 * The ThriftDataHandler implements the DataTransfer interface and defines how
 * the getDataBlock and getFileList requests are handled.
 * 
 * @author Nicolas Inden
 */
public class ThriftDataHandler implements DataTransfer.Iface {

	@Override
	public ThriftStorage getStorage() throws org.apache.thrift.TException {
		return toThriftStorage(Storage.getInstance());
	}

	/*
	 * Reads the requested data block from storage and returns it as a byte
	 * array.
	 */
	@Override
	public ByteBuffer getDataBlock(String filename, int blockID, String hash)
			throws org.apache.thrift.TException {
		FileHandle tmp;
		if ((tmp = Storage.getInstance().getFileHandle(filename)) == null) {
			return null;
		} else {
			byte[] plain = tmp.getChunkData(blockID);
			if (plain == null) {
				return null;
			}

			String plainkey = "P33rgr0up";
			byte[] salt = { 0x12, 0x78, 0x4F, 0x33, 0x13, 0x4B, 0x6B, 0x2F };
			// If we use a password for our channel, use it to encrypt the data
			if (!Constants.conference_pass.equals(""))
				plainkey = Constants.conference_pass;

			try {
				SecretKeyFactory fac = SecretKeyFactory
						.getInstance("PBKDF2WithHmacSHA1");
				KeySpec spec = new PBEKeySpec(plainkey.toCharArray(), salt,
						65536, 128);
				SecretKey tmp1 = fac.generateSecret(spec);
				SecretKey secret = new SecretKeySpec(tmp1.getEncoded(), "AES");
				// Init AES cipher
				Cipher ciph = Cipher.getInstance("AES/CBC/PKCS5Padding");
				ciph.init(Cipher.ENCRYPT_MODE, secret);
				// Encrypt data block
				byte[] encrypted = ciph.doFinal(plain);

				AlgorithmParameters params = ciph.getParameters();
				// IV is 16bytes in length
				byte[] iv = params.getParameterSpec(IvParameterSpec.class)
						.getIV();

				byte[] sendbuffer = appendByteArray(iv, encrypted);

				ByteBuffer buffer = ByteBuffer.wrap(sendbuffer);
				return buffer;
			} catch (Exception e) {
				Constants.log.addMsg(e.toString());
			}

			return null;
		}
	}

	/**
	 * Converts a Storage object into a thrift-sendable ThriftStorage object
	 * 
	 * @param localStorage
	 *            the Storage object
	 * @return the ThriftStorage object
	 */
	private static ThriftStorage toThriftStorage(Storage localStorage) {
		LinkedList<ThriftFileHandle> newList = new LinkedList<ThriftFileHandle>();
		for (FileHandle f : localStorage.getFileList()) {
			ThriftFileHandle newHandle = toThriftFileHandle(f);
			newList.add(newHandle);
		}
		ThriftStorage thriftStorage = new ThriftStorage(
				localStorage.getVersion(), newList);
		return thriftStorage;
	}

	private static ThriftFileHandle toThriftFileHandle(FileHandle localHandle) {
		LinkedList<ThriftFileChunk> newList = new LinkedList<ThriftFileChunk>();
		for (FileChunk f : localHandle.getChunks()) {
			ThriftFileChunk newChunk = toThriftFileChunk(f);
			newList.add(newChunk);
		}
		ThriftFileHandle thriftHandle = new ThriftFileHandle(
				localHandle.getPath(), localHandle.getVersion(),
				localHandle.getSize(), localHandle.getHexHash(),
				localHandle.getChunkSize(), newList);
		return thriftHandle;
	}

	private static ThriftFileChunk toThriftFileChunk(FileChunk localChunk) {
		LinkedList<ThriftP2PDevice> newList = new LinkedList<ThriftP2PDevice>();
		for (P2Pdevice d : localChunk.getPeers()) {
			ThriftP2PDevice newDev = toThriftP2PDevice(d);
			newList.add(newDev);
		}
		ThriftFileChunk thriftChunk = new ThriftFileChunk(localChunk.getID(),
				localChunk.getVersion(), localChunk.getSize(),
				localChunk.getHexHash(), newList);
		return thriftChunk;
	}

	private static ThriftP2PDevice toThriftP2PDevice(P2Pdevice localDevice) {
		return new ThriftP2PDevice(localDevice.getIP(), localDevice.getPort(),
				localDevice.getJID());
	}

	private static byte[] appendByteArray(byte[] a, byte[] b) {
		byte[] res = new byte[a.length + b.length];

		System.arraycopy(a, 0, res, 0, a.length);
		System.arraycopy(b, 0, res, a.length, b.length);

		return res;
	}
}
