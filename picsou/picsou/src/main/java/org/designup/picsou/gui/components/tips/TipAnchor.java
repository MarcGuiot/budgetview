package org.designup.picsou.gui.components.tips;

import net.java.balloontip.BalloonTip;

public enum TipAnchor {
  CENTER(BalloonTip.AttachLocation.CENTER),
  NORTH(BalloonTip.AttachLocation.NORTH),
  NORTHEAST(BalloonTip.AttachLocation.NORTHEAST),
  EAST(BalloonTip.AttachLocation.NORTHWEST),
  SOUTHEAST(BalloonTip.AttachLocation.SOUTHEAST),
  SOUTH(BalloonTip.AttachLocation.SOUTH),
  SOUTHWEST(BalloonTip.AttachLocation.SOUTHWEST),
  WEST(BalloonTip.AttachLocation.WEST),
  NORTHWEST(BalloonTip.AttachLocation.NORTHWEST);
  private BalloonTip.AttachLocation location;

  TipAnchor(BalloonTip.AttachLocation location) {
    this.location = location;
  }

  public BalloonTip.AttachLocation getLocation() {
    return location;
  }
}
