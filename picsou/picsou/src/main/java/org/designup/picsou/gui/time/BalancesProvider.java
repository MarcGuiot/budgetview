package org.designup.picsou.gui.time;

public interface BalancesProvider {
  double getAccountBalance(int monthId);

  double getAccountBalanceLimit(int monthId);

}
