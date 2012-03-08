/*
* Peergroup - Peergroup.java
* 
* Peergroup is a file synching tool using XMPP for data- and 
* participantmanagement and Apache Thrift for direct data-
* exchange between users.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* License: Not for public distribution!
*
* --- Mainclass ---
*/

package peergroup;

import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * This processes cmd-line args, initializes all needed settings
 * and starts Peergroups mainloop.
 *
 * @author Nicolas Inden
 */
public class Peergroup {
	
	 /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
		SignalHandler signalHandler = new SignalHandler() {
		           public void handle(Signal signal) {
		               Constants.log.addMsg("Caught signal: " + signal + ". Gracefully shutting down!",1);
					   Constants.main.interrupt();
					   Constants.storage.stopStorageWorker();
				   }
		};
		
		Signal.handle(new Signal("TERM"), signalHandler);
		Signal.handle(new Signal("INT"), signalHandler);
        Constants.log.addMsg("Starting " + Constants.PROGNAME + " "
                    + Constants.VERSION + "...",2);
        
        getCmdArgs(args);
		
		Constants.main = new MainWorker();		
		Constants.storage = new StorageWorker();
		Constants.main.start();
		Constants.storage.start();
		
		try{
			Constants.main.join();
			Constants.storage.join();
		}catch(InterruptedException ie){
			Constants.log.addMsg("Waiting for threads to shut down interrupted! Oh what a mess... Bye!",1);
		}
        
        Constants.log.closeLog();
    }
    
    private static void getCmdArgs(String[] args){
        for(String s: args){
            
        }
    }
    
}
