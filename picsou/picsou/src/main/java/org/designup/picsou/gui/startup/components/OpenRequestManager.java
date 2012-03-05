package org.designup.picsou.gui.startup.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class OpenRequestManager {
  private Stack<Callback> callbacks = new Stack<Callback>();
  private List<File> pendingFiles = new ArrayList<File>();

  public interface Callback {
    boolean accept();

    void openFiles(List<File> files);
  }

  synchronized public void pushCallback(Callback callback) {
    callbacks.push(callback);
    push();
  }

  private void push() {
    if (callbacks.size() >= 1 && !pendingFiles.isEmpty() && callbacks.peek().accept()) {
      List<File> filesToOpen = new ArrayList<File>(pendingFiles);
      pendingFiles.clear();
      callbacks.peek().openFiles(filesToOpen);
    }
  }

  synchronized public void popCallback() {
    callbacks.pop();
    push();
  }

  synchronized public void openFiles(List<File> files) {
    if (callbacks.isEmpty()) {
      pendingFiles.addAll(files);
    }
    else {
      Callback callback = callbacks.peek();
      if (callback.accept()) {
        callback.openFiles(files);
      }
      else {
        pendingFiles.addAll(files);
      }
    }
  }
}
