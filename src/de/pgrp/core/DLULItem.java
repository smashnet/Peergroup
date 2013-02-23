package de.pgrp.core;

public class DLULItem {

		/**
		 * The relative path to the corresponding file
		 */
		private String path;
		/**
		 * The chunk ID
		 */
		private int chunk;
		/**
		 * The currentTimeMillis() value when the down- or upload started
		 */
		private long startTime;
		
		public DLULItem(String path, int id){
			this.path = path;
			this.chunk = id;
			this.startTime = System.currentTimeMillis();
		}
		
		public boolean equals(DLULItem myitem){
			if(!this.path.equals(myitem.getPath()))
				return false;
			if(!(this.chunk == myitem.getChunkID()))
				return false;
			
			return true;
		}

		public String getPath(){
			return path;
		}

		public int getChunkID(){
			return chunk;
		}
		
		public long getStartTime(){
			return startTime;
		}
}
