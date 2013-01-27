/*
 * Peergroup - FSRequest.java
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
 * A general request consisting of a type and the content
 * 
 * @author Nicolas Inden
 */
public class FSRequest extends Request {

	/**
	 * The content of this request
	 */
	private String content;

	/**
	 * Constructor to use if no lamport time is given or necessary
	 */
	public FSRequest(int newID, String newContent) {
		super(newID);
		this.content = newContent;
	}

	/**
	 * Constructor to use if lamport timestamps are needed
	 */
	public FSRequest(int newID, long newLamp, String newContent) {
		super(newID, newLamp);
		this.content = newContent;
	}

	public void setID(int newID) {
		super.type = newID;
	}

	public void setLamport(long newLamp) {
		super.lamportTime = newLamp;
	}

	public void setContent(String newContent) {
		this.content = newContent;
	}

	public int getID() {
		return super.type;
	}

	public long getLamportTime() {
		return super.lamportTime;
	}

	public String getContent() {
		return this.content;
	}
}
