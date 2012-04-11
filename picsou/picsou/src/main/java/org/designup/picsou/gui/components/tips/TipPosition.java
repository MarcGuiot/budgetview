package org.designup.picsou.gui.components.tips;

import net.java.balloontip.positioners.BalloonTipPositioner;
import net.java.balloontip.positioners.Left_Above_Positioner;
import net.java.balloontip.positioners.Right_Above_Positioner;

public enum TipPosition {
  TOP_LEFT,
  TOP_RIGHT;

  BalloonTipPositioner getPositioner() {
    switch (this) {
      case TOP_LEFT:
        return new Left_Above_Positioner(10, 20);
      case TOP_RIGHT:
        return new Right_Above_Positioner(10, 20);
    }
    throw new RuntimeException("Undef");
  }
}
