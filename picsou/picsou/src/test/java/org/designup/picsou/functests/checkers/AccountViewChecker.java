package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.components.PopupButton;
import org.designup.picsou.functests.checkers.components.PopupChecker;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatcher;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.util.*;

import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.finder.ComponentMatchers.*;

public abstract class AccountViewChecker<T extends AccountViewChecker> extends ViewChecker {
  protected Window mainWindow;
  protected String panelName;
  protected Panel accountsPanel;

  public AccountViewChecker(Window mainWindow, String panelName) {
    super(mainWindow);
    this.mainWindow = mainWindow;
    this.panelName = panelName;
  }

  public void checkAccountNames(String... expectedNames) {
    org.globsframework.utils.TestUtils.assertSetEquals(getDisplayedAccounts(), expectedNames);
  }

  public void select(String accountName, String... others) {
    getAccountPanel(accountName).getToggleButton("selectAccount").select();
    for (String other : others) {
      Mouse.click(getAccountPanel(other), Key.Modifier.SHIFT);
    }
  }

  public void checkNoAccountsSelected() {
    TestUtils.assertEmpty(getSelectedAccounts());
  }

  public void checkSelectedAccounts(String... accountNames) {
    List<String> actual = getSelectedAccounts();
    TestUtils.assertSetEquals(actual, accountNames);
  }

  private List<String> getSelectedAccounts() {
    Panel accountsPanel = getAccountsPanel();
    UIComponent[] toggles = accountsPanel.getUIComponents(ToggleButton.class);
    List<String> actual = new ArrayList<String>();
    for (UIComponent toggle : toggles) {
      if (((ToggleButton)toggle).isSelected().isTrue()) {
        actual.add(toggle.getContainer("accountPanel").getButton("editAccount").getLabel());
      }
    }
    return actual;
  }

  private Set<String> getDisplayedAccounts() {
    UIComponent[] uiComponents = getAccountsPanel().getUIComponents(Button.class, "editAccount");
    Set<String> existingNames = new HashSet<String>();
    for (UIComponent uiComponent : uiComponents) {
      Button button = (Button)uiComponent;
      existingNames.add(button.getLabel());
    }
    return existingNames;
  }

  public T checkAccountOrder(String... accounts) {
    UIComponent[] uiComponents = getAccountsPanel().getUIComponents(Button.class, "editAccount");
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

  public void checkNotPresent(String accountName) {
    TestUtils.assertNotContains(getDisplayedAccounts(), accountName);
  }

  public void checkAccount(String accountName, double balance, String updateDate) {
    Panel parentPanel = getAccountPanel(accountName);
    assertThat(parentPanel.getButton("accountPosition").textEquals(toString(balance)));
    if (updateDate != null) {
      Date date = Dates.parse(updateDate);
      UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textEquals(Formatting.toString(date)));
    }
  }

  public void checkAccountUpdateDate(String accountName, String dateText) {
    Panel parentPanel = getAccountPanel(accountName);
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textEquals(dateText));
  }

  public void checkAccountWithoutPosition(String accountName, String yyyymmdd) {
    Panel parentPanel = getAccountPanel(accountName);
    assertThat(parentPanel.getButton("accountPosition").textEquals("0.00"));
    Date date = Dates.parse(yyyymmdd);
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textEquals(Formatting.toString(date)));
  }

  public void checkDisplayIsEmpty(String accountName) {
    Panel parentPanel = getAccountPanel(accountName);
    UISpecAssert.assertTrue(parentPanel.getButton("accountPosition").textEquals("0.00"));
    UISpecAssert.assertTrue(parentPanel.getTextBox("accountUpdateDate").textEquals("2006/02/01"));
  }

  public T checkSummary(double amount, String updateDate) {
    Date date = Dates.parse(updateDate);
    assertThat(getAccountsPanel().getTextBox("referencePosition").textEquals(toString(amount)));
    assertThat(getAccountsPanel().getTextBox("referencePositionDate").textEquals("on " + Formatting.toString(date)));
    return (T)this;
  }

  public T checkReferencePositionDate(String text) {
    assertThat(getAccountsPanel().getTextBox("referencePositionDate").textEquals(text));
    return (T)this;
  }

  public T checkReferencePositionDateContains(String text) {
    assertThat(getAccountsPanel().getTextBox("referencePositionDate").textContains(text));
    return (T)this;
  }


  public void checkPosition(String accountName, double position){
    Panel parentPanel = getAccountPanel(accountName);
    UISpecAssert.assertTrue(parentPanel.getButton("accountPosition").textEquals(toString(position)));
  }

  public abstract T checkEstimatedPosition(double amount);

  public abstract T checkNoEstimatedPosition();

  public abstract T checkEstimatedPositionDate(String text);

  public AccountPositionEditionChecker editPosition(String accountName) {
    Panel parentPanel = getAccountPanel(accountName);
    Window window = WindowInterceptor.getModalDialog(parentPanel.getButton("accountPosition").triggerClick());
    return new AccountPositionEditionChecker(window);
  }

  public T changePosition(String accountName, final double balance, final String operationLabel) {
    editPosition(accountName)
      .setAmountAndEnter(balance);
    return (T)this;
  }

  public AccountEditionChecker edit(String accountName) {
    return AccountEditionChecker.open(triggerEditAccount(accountName, Lang.get("accountView.edit")));
  }

  private Trigger triggerEditAccount(String accountName, String menuOption) {
    PopupButton button = new PopupButton(getAccountPanel(accountName).getButton("editAccount"));
    return button.triggerClick(menuOption);
  }

  private Panel getAccountPanel(final String accountName) {
    views.selectData();
    Button account = null;
    try {
      final ComponentMatcher componentMatcher =
        and(innerNameIdentity("editAccount"),
            displayedNameSubstring(accountName));
      account = getAccountsPanel().getButton(componentMatcher);
    }
    catch (Throwable e) {
      Assert.fail("Account '" + accountName + "' not found - available accounts: " + getDisplayedAccounts());
    }
    return account.getContainer("accountPanel");
  }

  public AccountEditionChecker createNewAccount() {
    return AccountEditionChecker.open(getAccountsPanel().getButton("createAccount").triggerClick());
  }

  public void createSavingsAccount(String name, Double position) {
    AccountEditionChecker accountEditionChecker = createNewAccount()
      .setName(name)
      .setAccountNumber("1234")
      .selectBank("LCL")
      .setAsSavings()
      .checkIsSavings();
    if (position != null) {
      accountEditionChecker
        .setPosition(position);
    }
    accountEditionChecker
      .validate();
  }

  public void createMainAccount(String name, double balance) {
    createNewAccount()
      .setName(name)
      .setAccountNumber("4321")
      .selectBank("CIC")
      .setAsMain()
      .checkIsMain()
      .setPosition(balance)
      .validate();
  }

  public void checkAccountWebsite(String accountName, String linkText, String expectedUrl) {
    BrowsingChecker.checkDisplay(triggerEditAccount(accountName, linkText), expectedUrl);
  }

  public void checkAccountWebsiteLinkDisabled(String accountName) {
    PopupButton button = new PopupButton(getAccountPanel(accountName).getButton("editAccount"));
    button.checkItemDisabled(Lang.get("accountView.goto.website.disabled"));
  }

  private Panel getAccountsPanel() {
    if (accountsPanel == null) {
      views.selectData();
      accountsPanel = mainWindow.getPanel(panelName);
    }
    return accountsPanel;
  }

  public void checkLastImportPosition(String accountName, double amount) {
    //TODO
  }
}
