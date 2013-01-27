/*
* Peergroup - peergroup.thrift
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

namespace cpp de.pgrp.thrift
namespace java de.pgrp.thrift
namespace php de.pgrp.thrift
namespace perl de.pgrp.thrift

struct ThriftP2PDevice {
	1: string ip,
	2: i32 port,
	3: string jid
}

struct ThriftFileChunk {
	1: i32 chunkID,
	2: i32 blockVersion,
	3: i32 size,
	4: string hash,
	5: list<ThriftP2PDevice> devices
}

struct ThriftFileHandle {
	1: string filename,
	2: i32 fileVersion,
	3: i64 size, 
	4: string hash,
	5: i32 chunkSize,
	6: list<ThriftFileChunk> chunks
}

struct ThriftStorage {
	1: i32 version,
	2: list<ThriftFileHandle> files 
}

service DataTransfer {
	ThriftStorage getStorage(),
	binary getDataBlock(1:string filename, 2:i32 blockID, 3:string hash)
}