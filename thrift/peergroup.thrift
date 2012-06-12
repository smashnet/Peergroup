/*
* Peergroup - peergroup.thrift
* 
* Peergroup is a P2P Shared Storage System using XMPP for data- and 
* participantmanagement and Apache Thrift for direct data-
* exchange between users.
*
* Author : Nicolas Inden
* Contact: nicolas.inden@rwth-aachen.de
*
* License: Not for public distribution!
*/

namespace cpp peergroup
namespace java peergroup
namespace php peergroup
namespace perl peergroup

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