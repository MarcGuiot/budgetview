package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class AccountViewChecker extends ViewChecker {

  private Panel accountView;

  public AccountViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public AccountEditionChecker createNewAccount() {
    return AccountEditionChecker.open(getPanel().getButton("createAccount").triggerClick());
  }

  public AccountViewChecker createSavingsAccount(String name, Double position) {
    return createSavingsAccount(name, "1234", position);
  }

  public AccountViewChecker createSavingsAccount(String name, String number, Double position) {
    AccountEditionChecker accountEdition = createNewAccount()
      .setName(name)
      .setAccountNumber("1234")
      .selectBank("LCL")
      .setAsSavings()
      .checkIsSavings();
    if (position != null) {
      accountEdition.setPosition(position);
    }
    accountEdition
      .validate();
    return this;
  }

  public AccountViewChecker createMainAccount(String name, String number, double balance) {
    createNewAccount()
      .setName(name)
      .setAccountNumber(number)
      .selectBank("CIC")
      .setAsMain()
      .checkIsMain()
      .setPosition(balance)
      .validate();
    return this;
  }

  public AccountViewChecker checkShowsAccounts(String... accountNames) {
    java.util.List<String> actual = new ArrayList<String>();
    Component[] buttons = getPanel().getSwingComponents(JButton.class, "editAccount");
    for (Component button : buttons) {
      actual.add(((JButton)button).getText());
    }
    TestUtils.assertEquals(actual, accountNames);
    return this;
  }

  private Panel getPanel() {
    if (accountView == null) {
      views.selectData();
      accountView = mainWindow.getPanel("accountView");
    }
    return accountView;
  }

  public AccountViewChecker checkContent(final String expectedContent) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Assert.assertEquals(expectedContent.trim(), AccountViewPanelChecker.getActualContent(getPanel()).trim());
      }
    });
    return this;
  }

  public AccountViewChecker checkContentContains(String text) {
    String actualContent = AccountViewPanelChecker.getActualContent(getPanel());
    if (!actualContent.contains(text)) {
      Assert.fail(text + " not found in:\n" + actualContent);
    }
    return this;
  }

  public AccountViewChecker checkHidden() {
    checkComponentVisible(mainWindow, JPanel.class, "accountView", false);
    return this;
  }

  public AccountViewChecker checkShown() {
    checkComponentVisible(mainWindow, JPanel.class, "accountView", true);
    return this;
  }
}
