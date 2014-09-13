package org.designup.picsou.gui.components.tips;

import net.java.balloontip.positioners.*;

public enum TipPosition {
  TOP_LEFT,
  TOP_RIGHT,
  BOTTOM_LEFT,
  BOTTOM_RIGHT,
  CENTER;

  BalloonTipPositioner getPositioner() {
    switch (this) {
      case TOP_LEFT:
        return new LeftAbovePositioner(10, 20);
      case TOP_RIGHT:
        return new RightAbovePositioner(10, 20);
      case BOTTOM_LEFT:
        return new LeftBelowPositioner(10, 20);
      case BOTTOM_RIGHT:
        return new RightBelowPositioner(10, 20);
      case CENTER:
        return new CenteredPositioner(10);
    }
    throw new RuntimeException("Undefined position");
  }
}
