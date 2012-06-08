/*
* Peergroup - ModifyQueueWorker.java
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

import java.util.concurrent.TimeUnit;

/**
 * This thread regularily checks the ModifyEventQueue if files
 * haven't been updated for more than one second. In this case
 * a modify request is put in the main queue.
 *
 * @author Nicolas Inden
 */
public class ModifyQueueWorker extends Thread {
	
	/**
	* Creates a ModifyQueueWorker.
	*/
	public ModifyQueueWorker(){
	}
	
	/**
	* The run() method
	*/
	public void run(){
		this.setName("ModifyQueue Thread");
		Constants.log.addMsg("ModifyQueue thread started...");
		while(!isInterrupted()){
			try{
				//Process data from storeQueue
				long timeA,timeB;
				timeA = System.currentTimeMillis();
				do{
					StoreBlock blockInfo = Constants.storeQueue.poll(100,TimeUnit.MILLISECONDS);
					if(blockInfo == null){
						timeB = System.currentTimeMillis();
						continue;
					}
					FileHandle tmp = blockInfo.getFileHandle();
					tmp.setChunkData(blockInfo.getID(),blockInfo.getHexHash(),
						blockInfo.getDevice(),blockInfo.getData());
					tmp.updateChunkVersion(blockInfo.getID());
					
					Network.getInstance().sendMUCCompletedChunk(blockInfo.getName(),blockInfo.getID(),blockInfo.getVersion());
					
					if(tmp.isComplete()){
						Network.getInstance().sendMUCmessage("Completed >> " + tmp.getPath() + " (" + tmp.getSize()
							+ "Bytes) <<");
						Constants.log.addMsg("Completed download: " + blockInfo.getName() + " - Version " + blockInfo.getVersion(),2);
						tmp.trimFile();
						tmp.setUpdating(false);
					}
					
					timeB = System.currentTimeMillis();
				}while(timeB-timeA < 1000);
				
				if(timeB-timeA < 1000){
					Thread.sleep(timeB-timeA);
				}
				//Do requestQueue
				long curTime = System.currentTimeMillis();
				for(ModifyEvent e : Constants.modifyQueue){
					if(curTime - e.getTime() > 2000){
						Constants.requestQueue.offer(new FSRequest(e.getType(),e.getName()));
						Constants.modifyQueue.remove(e);
					}
				}
			}catch(InterruptedException ie){
				interrupt();
				break;
			}
		}
		Constants.log.addMsg("ModifyQueue thread interrupted. Closing...",4);
	}
    
}
