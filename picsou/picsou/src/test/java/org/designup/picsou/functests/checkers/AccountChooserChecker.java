package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowInterceptor;

public class AccountChooserChecker extends GuiChecker {
  private Window dialog;
  private ImportChecker importChecker;

  public AccountChooserChecker(ImportChecker importChecker, Window dialog) {
    this.importChecker = importChecker;
    this.dialog = dialog;
  }

  public static AccountChooserChecker open(ImportChecker importChecker, Trigger trigger) {
    return new AccountChooserChecker(importChecker, WindowInterceptor.getModalDialog(trigger));
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

  public ImportChecker validate() {
    dialog.getButton("OK").click();
    return importChecker;
  }
}
