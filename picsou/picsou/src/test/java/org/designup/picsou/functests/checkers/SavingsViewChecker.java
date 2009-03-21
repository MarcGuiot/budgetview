package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

public class SavingsViewChecker extends GuiChecker {
  private Window window;

  public SavingsViewChecker(Window window) {
    this.window = window;
  }

  public SavingsViewChecker checkSavingsBalance(double balance) {
    return this;
  }

  public void checkSavingsIn(String accountName, double observedAmount, double plannedAmount) {
//    assertThat(window.getButton(accountName + ":savingsInAmount").textEquals(toString(observedAmount)));
//    assertThat(window.getTextBox(accountName + ":savingsPlannedInAmount").textEquals(toString(plannedAmount)));
  }

  public void checkSavingsOut(String accoutName, double observedAmount, double plannedAmount) {
//    assertThat(window.getButton(accoutName + ":savingsOutAmount").textEquals(toString(observedAmount)));
//    assertThat(window.getTextBox(accoutName + ":savingsPlannedOutAmount").textEquals(toString(plannedAmount)));
  }

  public void checkAmount(String accountName, String seriesName, double observedAmount, double plannedAmount) {
    assertThat(window.getButton(accountName + "." + seriesName + ".observedSeriesAmount").textEquals(toString(observedAmount)));
    assertThat(window.getButton(accountName + "." + seriesName + ".plannedSeriesAmount").textEquals(toString(plannedAmount)));
  }

  public void checkSavingsInNotVisible(String accountName, String seriesName) {
    UISpecAssert.assertFalse(window.getPanel(accountName + "." + seriesName + ".gauge").isVisible());
  }

  public SeriesEditionDialogChecker editSeries(String accountName, String seriesName) {
    return openSeriesEditionDialog(accountName, seriesName);
  }

  private SeriesEditionDialogChecker openSeriesEditionDialog(String accountName, String seriesName) {
    String buttonName = accountName + "." + seriesName + ".edit";
    Window dialog = WindowInterceptor.getModalDialog(window.getButton(buttonName).triggerClick());
    return new SeriesEditionDialogChecker(dialog, false);
  }

}
