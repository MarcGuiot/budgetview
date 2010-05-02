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

import javax.swing.*;
import java.util.*;

public class AccountViewChecker<T extends AccountViewChecker> extends GuiChecker {
  protected Panel panel;

  public AccountViewChecker(Panel panel, String panelName) {
    this.panel = panel.getPanel(panelName);
  }

  public void checkAccountNames(String... expectedNames) {
    org.globsframework.utils.TestUtils.assertSetEquals(getDisplayedAccounts(), expectedNames);
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

  public T checkAccountOrder(String ...accounts){
    UIComponent[] uiComponents = panel.getUIComponents(Button.class, "accountName");
    List<String> existingNames = new ArrayList<String>();
    for (UIComponent uiComponent : uiComponents) {
      Button button = (Button)uiComponent;
      existingNames.add(button.getLabel());
    }
    TestUtils.assertEquals(existingNames, accounts);
    return (T)this;
  }

  public T checkNoAccountsDisplayed() {
    TestUtils.assertEmpty(getDisplayedAccounts());
    return (T)this;
  }

  public void checkContainsAccount(String accountName) {
    TestUtils.assertContains(getDisplayedAccounts(), accountName);
  }

  public void checkNotPresent(String accountName) {
    TestUtils.assertNotContains(getDisplayedAccounts(), accountName);
  }

  public void checkAccount(String accountName, double balance, String updateDate) {
    Panel parentPanel = getAccountPanel(accountName);
    assertThat(parentPanel.getButton("accountPosition").textEquals(toString(balance)));
    Date date = Dates.parse(updateDate);
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textEquals(Formatting.toString(date)));
  }

  public void checkDisplayIsEmpty(String accountName) {
    Panel parentPanel = getAccountPanel(accountName);
    UISpecAssert.assertTrue(parentPanel.getButton("accountPosition").textEquals("0.00"));
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textEquals("01/02/2006"));
  }

  public void gotoOperations(String accountName) {
    getAccountPanel(accountName).getButton("gotoOperations").click();
  }

  public T checkSummary(double amount, String updateDate) {
    Date date = Dates.parse(updateDate);
    assertThat(panel.getTextBox("referencePosition").textEquals(toString(amount)));
    assertThat(panel.getTextBox("referencePositionDate")
      .textEquals("on " + Formatting.toString(date)));
    return (T)this;
  }

  public T checkEstimatedPosition(double amount) {
    assertThat(panel.getTextBox("estimatedPosition").textEquals(toString(amount)));
    return (T)this;
  }

  public T checkNoEstimatedPosition() {
    assertThat(panel.getTextBox("estimatedPosition").textEquals("-"));
    return (T)this;
  }

  public T checkEstimatedPositionColor(String color) {
    assertThat(panel.getTextBox("estimatedPosition").foregroundNear(color));
    return (T)this;
  }

  public T checkEstimatedPositionDate(String text) {
    assertThat(panel.getTextBox("estimatedPositionDate").textEquals(text));
    return (T)this;
  }

  public ImportChecker openImportForAccount(String accountName) {
    Button importButton = getAccountPanel(accountName).getButton("Import data");
    return ImportChecker.open(importButton.triggerClick());
  }

  public AccountPositionEditionChecker editPosition(String accountName) {
    Panel parentPanel = getAccountPanel(accountName);
    Window window = WindowInterceptor.getModalDialog(parentPanel.getButton("accountPosition").triggerClick());
    return new AccountPositionEditionChecker(window);
  }

  public T changePosition(String accountName, final double balance, final String operationLabel) {
    editPosition(accountName)
      .checkOperationLabel(operationLabel.toUpperCase())
      .setAmountAndEnter(balance);
    return (T)this;
  }

  public AccountEditionChecker edit(String accountName) {
    return AccountEditionChecker.open(getAccountPanel(accountName).getButton(accountName).triggerClick());
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

  public void createSavingsAccount(String name, double balance) {
    createNewAccount()
      .setAccountName(name)
      .setAccountNumber("1234")
      .selectBank("LCL")
      .setAsSavings()
      .checkIsSavings()
      .setPosition(balance)
      .validate();
  }

  public void createMainAccount(String name, double balance) {
    createNewAccount()
      .setAccountName(name)
      .setAccountNumber("4321")
      .selectBank("CIC")
      .setAsMain()
      .checkIsMain()
      .setPosition(balance)
      .validate();
  }

  public void checkAccountWebsite(String accountName, String linkText, String expectedUrl) {
    Panel panel = getAccountPanel(accountName);
    Button button = panel.getButton(linkText);
    assertThat(button.isVisible());
    BrowsingChecker.checkDisplay(button, expectedUrl);
  }

  public void checkAccountWebsiteLinkNotShown(String accountName) {
    Panel panel = getAccountPanel(accountName);
    checkComponentVisible(panel, JButton.class, "gotoWebsite", false);
  }
}
