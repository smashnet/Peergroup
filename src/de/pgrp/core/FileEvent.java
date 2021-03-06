/*
 * Peergroup - FileEvent.java
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

/**
 * The FileEvent contains a filename, and a timestamp for the last time an event
 * arrived.
 * 
 * @author Nicolas Inden
 */
public class FileEvent {

	private int type;
	private String filename;
	private long time;

	public FileEvent(int newType, String name) {
		this.type = newType;
		this.filename = name;
		this.time = System.currentTimeMillis();
	}

	public FileEvent(String name) {
		this.type = Globals.LOCAL_FILE_MODIFY;
		this.filename = name;
		this.time = System.currentTimeMillis();
	}

	public void setName(String newName) {
		this.filename = newName;
	}

	public void setTime(long timestamp) {
		this.time = timestamp;
	}

	public int getType() {
		return this.type;
	}

	public String getName() {
		return this.filename;
	}

	public long getTime() {
		return this.time;
	}
}
