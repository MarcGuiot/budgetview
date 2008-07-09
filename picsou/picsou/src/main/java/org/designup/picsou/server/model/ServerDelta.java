package org.designup.picsou.server.model;

public class ServerDelta {
  private ServerState state;
  private int version;
  private byte[] data;
  private int id;

  public ServerDelta(int id) {
    this.id = id;
  }

  public void setState(ServerState state) {
    this.state = state;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public ServerState getState() {
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
