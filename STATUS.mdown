#Status
_IN PROGRESS:_ This feature is currently in development in the "development" branch

_PLANNED:_ This feature is in the queue for implementation

_MAYBE:_ I'm not sure if this feature will be implemented (maybe if I'm bored ;-))

##Features

	IN PROGRESS: Implement directory handling
	PLANNED: 	 Save file list to file to prevent hashing on startup (Java File lastModified())
	PLANNED: 	 XMPP getMessage blocks and cannot be interrupted -> need to use stop() to stop thread -> bad
	MAYBE: 		 Hide incomplete files -> not recognized by jpathwatch (don't know if necessary)
	MAYBE: 		 Think of a better approach for merging local file list with remote file list
	
##Known Bugs

* Crashes when using directories in shared folder (FIX IN PROGRESS)