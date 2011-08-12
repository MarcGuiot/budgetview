package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;

public class SavingsAccountViewChecker extends AccountViewChecker<SavingsAccountViewChecker> {

  private SavingsViewChecker savings;

  public SavingsAccountViewChecker(Window window) {
    super(window, "savingsAccountView");
  }

  public SavingsAccountViewChecker checkEstimatedPosition(String accountName, double position) {
    getSavings().checkEstimatedPosition(accountName, position);
    return this;
  }

  public SavingsAccountViewChecker checkEstimatedPosition(double amount) {
    getSavings().checkTotalEstimatedPosition(amount);
    return this;
  }

  public SavingsAccountViewChecker checkNoEstimatedPosition() {
    getSavings().checkNoEstimatedPosition();
    return this;
  }

  public SavingsAccountViewChecker checkEstimatedPositionDate(String date) {
    getSavings().checkTotalEstimatedPositionDate(date);
    return this;
  }

  private SavingsViewChecker getSavings() {
    if (savings == null) {
      savings = new SavingsViewChecker(mainWindow);
    }
    return savings;
  }
}
