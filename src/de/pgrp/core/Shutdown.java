package de.pgrp.core;

import java.util.concurrent.BrokenBarrierException;

public class Shutdown extends Thread{
	
	public void run(){
		Globals.log.addMsg("Caught signal. Gracefully shutting down!", 1);

		if (!Globals.serverMode) {
			if (Globals.storage != null)
				Globals.storage.stopStorageWorker();
		}
		if (Globals.network != null)
			Globals.network.stopNetworkWorker();
		if (Globals.thriftClient != null)
			Globals.thriftClient.stopPoolExecutor();
		if (Globals.enableModQueue) {
			if (Globals.modQueue != null)
				Globals.modQueue.interrupt();
		}
		if (Globals.main != null)
			Globals.main.interrupt();
		
		try {
			Globals.shutdownBarrier.await();
		} catch (InterruptedException ie) {

		} catch (BrokenBarrierException bbe) {
			Globals.log.addMsg(bbe.toString(), 4);
		}
	}
}
