package org.globsframework.gui.splits.components;

import javax.swing.border.LineBorder;
import java.awt.*;

public class MutableLineBorder extends LineBorder {
  public MutableLineBorder() {
    super(Color.BLACK);
  }

  public void setColor(Color color) {
    this.lineColor = color;
  }
}
