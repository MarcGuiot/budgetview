package org.designup.picsou.functests.checkers;

import org.uispec4j.ToggleButton;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import junit.framework.Assert;

public class MonthChooserChecker extends DataChecker {
  private Window dialog;

  public MonthChooserChecker(Window dialog) {
    this.dialog = dialog;
  }

  public void checkVisibleYears(int previous, int current, int next) {
    dialog.getTextBox("previousYearLabel").textEquals(Integer.toString(previous));
    dialog.getTextBox("currentYearLabel").textEquals(Integer.toString(current));
    dialog.getTextBox("nextYearLabel").textEquals(Integer.toString(next));
  }

  public void checkSelectedInPreviousMonth(int monthId) {
    UISpecAssert.assertTrue(getButtonInPreviousYear(monthId).isSelected());
    UISpecAssert.assertFalse(getButtonInCurrentYear(monthId).isSelected());
    UISpecAssert.assertFalse(getButtonInNextYear(monthId).isSelected());
  }

  public void checkSelectedInCurrentMonth(int monthId) {
    UISpecAssert.assertFalse(getButtonInPreviousYear(monthId).isSelected());
    UISpecAssert.assertTrue(getButtonInCurrentYear(monthId).isSelected());
    UISpecAssert.assertFalse(getButtonInNextYear(monthId).isSelected());
  }

  public void checkSelectedInNextMonth(int monthId) {
    UISpecAssert.assertFalse(getButtonInPreviousYear(monthId).isSelected());
    UISpecAssert.assertFalse(getButtonInCurrentYear(monthId).isSelected());
    UISpecAssert.assertTrue(getButtonInNextYear(monthId).isSelected());
  }

  public void checkNoneSelected() {
    for (int i = 1; i <= 12; i++){
      UISpecAssert.assertFalse(getButtonInPreviousYear(i).isSelected());
      UISpecAssert.assertFalse(getButtonInCurrentYear(i).isSelected());
      UISpecAssert.assertFalse(getButtonInNextYear(i).isSelected());
    }
  }

  public void nextYear() {
    dialog.getButton("nextYearAction").click();
  }

  public void previousYear() {
    dialog.getButton("previousYearAction").click();
  }

  public void nextPage() {
    dialog.getButton("nextPageAction").click();
  }

  public void previousPage() {
    dialog.getButton("previousPageAction").click();
  }

  public void selectMonthInCurrent(int month) {
    ToggleButton button = getButtonInCurrentYear(month);
    button.click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  public void selectMonthInPrevious(int month) {
    ToggleButton button = getButtonInPreviousYear(month);
    button.click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }

  private ToggleButton getButtonInCurrentYear(int month) {
    return dialog.getPanel("currentYearMonths")
      .getToggleButton(Integer.toString(month));
  }

  private ToggleButton getButtonInNextYear(int month) {
    return dialog.getPanel("nextYearMonths")
      .getToggleButton(Integer.toString(month));
  }

  private ToggleButton getButtonInPreviousYear(int month) {
    return dialog.getPanel("previousYearMonths")
      .getToggleButton(Integer.toString(month));
  }

  public void cancel() {
    dialog.getButton("cancel").click();
    UISpecAssert.assertFalse(dialog.isVisible());
  }
}
