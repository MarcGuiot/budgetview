package com.designup.siteweaver.server.upload;

public class FileHandle {
  public final String path;
  public final long timestamp;

  public FileHandle(String path, long timestamp) {
    this.path = path;
    this.timestamp = timestamp;
  }

  public String toString() {
    return path + ":" + timestamp;
  }
}
