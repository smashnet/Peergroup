/*
* Peergroup - ModifyQueueWorker.java
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
						//Network.getInstance().sendMUCmessage("Completed >> " + tmp.getPath() + " (" + tmp.getSize()
						//	+ "Bytes) <<");
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
