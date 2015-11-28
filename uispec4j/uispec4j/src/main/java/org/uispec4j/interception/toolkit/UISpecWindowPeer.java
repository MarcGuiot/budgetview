package org.uispec4j.interception.toolkit;

import java.awt.*;

public class UISpecWindowPeer extends Empty.WindowPeeer {
  Window window;
  private boolean inProgress = false;

  public UISpecWindowPeer(Window window) {
    this.window = window;
  }

  public void show() {
    processShow();
  }

  public void setVisible(boolean shown) {
    if (shown) {
      processShow();
    }
  }

  public void processShow() {
    if (!inProgress) {
      inProgress = true;
      try {
        UISpecDisplay.instance().showWindow(window);
        UISpecDisplay.instance().rethrowIfNeeded();
      }
      finally {
        inProgress = false;
      }
    }
  }
  public Toolkit getToolkit() {
    return UISpecToolkit.instance();
  }
}
