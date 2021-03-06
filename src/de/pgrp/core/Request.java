/*
 * Peergroup - Request.java
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
public class Request {

	/**
	 * The type specifies what kind of request this is
	 */
	protected int type;

	/**
	 * The lamport time for this request (to reconstruct an ordering of
	 * requests)
	 */
	protected long lamportTime;

	/**
	 * Constructor to use if no lamport time is given or necessary
	 */
	public Request(int newID) {
		this.type = newID;
		this.lamportTime = -1;
	}

	/**
	 * Constructor to use if lamport timestamps are needed
	 */
	public Request(int newID, long newLamp) {
		this.type = newID;
		this.lamportTime = newLamp;
	}

	public void setID(int newID) {
		this.type = newID;
	}

	public void setLamport(long newLamp) {
		this.lamportTime = newLamp;
	}

	public int getID() {
		return this.type;
	}

	public long getLamportTime() {
		return this.lamportTime;
	}
}
