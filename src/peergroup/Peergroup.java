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
		// -- Handle SIGINT and SIGTERM
		SignalHandler signalHandler = new SignalHandler() {
			public void handle(Signal signal) {
				Constants.log.addMsg("Caught signal: " + signal + ". Gracefully shutting down!",1);
				Constants.main.interrupt();
				Constants.storage.stopStorageWorker();
				//Constants.network.interrupt();
			}
		};
		Signal.handle(new Signal("TERM"), signalHandler);
		Signal.handle(new Signal("INT"), signalHandler);
		
		// -- Here we go
        Constants.log.addMsg("Starting " + Constants.PROGNAME + " " + Constants.VERSION + "...",2);
        
        getCmdArgs(args);
		
		// -- Create Threads
		Constants.main = new MainWorker();		
		Constants.storage = new StorageWorker();
		Constants.network = new NetworkWorker();
		// -- Start Threads
		Constants.main.start();
		Constants.storage.start();
		//Constants.network.start();
		
		// -- Wait for threads to terminate (only happens through SIGINT/SIGTERM see handler above)
		try{
			Constants.main.join();
			Constants.storage.join();
			//Constants.network.join();
		}catch(InterruptedException ie){
			Constants.log.addMsg("Couldn't wait for all threads to cleanly shut down! Oh what a mess... Bye!",1);
		}
        
        Constants.log.closeLog();
    }
    
	/**
	* Parses the arguments given on the command line
	*
	* @param args the array of commands
	*/
    private static void getCmdArgs(String[] args){
		String last = "";
        for(String s: args){
            if(s.equals("-h") || s.equals("--help")){
				System.out.println(getHelpString());
				System.exit(0);
			}
			if(last.equals("-dir")){
				Constants.rootDirectory = s;
				Constants.log.addMsg("Set share directory to: " + Constants.rootDirectory,3);
			}
			if(last.equals("-jid")){
				String jid[] = s.split("@");
				if(jid.length < 2){
					Constants.log.addMsg("Invalid JID!",1);
					System.exit(1);
				}
				Constants.user = jid[0];
				Constants.server = jid[1];
				Constants.log.addMsg("Set JID to: " + Constants.user + "@" + Constants.server,3);
			}
			if(last.equals("-chan")){
				String conf[] = s.split("@");
				if(conf.length < 2){
					Constants.log.addMsg("Invalid conference channel!",1);
					System.exit(1);
				}
				Constants.conference_channel = conf[0];
				Constants.conference_server = conf[1];
				Constants.log.addMsg("Set conference channel to: " + Constants.conference_channel + "@" 
					+ Constants.conference_server,3);
			}
			if(last.equals("-port")){
				try{
					Constants.port = Integer.parseInt(s);
				}catch(NumberFormatException nan){
					Constants.log.addMsg("Invalid port!",1);
					System.exit(1);
				}
				Constants.log.addMsg("Set port to: " + Constants.port,3);
			}
			if(last.equals("-limit")){
				try{
					Constants.shareLimit = Long.parseLong(s);
				}catch(NumberFormatException nan){
					Constants.log.addMsg("Invalid share limit!",1);
					System.exit(1);
				}
				Constants.log.addMsg("Set share limit to: " + Constants.shareLimit,3);
			}
			last = s;
        }
    }
	
	/**
	* Creates the help string containing all information of how to use this program
	*
	* @return the help string
	*/
	private static String getHelpString(){
		String out = "";
		out += "\t-h\t\t\tprints this help\n";
		out += "\t-dir\t[DIR]\t\tset the shared files directory (default: ./share/)\n";
		out += "\t-jid\t[JID]\t\tset your jabber ID (e.g. foo@jabber.bar.com)\n";
		out += "\t-port\t[JID]\t\tset the XMPP server port (default: 5222)\n";
		out += "\t-chan\t[CHANNEL]\tset the conference channel to join (e.g. foo@conference.jabber.bar.com)\n";
		out += "\t-limit\t[LIMIT]\t\tset the amount of space you want to share in MB (default: 2048MB)\n";
		return out;
	}
    
}