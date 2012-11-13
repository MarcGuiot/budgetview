package org.designup.picsou.functests.checkers;

import org.designup.picsou.utils.Lang;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class BudgetViewMessageChecker extends ViewChecker {
  public BudgetViewMessageChecker(Window mainWindow) {
    super(mainWindow);
  }

  public BudgetViewMessageChecker checkFirstImportMessage(String accountName, double computedAmount, double importedAmount) {
    return checkMessage(Lang.get("messages.account.position.error.msg", accountName,
                                 computedAmount, importedAmount), 1);
  }

  public BudgetViewMessageChecker checkSecondImportMessage(String accountName, double computedAmount, double importedAmount) {
    return checkMessage(Lang.get("messages.account.position.error.msg", accountName,
                                 computedAmount, importedAmount), 2);
  }

  private BudgetViewMessageChecker checkMessage(String msg, int id) {
    Window dialog = getDialog();
    assertThat(dialog.getTextBox("Text " + id).textContains(msg));
    return this;
  }

  private Window getDialog() {
    Panel panel = mainWindow.getPanel("MessagesView");
    return WindowInterceptor.getModalDialog(panel.getButton().triggerClick());
  }

}
