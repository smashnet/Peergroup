/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peergroup;

import java.util.*;

/**
 *
 * @author Nicolas Inden
 */
public class Storage {
    
    public final static String rootDirectory = "./share/";
    public final static String tmpDirectory = "./tmp/";
    
    private LinkedList<FileHandle> files;
    private long shareLimit;
    
}
