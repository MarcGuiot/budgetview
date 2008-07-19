package org.globsframework.gui.splits.components;

import javax.swing.border.MatteBorder;
import java.awt.*;

public class MutableMatteBorder extends MatteBorder {
  public MutableMatteBorder(int top, int left, int bottom, int right, Color matteColor) {
    super(top, left, bottom, right, matteColor);
  }

  public void setColor(Color color) {
    this.color = color;
  }
}
