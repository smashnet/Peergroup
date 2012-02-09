/*
* Peergroup - Storage.java
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
public class Storage {
    
    private int version;
    private LinkedList<FileHandle> files;
    private long shareLimit;
    
}
