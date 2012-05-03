/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

# Thrift Tutorial
# Mark Slee (mcslee@facebook.com)
#
# This file aims to teach you how to use Thrift, in a .thrift file. Neato. The
# first thing to notice is that .thrift files support standard shell comments.
# This lets you make your thrift file executable and include your Thrift build
# step on the top line. And you can place comments like this anywhere you like.
#
# Before running this file, you will need to have installed the thrift compiler
# into /usr/local/bin.

/**
 * The first thing to know about are types. The available types in Thrift are:
 *
 *  bool        Boolean, one byte
 *  byte        Signed byte
 *  i16         Signed 16-bit integer
 *  i32         Signed 32-bit integer
 *  i64         Signed 64-bit integer
 *  double      64-bit floating point value
 *  string      String
 *  binary      Blob (byte array)
 *  map<t1,t2>  Map from one type to another
 *  list<t1>    Ordered list of one type
 *  set<t1>     Set of unique elements of one type
 *
 * Did you also notice that Thrift supports C style comments?
 */

// Just in case you were wondering... yes. We support simple C comments too.

/**
 * Thrift files can namespace, package, or prefix their output in various
 * target languages.
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
	2: string hash,
	3: i32 blockVersion,
	4: list<ThriftP2PDevice> devices
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