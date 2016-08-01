package com.budgetview.gui.utils;

import org.globsframework.gui.splits.utils.GuiUtils;

import java.awt.*;

public class FrameSize {
  public final Dimension screenSize;
  public final Dimension targetFrameSize;

  public static FrameSize init(Window window) {
    Dimension maxSize = GuiUtils.getMaxSize(window);
    Dimension targetSize =
      new Dimension(Math.min(maxSize.width, 1100),
                    Math.min(maxSize.height, 800));
    return new FrameSize(maxSize, targetSize);
  }

  private FrameSize(Dimension screenSize, Dimension targetFrameSize) {
    this.screenSize = screenSize;
    this.targetFrameSize = targetFrameSize;
  }

  public String toString() {
    return "FrameSize: screen:" + screenSize.width + "x" + screenSize.height +
           " - targetFrame:" + targetFrameSize.width + "x" + targetFrameSize.height;
  }
}
