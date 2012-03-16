/*
* Peergroup - Request.java
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
 * A general request consisting of an ID and the content
 *
 * @author Nicolas Inden
 */
public class Request {
	
	private int id;
	private String content;
    
	public Request(int newID, String newContent){
		this.id = newID;
		this.content = newContent;
	}
	
	public void setID(int newID){
		this.id = newID;
	}
	
	public void setContent(String newContent){
		this.content = newContent;
	}
	
	public int getID(){
		return this.id;
	}
	
	public String getContent(){
		return this.content;
	}
}