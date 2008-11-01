package org.designup.picsou.gui.time;

public interface PositionProvider {
  Double getPosition(int monthId);

  double getPositionLimit(int monthId);

}
