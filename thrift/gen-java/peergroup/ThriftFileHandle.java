/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package peergroup;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThriftFileHandle implements org.apache.thrift.TBase<ThriftFileHandle, ThriftFileHandle._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ThriftFileHandle");

  private static final org.apache.thrift.protocol.TField FILENAME_FIELD_DESC = new org.apache.thrift.protocol.TField("filename", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField FILE_VERSION_FIELD_DESC = new org.apache.thrift.protocol.TField("fileVersion", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField SIZE_FIELD_DESC = new org.apache.thrift.protocol.TField("size", org.apache.thrift.protocol.TType.I64, (short)3);
  private static final org.apache.thrift.protocol.TField HASH_FIELD_DESC = new org.apache.thrift.protocol.TField("hash", org.apache.thrift.protocol.TType.STRING, (short)4);
  private static final org.apache.thrift.protocol.TField CHUNK_SIZE_FIELD_DESC = new org.apache.thrift.protocol.TField("chunkSize", org.apache.thrift.protocol.TType.I32, (short)5);
  private static final org.apache.thrift.protocol.TField CHUNKS_FIELD_DESC = new org.apache.thrift.protocol.TField("chunks", org.apache.thrift.protocol.TType.LIST, (short)6);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ThriftFileHandleStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ThriftFileHandleTupleSchemeFactory());
  }

  public String filename; // required
  public int fileVersion; // required
  public long size; // required
  public String hash; // required
  public int chunkSize; // required
  public List<ThriftFileChunk> chunks; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    FILENAME((short)1, "filename"),
    FILE_VERSION((short)2, "fileVersion"),
    SIZE((short)3, "size"),
    HASH((short)4, "hash"),
    CHUNK_SIZE((short)5, "chunkSize"),
    CHUNKS((short)6, "chunks");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // FILENAME
          return FILENAME;
        case 2: // FILE_VERSION
          return FILE_VERSION;
        case 3: // SIZE
          return SIZE;
        case 4: // HASH
          return HASH;
        case 5: // CHUNK_SIZE
          return CHUNK_SIZE;
        case 6: // CHUNKS
          return CHUNKS;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __FILEVERSION_ISSET_ID = 0;
  private static final int __SIZE_ISSET_ID = 1;
  private static final int __CHUNKSIZE_ISSET_ID = 2;
  private BitSet __isset_bit_vector = new BitSet(3);
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.FILENAME, new org.apache.thrift.meta_data.FieldMetaData("filename", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.FILE_VERSION, new org.apache.thrift.meta_data.FieldMetaData("fileVersion", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.SIZE, new org.apache.thrift.meta_data.FieldMetaData("size", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.HASH, new org.apache.thrift.meta_data.FieldMetaData("hash", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CHUNK_SIZE, new org.apache.thrift.meta_data.FieldMetaData("chunkSize", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.CHUNKS, new org.apache.thrift.meta_data.FieldMetaData("chunks", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ThriftFileChunk.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ThriftFileHandle.class, metaDataMap);
  }

  public ThriftFileHandle() {
  }

  public ThriftFileHandle(
    String filename,
    int fileVersion,
    long size,
    String hash,
    int chunkSize,
    List<ThriftFileChunk> chunks)
  {
    this();
    this.filename = filename;
    this.fileVersion = fileVersion;
    setFileVersionIsSet(true);
    this.size = size;
    setSizeIsSet(true);
    this.hash = hash;
    this.chunkSize = chunkSize;
    setChunkSizeIsSet(true);
    this.chunks = chunks;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ThriftFileHandle(ThriftFileHandle other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetFilename()) {
      this.filename = other.filename;
    }
    this.fileVersion = other.fileVersion;
    this.size = other.size;
    if (other.isSetHash()) {
      this.hash = other.hash;
    }
    this.chunkSize = other.chunkSize;
    if (other.isSetChunks()) {
      List<ThriftFileChunk> __this__chunks = new ArrayList<ThriftFileChunk>();
      for (ThriftFileChunk other_element : other.chunks) {
        __this__chunks.add(new ThriftFileChunk(other_element));
      }
      this.chunks = __this__chunks;
    }
  }

  public ThriftFileHandle deepCopy() {
    return new ThriftFileHandle(this);
  }

  @Override
  public void clear() {
    this.filename = null;
    setFileVersionIsSet(false);
    this.fileVersion = 0;
    setSizeIsSet(false);
    this.size = 0;
    this.hash = null;
    setChunkSizeIsSet(false);
    this.chunkSize = 0;
    this.chunks = null;
  }

  public String getFilename() {
    return this.filename;
  }

  public ThriftFileHandle setFilename(String filename) {
    this.filename = filename;
    return this;
  }

  public void unsetFilename() {
    this.filename = null;
  }

  /** Returns true if field filename is set (has been assigned a value) and false otherwise */
  public boolean isSetFilename() {
    return this.filename != null;
  }

  public void setFilenameIsSet(boolean value) {
    if (!value) {
      this.filename = null;
    }
  }

  public int getFileVersion() {
    return this.fileVersion;
  }

  public ThriftFileHandle setFileVersion(int fileVersion) {
    this.fileVersion = fileVersion;
    setFileVersionIsSet(true);
    return this;
  }

  public void unsetFileVersion() {
    __isset_bit_vector.clear(__FILEVERSION_ISSET_ID);
  }

  /** Returns true if field fileVersion is set (has been assigned a value) and false otherwise */
  public boolean isSetFileVersion() {
    return __isset_bit_vector.get(__FILEVERSION_ISSET_ID);
  }

  public void setFileVersionIsSet(boolean value) {
    __isset_bit_vector.set(__FILEVERSION_ISSET_ID, value);
  }

  public long getSize() {
    return this.size;
  }

  public ThriftFileHandle setSize(long size) {
    this.size = size;
    setSizeIsSet(true);
    return this;
  }

  public void unsetSize() {
    __isset_bit_vector.clear(__SIZE_ISSET_ID);
  }

  /** Returns true if field size is set (has been assigned a value) and false otherwise */
  public boolean isSetSize() {
    return __isset_bit_vector.get(__SIZE_ISSET_ID);
  }

  public void setSizeIsSet(boolean value) {
    __isset_bit_vector.set(__SIZE_ISSET_ID, value);
  }

  public String getHash() {
    return this.hash;
  }

  public ThriftFileHandle setHash(String hash) {
    this.hash = hash;
    return this;
  }

  public void unsetHash() {
    this.hash = null;
  }

  /** Returns true if field hash is set (has been assigned a value) and false otherwise */
  public boolean isSetHash() {
    return this.hash != null;
  }

  public void setHashIsSet(boolean value) {
    if (!value) {
      this.hash = null;
    }
  }

  public int getChunkSize() {
    return this.chunkSize;
  }

  public ThriftFileHandle setChunkSize(int chunkSize) {
    this.chunkSize = chunkSize;
    setChunkSizeIsSet(true);
    return this;
  }

  public void unsetChunkSize() {
    __isset_bit_vector.clear(__CHUNKSIZE_ISSET_ID);
  }

  /** Returns true if field chunkSize is set (has been assigned a value) and false otherwise */
  public boolean isSetChunkSize() {
    return __isset_bit_vector.get(__CHUNKSIZE_ISSET_ID);
  }

  public void setChunkSizeIsSet(boolean value) {
    __isset_bit_vector.set(__CHUNKSIZE_ISSET_ID, value);
  }

  public int getChunksSize() {
    return (this.chunks == null) ? 0 : this.chunks.size();
  }

  public java.util.Iterator<ThriftFileChunk> getChunksIterator() {
    return (this.chunks == null) ? null : this.chunks.iterator();
  }

  public void addToChunks(ThriftFileChunk elem) {
    if (this.chunks == null) {
      this.chunks = new ArrayList<ThriftFileChunk>();
    }
    this.chunks.add(elem);
  }

  public List<ThriftFileChunk> getChunks() {
    return this.chunks;
  }

  public ThriftFileHandle setChunks(List<ThriftFileChunk> chunks) {
    this.chunks = chunks;
    return this;
  }

  public void unsetChunks() {
    this.chunks = null;
  }

  /** Returns true if field chunks is set (has been assigned a value) and false otherwise */
  public boolean isSetChunks() {
    return this.chunks != null;
  }

  public void setChunksIsSet(boolean value) {
    if (!value) {
      this.chunks = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case FILENAME:
      if (value == null) {
        unsetFilename();
      } else {
        setFilename((String)value);
      }
      break;

    case FILE_VERSION:
      if (value == null) {
        unsetFileVersion();
      } else {
        setFileVersion((Integer)value);
      }
      break;

    case SIZE:
      if (value == null) {
        unsetSize();
      } else {
        setSize((Long)value);
      }
      break;

    case HASH:
      if (value == null) {
        unsetHash();
      } else {
        setHash((String)value);
      }
      break;

    case CHUNK_SIZE:
      if (value == null) {
        unsetChunkSize();
      } else {
        setChunkSize((Integer)value);
      }
      break;

    case CHUNKS:
      if (value == null) {
        unsetChunks();
      } else {
        setChunks((List<ThriftFileChunk>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case FILENAME:
      return getFilename();

    case FILE_VERSION:
      return Integer.valueOf(getFileVersion());

    case SIZE:
      return Long.valueOf(getSize());

    case HASH:
      return getHash();

    case CHUNK_SIZE:
      return Integer.valueOf(getChunkSize());

    case CHUNKS:
      return getChunks();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case FILENAME:
      return isSetFilename();
    case FILE_VERSION:
      return isSetFileVersion();
    case SIZE:
      return isSetSize();
    case HASH:
      return isSetHash();
    case CHUNK_SIZE:
      return isSetChunkSize();
    case CHUNKS:
      return isSetChunks();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ThriftFileHandle)
      return this.equals((ThriftFileHandle)that);
    return false;
  }

  public boolean equals(ThriftFileHandle that) {
    if (that == null)
      return false;

    boolean this_present_filename = true && this.isSetFilename();
    boolean that_present_filename = true && that.isSetFilename();
    if (this_present_filename || that_present_filename) {
      if (!(this_present_filename && that_present_filename))
        return false;
      if (!this.filename.equals(that.filename))
        return false;
    }

    boolean this_present_fileVersion = true;
    boolean that_present_fileVersion = true;
    if (this_present_fileVersion || that_present_fileVersion) {
      if (!(this_present_fileVersion && that_present_fileVersion))
        return false;
      if (this.fileVersion != that.fileVersion)
        return false;
    }

    boolean this_present_size = true;
    boolean that_present_size = true;
    if (this_present_size || that_present_size) {
      if (!(this_present_size && that_present_size))
        return false;
      if (this.size != that.size)
        return false;
    }

    boolean this_present_hash = true && this.isSetHash();
    boolean that_present_hash = true && that.isSetHash();
    if (this_present_hash || that_present_hash) {
      if (!(this_present_hash && that_present_hash))
        return false;
      if (!this.hash.equals(that.hash))
        return false;
    }

    boolean this_present_chunkSize = true;
    boolean that_present_chunkSize = true;
    if (this_present_chunkSize || that_present_chunkSize) {
      if (!(this_present_chunkSize && that_present_chunkSize))
        return false;
      if (this.chunkSize != that.chunkSize)
        return false;
    }

    boolean this_present_chunks = true && this.isSetChunks();
    boolean that_present_chunks = true && that.isSetChunks();
    if (this_present_chunks || that_present_chunks) {
      if (!(this_present_chunks && that_present_chunks))
        return false;
      if (!this.chunks.equals(that.chunks))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(ThriftFileHandle other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    ThriftFileHandle typedOther = (ThriftFileHandle)other;

    lastComparison = Boolean.valueOf(isSetFilename()).compareTo(typedOther.isSetFilename());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFilename()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.filename, typedOther.filename);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetFileVersion()).compareTo(typedOther.isSetFileVersion());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFileVersion()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.fileVersion, typedOther.fileVersion);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSize()).compareTo(typedOther.isSetSize());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSize()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.size, typedOther.size);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetHash()).compareTo(typedOther.isSetHash());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetHash()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.hash, typedOther.hash);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetChunkSize()).compareTo(typedOther.isSetChunkSize());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetChunkSize()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.chunkSize, typedOther.chunkSize);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetChunks()).compareTo(typedOther.isSetChunks());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetChunks()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.chunks, typedOther.chunks);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ThriftFileHandle(");
    boolean first = true;

    sb.append("filename:");
    if (this.filename == null) {
      sb.append("null");
    } else {
      sb.append(this.filename);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("fileVersion:");
    sb.append(this.fileVersion);
    first = false;
    if (!first) sb.append(", ");
    sb.append("size:");
    sb.append(this.size);
    first = false;
    if (!first) sb.append(", ");
    sb.append("hash:");
    if (this.hash == null) {
      sb.append("null");
    } else {
      sb.append(this.hash);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("chunkSize:");
    sb.append(this.chunkSize);
    first = false;
    if (!first) sb.append(", ");
    sb.append("chunks:");
    if (this.chunks == null) {
      sb.append("null");
    } else {
      sb.append(this.chunks);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class ThriftFileHandleStandardSchemeFactory implements SchemeFactory {
    public ThriftFileHandleStandardScheme getScheme() {
      return new ThriftFileHandleStandardScheme();
    }
  }

  private static class ThriftFileHandleStandardScheme extends StandardScheme<ThriftFileHandle> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ThriftFileHandle struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // FILENAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.filename = iprot.readString();
              struct.setFilenameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // FILE_VERSION
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.fileVersion = iprot.readI32();
              struct.setFileVersionIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // SIZE
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.size = iprot.readI64();
              struct.setSizeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // HASH
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.hash = iprot.readString();
              struct.setHashIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // CHUNK_SIZE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.chunkSize = iprot.readI32();
              struct.setChunkSizeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // CHUNKS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list8 = iprot.readListBegin();
                struct.chunks = new ArrayList<ThriftFileChunk>(_list8.size);
                for (int _i9 = 0; _i9 < _list8.size; ++_i9)
                {
                  ThriftFileChunk _elem10; // optional
                  _elem10 = new ThriftFileChunk();
                  _elem10.read(iprot);
                  struct.chunks.add(_elem10);
                }
                iprot.readListEnd();
              }
              struct.setChunksIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, ThriftFileHandle struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.filename != null) {
        oprot.writeFieldBegin(FILENAME_FIELD_DESC);
        oprot.writeString(struct.filename);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(FILE_VERSION_FIELD_DESC);
      oprot.writeI32(struct.fileVersion);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(SIZE_FIELD_DESC);
      oprot.writeI64(struct.size);
      oprot.writeFieldEnd();
      if (struct.hash != null) {
        oprot.writeFieldBegin(HASH_FIELD_DESC);
        oprot.writeString(struct.hash);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(CHUNK_SIZE_FIELD_DESC);
      oprot.writeI32(struct.chunkSize);
      oprot.writeFieldEnd();
      if (struct.chunks != null) {
        oprot.writeFieldBegin(CHUNKS_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.chunks.size()));
          for (ThriftFileChunk _iter11 : struct.chunks)
          {
            _iter11.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ThriftFileHandleTupleSchemeFactory implements SchemeFactory {
    public ThriftFileHandleTupleScheme getScheme() {
      return new ThriftFileHandleTupleScheme();
    }
  }

  private static class ThriftFileHandleTupleScheme extends TupleScheme<ThriftFileHandle> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ThriftFileHandle struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetFilename()) {
        optionals.set(0);
      }
      if (struct.isSetFileVersion()) {
        optionals.set(1);
      }
      if (struct.isSetSize()) {
        optionals.set(2);
      }
      if (struct.isSetHash()) {
        optionals.set(3);
      }
      if (struct.isSetChunkSize()) {
        optionals.set(4);
      }
      if (struct.isSetChunks()) {
        optionals.set(5);
      }
      oprot.writeBitSet(optionals, 6);
      if (struct.isSetFilename()) {
        oprot.writeString(struct.filename);
      }
      if (struct.isSetFileVersion()) {
        oprot.writeI32(struct.fileVersion);
      }
      if (struct.isSetSize()) {
        oprot.writeI64(struct.size);
      }
      if (struct.isSetHash()) {
        oprot.writeString(struct.hash);
      }
      if (struct.isSetChunkSize()) {
        oprot.writeI32(struct.chunkSize);
      }
      if (struct.isSetChunks()) {
        {
          oprot.writeI32(struct.chunks.size());
          for (ThriftFileChunk _iter12 : struct.chunks)
          {
            _iter12.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ThriftFileHandle struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(6);
      if (incoming.get(0)) {
        struct.filename = iprot.readString();
        struct.setFilenameIsSet(true);
      }
      if (incoming.get(1)) {
        struct.fileVersion = iprot.readI32();
        struct.setFileVersionIsSet(true);
      }
      if (incoming.get(2)) {
        struct.size = iprot.readI64();
        struct.setSizeIsSet(true);
      }
      if (incoming.get(3)) {
        struct.hash = iprot.readString();
        struct.setHashIsSet(true);
      }
      if (incoming.get(4)) {
        struct.chunkSize = iprot.readI32();
        struct.setChunkSizeIsSet(true);
      }
      if (incoming.get(5)) {
        {
          org.apache.thrift.protocol.TList _list13 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.chunks = new ArrayList<ThriftFileChunk>(_list13.size);
          for (int _i14 = 0; _i14 < _list13.size; ++_i14)
          {
            ThriftFileChunk _elem15; // optional
            _elem15 = new ThriftFileChunk();
            _elem15.read(iprot);
            struct.chunks.add(_elem15);
          }
        }
        struct.setChunksIsSet(true);
      }
    }
  }

}
