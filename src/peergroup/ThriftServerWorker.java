/*
* Peergroup - ThriftServerWorker.java
* 
* Peergroup is a P2P Shared Storage System using XMPP for data- and 
* participantmanagement and Apache Thrift for direct data-
* exchange between users.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* License: Not for public distribution!
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
		this.setName("THRIFT-Handling Thread");
		this.startServer(Constants.p2pPort);
		Constants.log.addMsg("Thrift-Server-Thread interrupted. Closing...",4);
	}
	
	private void startServer(int port){
		try{
			this.serverTransport = new TServerSocket(port);
			this.processor = new DataTransfer.Processor(new ThriftDataHandler());
			this.server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));
			
			Constants.log.addMsg("Starting thrift handler on port " + port);
			this.server.serve();
		}catch(TTransportException e){
			Constants.log.addMsg("Error in thrift handler: " + e);
		}
	}
	
	public void stopThriftWorker(){
		this.server.stop();
	}
}
