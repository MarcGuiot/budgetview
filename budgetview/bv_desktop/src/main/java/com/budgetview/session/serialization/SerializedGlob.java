package com.budgetview.session.serialization;

public class SerializedGlob {
  private String globTypeName;
  private int id;
  private int version;
  private byte[] data;

  public SerializedGlob(String globTypeName, int id, int version, byte[] data) {
    this.globTypeName = globTypeName;
    this.id = id;
    this.version = version;
    this.data = data;
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
}
