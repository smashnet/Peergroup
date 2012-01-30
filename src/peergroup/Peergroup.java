/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peergroup;

/**
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
