package org.designup.picsou.gui.time;

public interface BalancesProvider {
  Double getAccountBalance(int monthId);

  double getAccountBalanceLimit(int monthId);

}
