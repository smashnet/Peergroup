/*
 * Peergroup - ThriftServerWorker.java
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

import org.apache.thrift.server.*;
import org.apache.thrift.transport.*;

/**
 * This thread listens for thrift requests and processes them.
 * 
 * @author Nicolas Inden
 */
public class ThriftServerWorker extends Thread {

	private TServerSocket serverTransport;
	@SuppressWarnings("rawtypes")
	private DataTransfer.Processor processor;
	private TServer server;

	public ThriftServerWorker() {

	}

	/**
	 * The run() method
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void run() {
		this.setName("Thrift-Server Thread");
		try {
			this.serverTransport = new TServerSocket(Globals.p2pPort);
			this.processor = new DataTransfer.Processor(new ThriftDataHandler());
			
			//Singlethreaded:
			this.server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));
			
			//Multithreaded:
			//TThreadPoolServer.Args tpsa = new TThreadPoolServer.Args(serverTransport).processor(processor);
			//tpsa.minWorkerThreads(1);
			//tpsa.maxWorkerThreads(10);
			//this.server = new TThreadPoolServer(tpsa);

			Globals.log.addMsg("Starting thrift handler on port " + Globals.p2pPort);
			this.server.serve();
		} catch (TTransportException e) {
			Globals.log.addMsg("Thrift server error: " + e);
		}

		Globals.log.addMsg("Thrift-Server-Thread interrupted. Closing...", 4);
	}

	/**
	 * Normally this should cleanly shutdown this thread, but the thrift server
	 * is a bit intractable here, so we'd better use the deprecated
	 * Thread.stop() instead of this function.
	 */
	@SuppressWarnings("deprecation")
	public void stopThriftWorker() {
		this.server.stop();
		this.stop();
	}
}
