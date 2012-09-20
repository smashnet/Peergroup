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
* Copyright (c) 2012 Nicolas Inden
*/

package peergroup;

import java.util.*;
import org.apache.thrift.server.*;
import org.apache.thrift.transport.*;

/**
 * This thread listens for thrift requests and processes them.
 *
 * @author Nicolas Inden
 */
public class ThriftServerWorker extends Thread {
	
	private TServerSocket serverTransport;
	private DataTransfer.Processor processor;
	private TServer server;
	
	public ThriftServerWorker(){
		
	}
	
	/**
	* The run() method
	*/
	public void run(){
		this.setName("Thrift-Server Thread");
		try{
			this.serverTransport = new TServerSocket(Constants.p2pPort);
			this.processor = new DataTransfer.Processor(new ThriftDataHandler());
			TThreadPoolServer.Args tpsa = new TThreadPoolServer.Args(serverTransport).processor(processor);
			this.server = new TThreadPoolServer(tpsa);
			
			Constants.log.addMsg("Starting thrift handler on port " + Constants.p2pPort);
			this.server.serve();
		}catch(TTransportException e){
			Constants.log.addMsg("Thrift server error: " + e);
		}
		
		Constants.log.addMsg("Thrift-Server-Thread interrupted. Closing...",4);
	}
	
	/**
	* Normally this should cleanly shutdown this thread, but the
	* thrift server is a bit intractable here, so we'd better
	* use the deprecated Thread.stop() instead of this function.
	*/
	public void stopThriftWorker(){
		this.server.stop();
		this.interrupt();
	}
}
