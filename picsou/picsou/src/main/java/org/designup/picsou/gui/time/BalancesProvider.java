package org.designup.picsou.gui.time;

public interface BalancesProvider {
  double getBalance(int monthId);

  double getCurrentLevel(int monthId);

}
