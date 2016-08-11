package com.budgetview.session.serialization;

public class SerializableGlobType {
  private int version;
  private byte[] data;
  private int id;
  private String globTypeName;

  public SerializableGlobType(String globTypeName, SerializedDelta delta) {
    this.globTypeName = globTypeName;
    id = delta.getId();
    data = delta.getData();
    version = delta.getVersion();
  }

  public SerializableGlobType() {
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getVersion() {
    return version;
  }

  public byte[] getData() {
    return data;
  }

  public void setGlobTypeName(String globTypeName) {
    this.globTypeName = globTypeName;
  }
}