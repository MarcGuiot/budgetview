package com.budgetview.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class AccountChooserChecker extends GuiChecker {
  private Window dialog;
  private ImportDialogChecker importDialog;

  public AccountChooserChecker(ImportDialogChecker importDialog, Window dialog) {
    this.importDialog = importDialog;
    this.dialog = dialog;
  }

  public static AccountChooserChecker open(ImportDialogChecker importDialog, Trigger trigger) {
    return new AccountChooserChecker(importDialog, WindowInterceptor.getModalDialog(trigger));
  }

  public AccountChooserChecker associate(String newAccount, String existingAccount) {
    dialog.getTextBox(newAccount)
      .getContainer("parent")
      .getComboBox()
      .select(existingAccount);
    return this;
  }

  public AccountChooserChecker checkTargetContent(String newAccount, String... accounts) {
    assertThat(dialog.getTextBox(newAccount)
                 .getContainer("parent")
                 .getComboBox().contentEquals(accounts));
    return this;
  }

  public AccountChooserChecker checkSelected(String newAccount, String account) {
    assertThat(dialog.getTextBox(newAccount)
                 .getContainer("parent")
                 .getComboBox().selectionEquals(account));
    return this;
  }

  public ImportDialogChecker validate() {
    dialog.getButton("OK").click();
    assertFalse(dialog.isVisible());
    return importDialog;
  }
}
