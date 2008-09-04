package org.designup.picsou.functests.checkers;

import org.uispec4j.TextBox;
import org.uispec4j.ToggleButton;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class MonthChooserChecker extends DataChecker {
  private Window dialog;

  public MonthChooserChecker(Window dialog) {
    this.dialog = dialog;
  }

  public MonthChooserChecker checkVisibleYears(int previous, int current, int next) {
    UISpecAssert.assertTrue(dialog.getTextBox("previousYearLabel").textEquals(Integer.toString(previous)));
    UISpecAssert.assertTrue(getCurrentYear().textEquals(Integer.toString(current)));
    UISpecAssert.assertTrue(dialog.getTextBox("nextYearLabel").textEquals(Integer.toString(next)));
    return this;
  }

  public TextBox getCurrentYear() {
    return dialog.getTextBox("currentYearLabel");
  }

  public MonthChooserChecker checkSelectedInPreviousMonth(int monthId) {
    UISpecAssert.assertTrue(getButtonInPreviousYear(monthId).isSelected());
    UISpecAssert.assertFalse(getButtonInCurrentYear(monthId).isSelected());
    UISpecAssert.assertFalse(getButtonInNextYear(monthId).isSelected());
    return this;
  }

  public MonthChooserChecker checkSelectedInCurrentMonth(int monthId) {
    UISpecAssert.assertFalse(getButtonInPreviousYear(monthId).isSelected());
    UISpecAssert.assertTrue(getButtonInCurrentYear(monthId).isSelected());
    UISpecAssert.assertFalse(getButtonInNextYear(monthId).isSelected());
    return this;
  }

  public MonthChooserChecker checkSelectedInNextMonth(int monthId) {
    UISpecAssert.assertFalse(getButtonInPreviousYear(monthId).isSelected());
    UISpecAssert.assertFalse(getButtonInCurrentYear(monthId).isSelected());
    UISpecAssert.assertTrue(getButtonInNextYear(monthId).isSelected());
    return this;
  }

  public MonthChooserChecker checkNoneSelected() {
    for (int i = 1; i <= 12; i++) {
      UISpecAssert.assertFalse(getButtonInPreviousYear(i).isSelected());
      UISpecAssert.assertFalse(getButtonInCurrentYear(i).isSelected());
      UISpecAssert.assertFalse(getButtonInNextYear(i).isSelected());
    }
    return this;
  }

  public MonthChooserChecker nextYear() {
    dialog.getButton("nextYearAction").click();
    return this;
  }

  public MonthChooserChecker previousYear() {
    dialog.getButton("previousYearAction").click();
    return this;
  }

  public MonthChooserChecker nextPage() {
    dialog.getButton("nextPageAction").click();
    return this;
  }

  public MonthChooserChecker previousPage() {
    dialog.getButton("previousPageAction").click();
    return this;
  }

  public MonthChooserChecker selectMonthInCurrent(int month) {
    ToggleButton button = getButtonInCurrentYear(month);
    button.click();
    UISpecAssert.assertFalse(dialog.isVisible());
    return this;
  }

  public MonthChooserChecker selectMonthInPrevious(int month) {
    ToggleButton button = getButtonInPreviousYear(month);
    button.click();
    UISpecAssert.assertFalse(dialog.isVisible());
    return this;
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

  public MonthChooserChecker checkEnabled(int month) {
    UISpecAssert.assertThat(getButtonInCurrentYear(month).isEnabled());
    return this;
  }

  public MonthChooserChecker checkDisabled(int month) {
    UISpecAssert.assertFalse("month " + month + " not disabled", getButtonInCurrentYear(month).isEnabled());
    return this;
  }
}
