package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.description.Formatting;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.finder.ComponentMatcher;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AccountViewChecker extends DataChecker {
  private Panel panel;

  public AccountViewChecker(Panel panel, String panelName) {
    this.panel = panel.getPanel(panelName);
  }

  public void checkAccountNames(String... expectedNames) {
    Set<String> existingNames = getDisplayedAccounts();
    org.globsframework.utils.TestUtils.assertSetEquals(existingNames, expectedNames);
  }

  private Set<String> getDisplayedAccounts() {
    UIComponent[] uiComponents = panel.getUIComponents(Button.class, "accountName");
    Set<String> existingNames = new HashSet<String>();
    for (UIComponent uiComponent : uiComponents) {
      Button button = (Button)uiComponent;
      existingNames.add(button.getLabel());
    }
    return existingNames;
  }

  public void checkNoAccountsDisplayed() {
    TestUtils.assertEmpty(getDisplayedAccounts());
  }

  public void checkAccount(String accountName, double balance, String updateDate) {
    Panel parentPanel = getAccountPanel(accountName);
    assertThat(parentPanel.getButton("accountBalance").textEquals(toString(balance)));
    Date date = Dates.parse(updateDate);
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textEquals(Formatting.toString(date)));
  }

  public void checkDisplayIsEmpty(String accountName) {
    Panel parentPanel = getAccountPanel(accountName);
    UISpecAssert.assertTrue(parentPanel.getButton("accountBalance").textEquals("0.0"));
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textIsEmpty());
  }

  public void checkSummary(double amount, String updateDate) {
    assertThat(panel.getPanel("mainAccount").getTextBox("totalBalance").textEquals(toString(amount)));
    assertThat(panel.getPanel("mainAccount").getTextBox("accountTotalTitle")
      .textEquals("Total on " + updateDate));
  }

  public ImportChecker openImportForAccount(String accountName) {
    Button importButton = getAccountPanel(accountName).getButton("Import data");
    Window dialog = WindowInterceptor.getModalDialog(importButton.triggerClick());
    return new ImportChecker(dialog);
  }

  public BalanceEditionChecker getBalance(String accountName) {
    Panel parentPanel = getAccountPanel(accountName);
    Window window = WindowInterceptor.getModalDialog(parentPanel.getButton("accountBalance").triggerClick());
    return new BalanceEditionChecker(window);
  }

  public AccountViewChecker changeBalance(String accountName, final double balance, final String operationLabel) {
    BalanceEditionChecker balanceEditor = getBalance(accountName);
    balanceEditor
      .checkOperationLabel(operationLabel)
      .setAmountWithoutEnter(balance)
      .validate();
    return this;
  }

  public AccountEditionChecker edit(String accountName) {
    return AccountEditionChecker.open(getAccountPanel(accountName).getButton(accountName).triggerClick());
  }

  public AccountViewChecker checkAccountInformation(String accountName, String accountNumber) {
    Panel panel = getAccountPanel(accountName);
    assertThat(panel.getButton("name").textEquals(accountName));
    assertThat(panel.getTextBox("number").textEquals(accountNumber));
    return this;
  }

  private Panel getAccountPanel(final String accountName) {
    Button account = null;
    try {
      final ComponentMatcher componentMatcher = ComponentMatchers.displayedNameIdentity(accountName);
      account = panel.getButton(componentMatcher);
    }
    catch (Throwable e) {
      Assert.fail("Account '" + accountName + "' not found - available accounts: " + getDisplayedAccounts());
    }
    return account.getContainer("accountParent");
  }

  public AccountEditionChecker createNewAccount() {
    return AccountEditionChecker.open(panel.getButton("createAccount").triggerClick());
  }

  public void createSavingsAccount(final String name, final int balance) {
    createNewAccount()
      .setAccountName(name)
      .setAccountNumber("1234")
      .selectBank("LCL")
      .setAsSavings()
      .checkIsSavings()
      .setBalance(balance)
      .validate();
  }

}
