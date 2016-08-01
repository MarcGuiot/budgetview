package com.budgetview.functests.checkers.printing;

import com.budgetview.functests.checkers.GuiChecker;
import com.budgetview.functests.checkers.MessageDialogChecker;
import org.uispec4j.RadioButton;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class PrintDialogChecker extends GuiChecker {
  private Window dialog;

  public static PrintDialogChecker init(Trigger trigger) {
    return new PrintDialogChecker(WindowInterceptor.getModalDialog(trigger));
  }

  private PrintDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  public PrintDialogChecker checkOptions(String monthOption, String yearOption) {
    assertThat(getCurrentMonthRadio().textEquals(monthOption));
    assertThat(getCurrentYearRadio().textEquals(yearOption));
    return this;
  }

  public PrintDialogChecker checkCurrentMonthSelected() {
    assertThat(getCurrentMonthRadio().isSelected());
    assertFalse(getCurrentYearRadio().isSelected());
    return this;
  }

  public PrintDialogChecker checkCurrentYearSelected() {
    assertFalse(getCurrentMonthRadio().isSelected());
    assertThat(getCurrentYearRadio().isSelected());
    return this;
  }

  public PrintDialogChecker selectCurrentMonth() {
    getCurrentMonthRadio().click();
    return this;
  }

  public PrintDialogChecker selectCurrentYear() {
    getCurrentYearRadio().click();
    return this;
  }

  public void print() {
    dialog.getButton("Print").click();
    assertFalse(dialog.isVisible());
  }

  public void printWithErrorMessage(String title, String errorMessage) {
    MessageDialogChecker.open(dialog.getButton("Print").triggerClick())
      .checkTitle(title)
      .checkErrorMessageContains(errorMessage);
    assertFalse(dialog.isVisible());
  }

  public void cancel() {
    dialog.getButton("Cancel").click();
    assertFalse(dialog.isVisible());
  }

  private RadioButton getCurrentYearRadio() {
    return dialog.getRadioButton("currentYear");
  }

  private RadioButton getCurrentMonthRadio() {
    return dialog.getRadioButton("currentMonth");
  }

}
