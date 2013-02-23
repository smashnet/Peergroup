package de.pgrp.gui;

import de.pgrp.core.Globals;
import de.pgrp.core.Storage;

/**
 * This class  acquires recent data from the peergroup core, and updates the corresponding
 * GUI elements. 
 * 
 * @author Nicolas Inden
 *
 */
public class RefreshData implements Runnable{

	@Override
	public void run() {
		
		while(true){
			try {
				Thread.sleep(Globals.guiRefreshRate);
				
				//Set folder size
				//Possible improvement: Maintain global variable in Storage rather than count each time
				int sizeInMB = Storage.getInstance().getFolderSize()/1000000;
				//sizeInMB = Math.round(sizeInMB*1000)/1000.0;
				PGTrayIcon.getInstance().setFolderSize(sizeInMB);
			} catch (InterruptedException e) {
				
			}
		}
	}

}
