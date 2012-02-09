/*
* Peergroup - FileChunk.java
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

import java.util.LinkedList;

/**
 *
 * @author Nicolas Inden
 */
public class FileChunk {
    
    private int id;
    private byte[] hash;
    private String path;
    private LinkedList<P2Pdevice> peers;
    
    public FileChunk(){
        
    }
    
    public FileChunk(int no, byte[] digest){
        this.id = no;
        this.hash = digest;
    }
    
}
