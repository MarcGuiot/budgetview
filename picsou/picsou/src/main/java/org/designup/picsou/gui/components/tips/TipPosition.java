package org.designup.picsou.gui.components.tips;

import net.java.balloontip.positioners.*;

public enum TipPosition {
  TOP_LEFT,
  TOP_RIGHT,
  BOTTOM_LEFT,
  BOTTOM_RIGHT;

  BalloonTipPositioner getPositioner() {
    switch (this) {
      case TOP_LEFT:
        return new Left_Above_Positioner(10, 20);
      case TOP_RIGHT:
        return new Right_Above_Positioner(10, 20);
      case BOTTOM_LEFT:
        return new Left_Below_Positioner(10, 20);
      case BOTTOM_RIGHT:
        return new Right_Below_Positioner(10, 20);
    }
    throw new RuntimeException("Undefined position");
  }
}
