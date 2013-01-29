package de.pgrp.core;

import java.util.concurrent.BrokenBarrierException;

public class Shutdown extends Thread{
	
	public void run(){
		Constants.log.addMsg("Caught signal. Gracefully shutting down!", 1);

		if (!Constants.serverMode) {
			if (Constants.storage != null)
				Constants.storage.stopStorageWorker();
		}
		if (Constants.network != null)
			Constants.network.stopNetworkWorker();
		if (Constants.thriftClient != null)
			Constants.thriftClient.stopPoolExecutor();
		if (Constants.enableModQueue) {
			if (Constants.modQueue != null)
				Constants.modQueue.interrupt();
		}
		if (Constants.main != null)
			Constants.main.interrupt();
		
		try {
			Constants.shutdownBarrier.await();
		} catch (InterruptedException ie) {

		} catch (BrokenBarrierException bbe) {
			Constants.log.addMsg(bbe.toString(), 4);
		}
	}
}
