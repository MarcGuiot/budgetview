package org.designup.picsou.gui.components;

import junit.framework.TestCase;

import javax.swing.*;
import java.awt.*;

public abstract class ChartTestCase extends TestCase {
  protected FontMetrics getFontMetrics() {
    JLabel label = new JLabel();
    return new FontMetrics(label.getFont()) {
      public int stringWidth(String str) {
        return str.length() * 5;
      }

      public int getHeight() {
        return 12;
      }
    };
  }
}
