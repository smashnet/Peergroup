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

public class ThriftFileChunk implements org.apache.thrift.TBase<ThriftFileChunk, ThriftFileChunk._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("ThriftFileChunk");

  private static final org.apache.thrift.protocol.TField CHUNK_ID_FIELD_DESC = new org.apache.thrift.protocol.TField("chunkID", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField HASH_FIELD_DESC = new org.apache.thrift.protocol.TField("hash", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField BLOCK_VERSION_FIELD_DESC = new org.apache.thrift.protocol.TField("blockVersion", org.apache.thrift.protocol.TType.I32, (short)3);
  private static final org.apache.thrift.protocol.TField DEVICES_FIELD_DESC = new org.apache.thrift.protocol.TField("devices", org.apache.thrift.protocol.TType.LIST, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new ThriftFileChunkStandardSchemeFactory());
    schemes.put(TupleScheme.class, new ThriftFileChunkTupleSchemeFactory());
  }

  public int chunkID; // required
  public String hash; // required
  public int blockVersion; // required
  public List<ThriftP2PDevice> devices; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    CHUNK_ID((short)1, "chunkID"),
    HASH((short)2, "hash"),
    BLOCK_VERSION((short)3, "blockVersion"),
    DEVICES((short)4, "devices");

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
        case 1: // CHUNK_ID
          return CHUNK_ID;
        case 2: // HASH
          return HASH;
        case 3: // BLOCK_VERSION
          return BLOCK_VERSION;
        case 4: // DEVICES
          return DEVICES;
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
  private static final int __CHUNKID_ISSET_ID = 0;
  private static final int __BLOCKVERSION_ISSET_ID = 1;
  private BitSet __isset_bit_vector = new BitSet(2);
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.CHUNK_ID, new org.apache.thrift.meta_data.FieldMetaData("chunkID", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.HASH, new org.apache.thrift.meta_data.FieldMetaData("hash", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.BLOCK_VERSION, new org.apache.thrift.meta_data.FieldMetaData("blockVersion", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.DEVICES, new org.apache.thrift.meta_data.FieldMetaData("devices", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, ThriftP2PDevice.class))));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(ThriftFileChunk.class, metaDataMap);
  }

  public ThriftFileChunk() {
  }

  public ThriftFileChunk(
    int chunkID,
    String hash,
    int blockVersion,
    List<ThriftP2PDevice> devices)
  {
    this();
    this.chunkID = chunkID;
    setChunkIDIsSet(true);
    this.hash = hash;
    this.blockVersion = blockVersion;
    setBlockVersionIsSet(true);
    this.devices = devices;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public ThriftFileChunk(ThriftFileChunk other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    this.chunkID = other.chunkID;
    if (other.isSetHash()) {
      this.hash = other.hash;
    }
    this.blockVersion = other.blockVersion;
    if (other.isSetDevices()) {
      List<ThriftP2PDevice> __this__devices = new ArrayList<ThriftP2PDevice>();
      for (ThriftP2PDevice other_element : other.devices) {
        __this__devices.add(new ThriftP2PDevice(other_element));
      }
      this.devices = __this__devices;
    }
  }

  public ThriftFileChunk deepCopy() {
    return new ThriftFileChunk(this);
  }

  @Override
  public void clear() {
    setChunkIDIsSet(false);
    this.chunkID = 0;
    this.hash = null;
    setBlockVersionIsSet(false);
    this.blockVersion = 0;
    this.devices = null;
  }

  public int getChunkID() {
    return this.chunkID;
  }

  public ThriftFileChunk setChunkID(int chunkID) {
    this.chunkID = chunkID;
    setChunkIDIsSet(true);
    return this;
  }

  public void unsetChunkID() {
    __isset_bit_vector.clear(__CHUNKID_ISSET_ID);
  }

  /** Returns true if field chunkID is set (has been assigned a value) and false otherwise */
  public boolean isSetChunkID() {
    return __isset_bit_vector.get(__CHUNKID_ISSET_ID);
  }

  public void setChunkIDIsSet(boolean value) {
    __isset_bit_vector.set(__CHUNKID_ISSET_ID, value);
  }

  public String getHash() {
    return this.hash;
  }

  public ThriftFileChunk setHash(String hash) {
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

  public int getBlockVersion() {
    return this.blockVersion;
  }

  public ThriftFileChunk setBlockVersion(int blockVersion) {
    this.blockVersion = blockVersion;
    setBlockVersionIsSet(true);
    return this;
  }

  public void unsetBlockVersion() {
    __isset_bit_vector.clear(__BLOCKVERSION_ISSET_ID);
  }

  /** Returns true if field blockVersion is set (has been assigned a value) and false otherwise */
  public boolean isSetBlockVersion() {
    return __isset_bit_vector.get(__BLOCKVERSION_ISSET_ID);
  }

  public void setBlockVersionIsSet(boolean value) {
    __isset_bit_vector.set(__BLOCKVERSION_ISSET_ID, value);
  }

  public int getDevicesSize() {
    return (this.devices == null) ? 0 : this.devices.size();
  }

  public java.util.Iterator<ThriftP2PDevice> getDevicesIterator() {
    return (this.devices == null) ? null : this.devices.iterator();
  }

  public void addToDevices(ThriftP2PDevice elem) {
    if (this.devices == null) {
      this.devices = new ArrayList<ThriftP2PDevice>();
    }
    this.devices.add(elem);
  }

  public List<ThriftP2PDevice> getDevices() {
    return this.devices;
  }

  public ThriftFileChunk setDevices(List<ThriftP2PDevice> devices) {
    this.devices = devices;
    return this;
  }

  public void unsetDevices() {
    this.devices = null;
  }

  /** Returns true if field devices is set (has been assigned a value) and false otherwise */
  public boolean isSetDevices() {
    return this.devices != null;
  }

  public void setDevicesIsSet(boolean value) {
    if (!value) {
      this.devices = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case CHUNK_ID:
      if (value == null) {
        unsetChunkID();
      } else {
        setChunkID((Integer)value);
      }
      break;

    case HASH:
      if (value == null) {
        unsetHash();
      } else {
        setHash((String)value);
      }
      break;

    case BLOCK_VERSION:
      if (value == null) {
        unsetBlockVersion();
      } else {
        setBlockVersion((Integer)value);
      }
      break;

    case DEVICES:
      if (value == null) {
        unsetDevices();
      } else {
        setDevices((List<ThriftP2PDevice>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case CHUNK_ID:
      return Integer.valueOf(getChunkID());

    case HASH:
      return getHash();

    case BLOCK_VERSION:
      return Integer.valueOf(getBlockVersion());

    case DEVICES:
      return getDevices();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case CHUNK_ID:
      return isSetChunkID();
    case HASH:
      return isSetHash();
    case BLOCK_VERSION:
      return isSetBlockVersion();
    case DEVICES:
      return isSetDevices();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof ThriftFileChunk)
      return this.equals((ThriftFileChunk)that);
    return false;
  }

  public boolean equals(ThriftFileChunk that) {
    if (that == null)
      return false;

    boolean this_present_chunkID = true;
    boolean that_present_chunkID = true;
    if (this_present_chunkID || that_present_chunkID) {
      if (!(this_present_chunkID && that_present_chunkID))
        return false;
      if (this.chunkID != that.chunkID)
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

    boolean this_present_blockVersion = true;
    boolean that_present_blockVersion = true;
    if (this_present_blockVersion || that_present_blockVersion) {
      if (!(this_present_blockVersion && that_present_blockVersion))
        return false;
      if (this.blockVersion != that.blockVersion)
        return false;
    }

    boolean this_present_devices = true && this.isSetDevices();
    boolean that_present_devices = true && that.isSetDevices();
    if (this_present_devices || that_present_devices) {
      if (!(this_present_devices && that_present_devices))
        return false;
      if (!this.devices.equals(that.devices))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(ThriftFileChunk other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    ThriftFileChunk typedOther = (ThriftFileChunk)other;

    lastComparison = Boolean.valueOf(isSetChunkID()).compareTo(typedOther.isSetChunkID());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetChunkID()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.chunkID, typedOther.chunkID);
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
    lastComparison = Boolean.valueOf(isSetBlockVersion()).compareTo(typedOther.isSetBlockVersion());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetBlockVersion()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.blockVersion, typedOther.blockVersion);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDevices()).compareTo(typedOther.isSetDevices());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDevices()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.devices, typedOther.devices);
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
    StringBuilder sb = new StringBuilder("ThriftFileChunk(");
    boolean first = true;

    sb.append("chunkID:");
    sb.append(this.chunkID);
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
    sb.append("blockVersion:");
    sb.append(this.blockVersion);
    first = false;
    if (!first) sb.append(", ");
    sb.append("devices:");
    if (this.devices == null) {
      sb.append("null");
    } else {
      sb.append(this.devices);
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

  private static class ThriftFileChunkStandardSchemeFactory implements SchemeFactory {
    public ThriftFileChunkStandardScheme getScheme() {
      return new ThriftFileChunkStandardScheme();
    }
  }

  private static class ThriftFileChunkStandardScheme extends StandardScheme<ThriftFileChunk> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, ThriftFileChunk struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // CHUNK_ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.chunkID = iprot.readI32();
              struct.setChunkIDIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // HASH
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.hash = iprot.readString();
              struct.setHashIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // BLOCK_VERSION
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.blockVersion = iprot.readI32();
              struct.setBlockVersionIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // DEVICES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list0 = iprot.readListBegin();
                struct.devices = new ArrayList<ThriftP2PDevice>(_list0.size);
                for (int _i1 = 0; _i1 < _list0.size; ++_i1)
                {
                  ThriftP2PDevice _elem2; // optional
                  _elem2 = new ThriftP2PDevice();
                  _elem2.read(iprot);
                  struct.devices.add(_elem2);
                }
                iprot.readListEnd();
              }
              struct.setDevicesIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, ThriftFileChunk struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(CHUNK_ID_FIELD_DESC);
      oprot.writeI32(struct.chunkID);
      oprot.writeFieldEnd();
      if (struct.hash != null) {
        oprot.writeFieldBegin(HASH_FIELD_DESC);
        oprot.writeString(struct.hash);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(BLOCK_VERSION_FIELD_DESC);
      oprot.writeI32(struct.blockVersion);
      oprot.writeFieldEnd();
      if (struct.devices != null) {
        oprot.writeFieldBegin(DEVICES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.devices.size()));
          for (ThriftP2PDevice _iter3 : struct.devices)
          {
            _iter3.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class ThriftFileChunkTupleSchemeFactory implements SchemeFactory {
    public ThriftFileChunkTupleScheme getScheme() {
      return new ThriftFileChunkTupleScheme();
    }
  }

  private static class ThriftFileChunkTupleScheme extends TupleScheme<ThriftFileChunk> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, ThriftFileChunk struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetChunkID()) {
        optionals.set(0);
      }
      if (struct.isSetHash()) {
        optionals.set(1);
      }
      if (struct.isSetBlockVersion()) {
        optionals.set(2);
      }
      if (struct.isSetDevices()) {
        optionals.set(3);
      }
      oprot.writeBitSet(optionals, 4);
      if (struct.isSetChunkID()) {
        oprot.writeI32(struct.chunkID);
      }
      if (struct.isSetHash()) {
        oprot.writeString(struct.hash);
      }
      if (struct.isSetBlockVersion()) {
        oprot.writeI32(struct.blockVersion);
      }
      if (struct.isSetDevices()) {
        {
          oprot.writeI32(struct.devices.size());
          for (ThriftP2PDevice _iter4 : struct.devices)
          {
            _iter4.write(oprot);
          }
        }
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, ThriftFileChunk struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(4);
      if (incoming.get(0)) {
        struct.chunkID = iprot.readI32();
        struct.setChunkIDIsSet(true);
      }
      if (incoming.get(1)) {
        struct.hash = iprot.readString();
        struct.setHashIsSet(true);
      }
      if (incoming.get(2)) {
        struct.blockVersion = iprot.readI32();
        struct.setBlockVersionIsSet(true);
      }
      if (incoming.get(3)) {
        {
          org.apache.thrift.protocol.TList _list5 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.devices = new ArrayList<ThriftP2PDevice>(_list5.size);
          for (int _i6 = 0; _i6 < _list5.size; ++_i6)
          {
            ThriftP2PDevice _elem7; // optional
            _elem7 = new ThriftP2PDevice();
            _elem7.read(iprot);
            struct.devices.add(_elem7);
          }
        }
        struct.setDevicesIsSet(true);
      }
    }
  }

}

