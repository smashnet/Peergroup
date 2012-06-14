/*
* Peergroup - ThriftClientBase.java
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
import java.util.concurrent.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.*;

/**
 * This thread requests blocks or FileList information from other peers.
 *
 * @author Nicolas Inden
 */
public class ThriftClientBase extends Thread {
	
	private int corePoolSize;
	private int maxPoolSize;
	private long keepAliveTime;
	private ThreadPoolExecutor threadPool;
	private final ArrayBlockingQueue<Runnable> workQueue;
	
	public ThriftClientBase(){
		this.corePoolSize = 2;
		this.maxPoolSize = 10;
		this.keepAliveTime = 10;
		this.workQueue = new ArrayBlockingQueue<Runnable>(5000);
		this.threadPool = new ThreadPoolExecutor(corePoolSize,maxPoolSize,keepAliveTime,TimeUnit.SECONDS,workQueue);
	}
		
	public void stopThriftClientBase(){
		this.interrupt();
	}
	
	/**
	* The run() method
	*/
	public void run(){
		this.setName("ThriftClientThreadPool");
		
		/*
		* Main loop, takes requests from the queue and processes them
		*/
		while(!isInterrupted()){
			try{
				FileChunk tmp;
				if((tmp = Storage.getInstance().getRarestChunk()) != null){
					tmp.setDownloading(true);
					this.runTask(new ThriftClientGetData(tmp));
				}else{
					Thread.sleep(400);
				}
			}catch(InterruptedException ie){
				interrupt();
			}
		}
		this.threadPool.shutdown();
		Constants.log.addMsg("ThriftClientThreadPool interrupted/finished. Closing...",4);
	}
	
	private void runTask(Runnable task){
		this.threadPool.execute(task);
	}
	
	public void stopPoolExecutor(){
		this.interrupt();
	}
	
}
