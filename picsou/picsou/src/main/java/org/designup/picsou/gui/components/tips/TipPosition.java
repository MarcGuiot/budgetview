package org.designup.picsou.gui.components.tips;

import net.java.balloontip.positioners.BalloonTipPositioner;
import net.java.balloontip.positioners.Left_Above_Positioner;
import net.java.balloontip.positioners.Right_Above_Positioner;

public enum TipPosition {
  TOP_LEFT(new Left_Above_Positioner(10, 20)),
  TOP_RIGHT(new Right_Above_Positioner(10, 20));

  private BalloonTipPositioner positioner;

  TipPosition(BalloonTipPositioner positioner) {
    this.positioner = positioner;
  }

  BalloonTipPositioner getPositioner() {
    return positioner;
  }
}
