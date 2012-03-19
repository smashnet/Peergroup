/*
* Peergroup - ModifyQueueWorker.java
* 
* Peergroup is a file synching tool using XMPP for data- and 
* participantmanagement and Apache Thrift for direct data-
* exchange between users.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* License: Not for public distribution!
*/

package peergroup;

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
		Constants.log.addMsg("ModifyQueue thread started...",2);
		while(true){
			try{
				Thread.sleep(1000);
				long curTime = System.currentTimeMillis();
				for(ModifyEvent e : Constants.modifyQueue){
					if(curTime - e.getTime() > 1000){
						Constants.requestQueue.offer(new Request(Constants.LOCAL_ENTRY_MODIFY,e.getName()));
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
