package org.designup.picsou.gui.plaf;

import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;

public class PicsouSplitPaneUI extends BasicSplitPaneUI {
  public BasicSplitPaneDivider getDivider() {
    return new BasicSplitPaneDivider(this) {
      public void paint(Graphics g) {
      }
    };
  }
}
