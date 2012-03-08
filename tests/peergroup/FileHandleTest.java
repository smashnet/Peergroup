/*
* Peergroup - FileHandleTest.java
* 
* Peergroup is a file synching tool using XMPP for data- and 
* participantmanagement and Apache Thrift for direct data-
* exchange between users.
*
* This file is not part of peergroup, but a junit test for
* peergroup.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* License: Not for public distribution!
*/

package peergroup;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;

/**
 *
 * @author Nicolas Inden
 */
public class FileHandleTest {
    
    public FileHandleTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDivideAndConquerOfFile() throws Exception{
		Constants.log.addMsg("--------------------- Merge File From Chunks Test ----------------------",1);
		//Get random file into the system and create chunks (happens in constructor of FileHandle)
        File myFile = new File(Constants.rootDirectory + "test.bin");
		myFile.createNewFile();
		RandomAccessFile file = new RandomAccessFile(myFile,"rwd");
		file.setLength(1024001);
		file.seek(1024001);
		file.writeByte(9);

		FileHandle test = new FileHandle(myFile);
        System.out.print(test.toString());
		
		Constants.log.addMsg("Recreating file from chunks...",4);
		//Get all the single chunks, recreate the whole file and check hash
        try{
            File tmp = new File("temp/");
			int no_of_items = test.getChunkList().size();
			int i = 0;
            tmp.mkdir();
            tmp = new File("temp/"+test.getFile().getName());
            tmp.createNewFile();
            FileOutputStream outStream = new FileOutputStream(tmp);
            while(i < no_of_items){
				outStream.write(test.getChunkData(i));
				i++;
			}
            outStream.close();
        }catch(IOException ioe){
            Constants.log.addMsg("Error creating chunk:" + ioe, 1);
        }
		
        FileHandle test1 = new FileHandle("temp/test.bin");
        System.out.print(test1.toString());
		if(test.getHexHash().equals(test1.getHexHash())){
			Constants.log.addMsg("SUCCESS!! Hashes match :-)", 2);
		}
    }
	
	@Test
	public void testLocalFileUpdate() throws Exception{
		Constants.log.addMsg("--------------------- Local File Update Test ----------------------",1);
		//Get random file into the system and create chunks (happens in constructor of FileHandle)
        File myFile = new File(Constants.rootDirectory + "test.bin");
		myFile.createNewFile();
		RandomAccessFile file = new RandomAccessFile(myFile,"rwd");
		file.setLength(1024001);
		file.seek(1024001);
		file.writeByte(9);

		FileHandle test = new FileHandle(myFile);
        System.out.print(test.toString());
		
		//Apply change to the file
		RandomAccessFile outStream = new RandomAccessFile(test.getFile(),"rwd");
		outStream.seek(100001);
		outStream.writeByte(5);
		outStream.close();
		
		test.localUpdate();
		System.out.print(test.toString());
		test.getFile().delete();
	}
	
	@Test
	public void testShortenFileUpdate() throws Exception{
		Constants.log.addMsg("--------------------- Shorten File Test ----------------------",1);
		//Get random file into the system and create chunks (happens in constructor of FileHandle)
        File myFile = new File(Constants.rootDirectory + "test.bin");
		myFile.createNewFile();
		RandomAccessFile file = new RandomAccessFile(myFile,"rwd");
		file.setLength(1024001);
		file.seek(1024001);
		file.writeByte(9);

		FileHandle test = new FileHandle(myFile);
        System.out.print(test.toString());		
		
		//Cut the file to 512001 Bytes
		RandomAccessFile outStream = new RandomAccessFile(test.getFile(),"rwd");
		outStream.setLength(512001);
		outStream.close();
		
		test.localUpdate();
		System.out.print(test.toString());
		test.getFile().delete();
	}
	
	@Test
	public void testEnlargeFileUpdate() throws Exception{
		Constants.log.addMsg("--------------------- Enlarge File Test ----------------------",1);
		//Get random file into the system and create chunks (happens in constructor of FileHandle)
        File myFile = new File(Constants.rootDirectory + "test.bin");
		myFile.createNewFile();
		RandomAccessFile file = new RandomAccessFile(myFile,"rwd");
		file.setLength(1024001);
		file.seek(1024001);
		file.writeByte(9);

		FileHandle test = new FileHandle(myFile);
        System.out.print(test.toString());		
		
		//Enlarge the file to 20480001 Bytes (21 Chunks)
		RandomAccessFile outStream = new RandomAccessFile(test.getFile(),"rwd");
		outStream.setLength(2048001);
		outStream.close();
		
		test.localUpdate();
		System.out.print(test.toString());
		test.getFile().delete();
	}
	
}
