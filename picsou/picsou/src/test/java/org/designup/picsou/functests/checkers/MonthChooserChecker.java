package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.Month;
import org.uispec4j.Key;
import org.uispec4j.TextBox;
import org.uispec4j.ToggleButton;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import org.uispec4j.utils.KeyUtils;

import javax.swing.*;

public class MonthChooserChecker extends DataChecker {
  private Window dialog;

  public MonthChooserChecker(Window dialog) {
    this.dialog = dialog;
  }

  public MonthChooserChecker checkVisibleYears(int previous, int current, int next) {
    assertTrue(dialog.getTextBox("previousYearLabel").textEquals(Integer.toString(previous)));
    assertTrue(getCurrentYear().textEquals(Integer.toString(current)));
    assertTrue(dialog.getTextBox("nextYearLabel").textEquals(Integer.toString(next)));
    return this;
  }

  public TextBox getCurrentYear() {
    return dialog.getTextBox("currentYearLabel");
  }

  public MonthChooserChecker checkSelectedInPreviousMonth(int monthId) {
    assertTrue(getButtonInPreviousYear(monthId).isSelected());
    assertFalse(getButtonInCurrentYear(monthId).isSelected());
    assertFalse(getButtonInNextYear(monthId).isSelected());
    return this;
  }

  public MonthChooserChecker checkSelectedInCurrentMonth(int monthId) {
    assertFalse(getButtonInPreviousYear(monthId).isSelected());
    assertTrue(getButtonInCurrentYear(monthId).isSelected());
    assertFalse(getButtonInNextYear(monthId).isSelected());
    return this;
  }

  public MonthChooserChecker checkSelectedInNextMonth(int monthId) {
    assertFalse(getButtonInPreviousYear(monthId).isSelected());
    assertFalse(getButtonInCurrentYear(monthId).isSelected());
    assertTrue(getButtonInNextYear(monthId).isSelected());
    return this;
  }

  public MonthChooserChecker checkNoneSelected() {
    for (int i = 1; i <= 12; i++) {
      assertFalse(getButtonInPreviousYear(i).isSelected());
      assertFalse(getButtonInCurrentYear(i).isSelected());
      assertFalse(getButtonInNextYear(i).isSelected());
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
    assertFalse(dialog.isVisible());
    return this;
  }

  public MonthChooserChecker selectMonthInPrevious(int month) {
    ToggleButton button = getButtonInPreviousYear(month);
    button.click();
    assertFalse(dialog.isVisible());
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
    assertFalse(dialog.isVisible());
  }

  public MonthChooserChecker checkEnabledInCurrentYear(int month) {
    UISpecAssert.assertThat(getButtonInCurrentYear(month).isEnabled());
    return this;
  }

  public MonthChooserChecker checkDisabledInCurrentYear(int month) {
    assertFalse("month " + month + " not disabled", getButtonInCurrentYear(month).isEnabled());
    return this;
  }

  public MonthChooserChecker checkIsEnabled(int... monthIds) {
    for (int monthId : monthIds) {
      centerTo(monthId);
      assertTrue(getButtonInCurrentYear(Month.toMonth(monthId)).isEnabled());
    }
    return this;
  }

  public MonthChooserChecker checkIsDisabled(int... monthIds) {
    for (int monthId : monthIds) {
      centerTo(monthId);
      assertFalse(monthId + " is enable.", getButtonInCurrentYear(Month.toMonth(monthId)).isEnabled());
    }
    return this;
  }

  public MonthChooserChecker centerTo(int monthId) {
    int currentYear = Integer.parseInt(getCurrentYear().getText());
    int year = Month.toYear(monthId);
    for (; year < currentYear; year++) {
      previousYear();
    }
    for (; currentYear < year; currentYear++) {
      nextYear();
    }
    return this;
  }

  public MonthChooserChecker gotoCenter() {
    dialog.getButton("homeYearAction").click();
    return this;
  }

  public void pressEscapeKey() {
    final JDialog jDialog = (JDialog)dialog.getAwtComponent();
    KeyUtils.pressKey(jDialog.getRootPane(), Key.ESCAPE);
    assertFalse(dialog.isVisible());
  }

  public MonthChooserChecker selectMonth(int monthId) {
    centerTo(monthId);
    selectMonthInCurrent(Month.toMonth(monthId));
    return this;
  }
}
