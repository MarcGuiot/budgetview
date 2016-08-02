package com.budgetview.session.serialization;

public class SerializedDelta {
  private SerializedDeltaState state;
  private int version;
  private byte[] data;
  private int id;

  public SerializedDelta(int id) {
    this.id = id;
  }

  public void setState(SerializedDeltaState state) {
    this.state = state;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public SerializedDeltaState getState() {
    return state;
  }

  public int getId() {
    return id;
  }

  public int getVersion() {
    return version;
  }

  public byte[] getData() {
    return data;
  }
}
