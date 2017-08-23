package com.budgetview.functests.checkers;

import org.globsframework.utils.TestUtils;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.ToggleButton;
import org.uispec4j.Window;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.finder.ComponentMatchers.*;

public class CloudAccountsChecker extends ViewChecker {
  public CloudAccountsChecker(Window mainWindow) {
    super(mainWindow);
    checkPanelShown("importCloudAccountsPanel");
  }

  public CloudAccountsChecker checkAccounts(String... accounts) {
    List<String> actual = new ArrayList<String>();
    for (java.awt.Component panel : mainWindow.getPanel("accounts").getSwingComponents(and(fromClass(JPanel.class),
                                                                                           innerNameRegexp("accountPanel:.*")))) {
      Panel accountPanel = new Panel((JPanel)panel);
      TextBox accountName = accountPanel.getTextBox("accountName");
      TextBox accountNumber = accountPanel.getTextBox("accountNumber");
      ToggleButton toggle = accountPanel.getToggleButton();
      actual.add(accountName.getText() + " / " + accountNumber.getText() + " / " + toggle.isSelected().isTrue());
    }
    TestUtils.assertEquals(actual, accounts);
    return this;
  }

  public CloudAccountsChecker enableAccount(String accountName) {
    Panel panel = mainWindow.getPanel("accounts").getPanel("accountPanel:" + accountName);
    panel.getToggleButton().select();
    return this;
  }

  public CloudAccountsChecker disableAccount(String accountName) {
    Panel panel = mainWindow.getPanel("accounts").getPanel("accountPanel:" + accountName);
    panel.getToggleButton().unselect();
    return this;
  }

  public CloudAccountsChecker checkApplyDisabled() {
    assertFalse(mainWindow.getButton("apply").isEnabled());
    return this;
  }

  public CloudAccountsChecker apply() {
    mainWindow.getButton("apply").click();
    return this;
  }

  public CloudAccountsChecker checkApplyMessage(String text) {
    assertThat(mainWindow.getTextBox("applyMessage").htmlEquals(text));
    return this;
  }

  public CloudEditionChecker back() {
    mainWindow.getButton("back").click();
    return new CloudEditionChecker(mainWindow);
  }

  public void close() {
    mainWindow.getButton("close").click();
    assertFalse(mainWindow.isVisible());
  }
}
