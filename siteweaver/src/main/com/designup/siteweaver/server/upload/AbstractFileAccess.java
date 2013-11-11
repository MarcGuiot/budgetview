package com.designup.siteweaver.server.upload;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFileAccess implements FileAccess {
  private List<FileAccessListener> listeners = new ArrayList<FileAccessListener>();
  protected boolean applyChanges;

  public void addListener(FileAccessListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(FileAccessListener listener) {
    this.listeners.remove(listener);
  }

  public void setApplyChanges(boolean applyChanges) {
    this.applyChanges = applyChanges;
  }

  protected void notifyUploadText(String path, String content) {
    for (FileAccessListener listener : listeners) {
      listener.processUploadText(path, content);
    }
  }

  protected void notifyUploadFile(String path, File file) {
    for (FileAccessListener listener : listeners) {
      listener.processUploadFile(path, file);
    }
  }

  protected void notifyDelete(String path) {
    for (FileAccessListener listener : listeners) {
      listener.processDelete(path);
    }
  }
}
