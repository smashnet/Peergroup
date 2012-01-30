/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peergroup;

/**
 * This class is the saving point for all globally used constants
 * and variables.
 * 
 * @author Nicolas Inden
 */
public class Constants {
    
    public final static String PROGNAME    = "peergroup";
    public final static String VERSION     = "0.01 (development version)";
    
    public final static Logger log = new Logger();
    public final static Storage myStorage = new Storage();
    public final static Network myNetwork = new Network();
    
    public final static String rootDirectory = "./share/";
    public final static String tmpDirectory = "./tmp/";
    public static long shareLimit = 2097152;                //2GB in Bytes
    
    public String username;
    public String password;
    public String server;
    public String resource;
    
}
