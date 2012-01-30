/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package peergroup;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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
    public void testSomeMethod() throws Exception{
        FileHandle test = new FileHandle("/Users/nico/test.bin");
        
        System.out.print(test.toString());
    }
}
