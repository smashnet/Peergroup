/*
 * Peergroup - DelayQueueWorker.java
 * 
 * This file is part of Peergroup.
 *
 * Peergroup is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Peergroup is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Author : Nicolas Inden
 * Contact: nicolas.inden@rwth-aachen.de
 *
 * Copyright (c) 2013 Nicolas Inden
 */

package de.pgrp.core;

import java.util.concurrent.TimeUnit;

/**
 * This thread regularily checks the FileEventQueue if files haven't been
 * updated for more than one second. In this case a modify request is put in the
 * main queue.
 * 
 * @author Nicolas Inden
 */
public class DelayQueueWorker extends Thread {

	/**
	 * Creates a DelayQueueWorker.
	 */
	public DelayQueueWorker() {
	}

	/**
	 * The run() method
	 */
	@Override
	public void run() {
		this.setName("ModifyQueue Thread");
		Globals.log.addMsg("ModifyQueue thread started...");
		while (!isInterrupted()) {
			try {
				// Process data from storeQueue
				long timeA, timeB;
				timeA = System.currentTimeMillis();
				do {
					StoreBlock blockInfo = Globals.storeQueue.poll(100, TimeUnit.MILLISECONDS);
					if (blockInfo == null) {
						timeB = System.currentTimeMillis();
						continue;
					}
					FileHandle tmp = blockInfo.getFileHandle();
					tmp.setChunkData(blockInfo.getID(), blockInfo.getHexHash(), blockInfo.getDevice(), blockInfo.getData());
					tmp.updateChunkVersion(blockInfo.getID());

					Network.getInstance().sendMUCCompletedChunk(blockInfo.getName(), blockInfo.getID(), blockInfo.getVersion());

					if (tmp.isComplete()) {
						Globals.log.addMsg("Completed download: " + blockInfo.getName() + " - Version " + blockInfo.getVersion(), 2);
						tmp.trimFile();
						tmp.setUpdating(false);
						
						P2Pdevice me = P2Pdevice.getDevice(Globals.getJID(), Globals.remoteIP4, Globals.localIP4, Globals.p2pPort);
						Storage.getInstance().addP2PdeviceToFile(blockInfo.getName(), blockInfo.getVersion(), me);
						//Debug:
						//Globals.log.addMsg(tmp.toString());
					}

					timeB = System.currentTimeMillis();
				} while (timeB - timeA < 1000);

				if (timeB - timeA < 1000) {
					Thread.sleep(timeB - timeA);
				}
				// Do requestQueue
				long curTime = System.currentTimeMillis();
				/*
				 * Take each file-change (FileEvent) from the modifyQueue that
				 * is older than 2 seconds and enqueue it in the requestQueue.
				 * The heuristic should prevent that modify events are handled
				 * while the file is still not complete (eg while copying a file
				 * into the shared folder)
				 */
				for (FileEvent e : Globals.delayQueue) {
					if (curTime - e.getTime() > 2000) {
						Globals.requestQueue.offer(new FSRequest(e.getType(), e.getName()));
						Globals.delayQueue.remove(e);
					}
				}
			} catch (InterruptedException ie) {
				interrupt();
				break;
			}
		}
		Globals.log.addMsg("ModifyQueue thread interrupted. Closing...", 4);
	}

}
