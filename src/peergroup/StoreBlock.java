/*
* Peergroup - StoreBlock.java
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
* Copyright (c) 2012 Nicolas Inden
*/

package peergroup;

/**
 *
 * @author Nicolas Inden
 */
public class StoreBlock {
    
	private FileHandle handle;
    private int blockID;
	private String hash;
	private P2Pdevice node;
	private byte[] buffer;
    
    public StoreBlock(FileHandle handle, int id, String hash, P2Pdevice device, byte[] bytes){
        this.handle = handle;
		this.blockID = id;
		this.hash = hash;
		this.node = device;
		this.buffer = bytes;
    }
	
	public FileHandle getFileHandle(){
		return this.handle;
	}
	
	public String getName(){
		return this.handle.getPath();
	}
	
	public int getVersion(){
		return this.handle.getVersion();
	}
	
	public int getID(){
		return this.blockID;
	}
	
	public String getHexHash(){
		return this.hash;
	}
	
	public P2Pdevice getDevice(){
		return this.node;
	}
	
	public byte[] getData(){
		return this.buffer;
	}
}
