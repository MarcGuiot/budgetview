package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.globsframework.utils.Dates;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatcher;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;

import java.util.Date;

public class AccountChecker extends DataChecker {
  private Panel panel;

  public AccountChecker(Panel panel) {
    this.panel = panel;
  }

  public void assertDisplayEquals(String accountNumber, double balance, String updateDate) {
    Panel parentPanel = getAccountPanel(accountNumber);
    UISpecAssert.assertThat(parentPanel.getButton("accountBalance").textEquals(toString(balance)));
    Date date = Dates.parse(updateDate);
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textEquals(PicsouDescriptionService.toString(date)));
  }

  private Panel getAccountPanel(final String accountName) {
    final ComponentMatcher componentMatcher = ComponentMatchers.displayedNameIdentity(accountName);
    TextBox account = panel.getTextBox(componentMatcher);
    return account.getContainer("accountParent");
  }

  public void assertDisplayEquals(String accountName) {
    Panel parentPanel = getAccountPanel(accountName);
    UISpecAssert.assertTrue(parentPanel.getButton("accountBalance").textEquals("0.0"));
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textIsEmpty());
  }

  public void checkSummary(double amount, String updateDate) {
    UISpecAssert.assertThat(panel.getTextBox("totalBalance").textEquals(toString(amount)));
  }

  public Trigger getImportTrigger(String account) {
    return getAccountPanel(account).getButton("Import data").triggerClick();
  }

  public BalanceEditionChecker getBalance(String accountName) {
    Panel parentPanel = getAccountPanel(accountName);
    Window window = WindowInterceptor.getModalDialog(parentPanel.getButton("accountBalance").triggerClick());
    return new BalanceEditionChecker(window);
  }

  public AccountChecker changeBalance(String accountName, final double balance, final String label) {
    BalanceEditionChecker balanceEditor = getBalance(accountName);
    balanceEditor
      .setAmount(balance)
      .checkLabel(label)
      .validate();
    return this;
  }
}
