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
        Constants.log.addMsg("Starting " + Constants.PROGNAME + " "
                    + Constants.VERSION + "...",2);
        
        getCmdArgs(args);
        
        Constants.log.closeLog();
    }
    
    private static void getCmdArgs(String[] args){
        for(String s: args){
            
        }
    }
    
}
