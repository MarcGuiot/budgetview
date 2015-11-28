package org.uispec4j.interception.toolkit;

import java.awt.*;

public class UISpecFramePeer extends Empty.FramePeer {
  private Frame frame;
  private boolean inProgress = false;

  public UISpecFramePeer(Frame frame) {
    this.frame = frame;
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
        UISpecDisplay.instance().showFrame(frame);
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
