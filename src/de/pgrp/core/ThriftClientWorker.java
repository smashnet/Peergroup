/*
 * Peergroup - ThriftClientWorker.java
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

import java.util.concurrent.*;

/**
 * This thread requests blocks or FileList information from other peers.
 * 
 * @author Nicolas Inden
 */
public class ThriftClientWorker extends Thread {

	private int corePoolSize;
	private int maxPoolSize;
	private long keepAliveTime;
	private ThreadPoolExecutor threadPool;
	private final ArrayBlockingQueue<Runnable> workQueue;

	public ThriftClientWorker() {
		this.corePoolSize = 2;
		this.maxPoolSize = 10;
		this.keepAliveTime = 10;
		this.workQueue = new ArrayBlockingQueue<Runnable>(5000);
		this.threadPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
				keepAliveTime, TimeUnit.SECONDS, workQueue);
	}

	public void stopThriftClientWorker() {
		this.interrupt();
	}

	/**
	 * The run() method
	 */
	@Override
	public void run() {
		this.setName("ThriftClientThreadPool");

		/*
		 * Main loop, takes requests from the queue and processes them
		 */
		while (!isInterrupted()) {
			try {
				FileChunk tmp;
				if ((tmp = Storage.getInstance().getRarestChunk()) != null) {
					tmp.setDownloading(true);
					this.runTask(new ThriftClientGetData(tmp));
				} else {
					Thread.sleep(400);
				}
			} catch (InterruptedException ie) {
				interrupt();
			}
		}
		this.threadPool.shutdown();
		Constants.log.addMsg(
				"ThriftClientThreadPool interrupted/finished. Closing...", 4);
	}

	private void runTask(Runnable task) {
		this.threadPool.execute(task);
	}

	public void stopPoolExecutor() {
		this.interrupt();
	}

}
