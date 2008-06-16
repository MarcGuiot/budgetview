package org.designup.picsou.gui.startup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class OpenRequestManager {
  private Stack<Callback> callbacks = new Stack<Callback>();
  private List<File> pendingFiles = new ArrayList<File>();

  public interface Callback {
    void openFiles(List<File> files);
  }

  synchronized public void pushCallback(Callback callback) {
    callbacks.push(callback);
    if (callbacks.size() == 1 && !pendingFiles.isEmpty()) {
      List<File> filesToOpen = new ArrayList<File>(pendingFiles);
      pendingFiles.clear();
      callback.openFiles(filesToOpen);
    }
  }

  synchronized public void popCallback() {
    callbacks.pop();
  }

  synchronized public void openFiles(List<File> files) {
    if (callbacks.isEmpty()) {
      pendingFiles.addAll(files);
    }
    else {
      callbacks.peek().openFiles(files);
    }
  }
}
