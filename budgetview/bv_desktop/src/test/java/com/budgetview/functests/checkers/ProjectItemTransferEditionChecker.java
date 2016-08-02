package com.budgetview.functests.checkers;

import com.budgetview.desktop.description.Formatting;
import junit.framework.Assert;
import org.uispec4j.Panel;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ProjectItemTransferEditionChecker extends ProjectItemEditionChecker<ProjectItemTransferEditionChecker> {
  public ProjectItemTransferEditionChecker(Panel panel) {
    super(panel);
  }

  public ProjectItemTransferEditionChecker setMonthAmount(double amount) {
    if (amount < 0) {
      Assert.fail("Amount should be positive");
    }
    panel.getInputTextBox("monthAmountEditor").setText(Formatting.toString(amount));
    return this;
  }

  public ProjectItemTransferEditionChecker setAmount(double amount) {
    panel.getInputTextBox("amountEditor").setText(Formatting.toString(amount));
    return this;
  }

  public ProjectItemTransferEditionChecker checkAmount(double amount) {
    assertThat(panel.getInputTextBox("amountEditor").textEquals(Formatting.toString(amount)));
    return this;
  }

  public ProjectItemTransferEditionChecker checkPositiveAmountsOnly() {
    checkComponentVisible(panel, JToggleButton.class, "signToggle", false);
    return this;
  }

  public ProjectItemTransferEditionChecker setFromAccount(String accountName) {
    panel.getComboBox("fromAccount").select(accountName);
    return this;
  }

  public ProjectItemTransferEditionChecker checkFromAccount(String accountName) {
    assertThat(panel.getComboBox("fromAccount").selectionEquals(accountName));
    return this;
  }

  public ProjectItemTransferEditionChecker checkFromAccounts(String... accountNames) {
    assertThat(panel.getComboBox("fromAccount").contentEquals(accountNames));
    return this;
  }

  public ProjectItemTransferEditionChecker checkNoFromAccountSelected(String label) {
    assertThat(panel.getComboBox("fromAccount").selectionEquals(label));
    return this;
  }

  public ProjectItemTransferEditionChecker setToAccount(String accountName) {
    panel.getComboBox("toAccount").select(accountName);
    return this;
  }

  public ProjectItemTransferEditionChecker checkToAccount(String accountName) {
    assertThat(panel.getComboBox("toAccount").selectionEquals(accountName));
    return this;
  }

  public ProjectItemTransferEditionChecker checkToAccounts(String... accountNames) {
    assertThat(panel.getComboBox("toAccount").contentEquals(accountNames));
    return this;
  }

  public ProjectItemTransferEditionChecker checkNoToAccountSelected(String label) {
    assertThat(panel.getComboBox("toAccount").selectionEquals(label));
    return this;
  }

  public ProjectItemTransferEditionChecker checkSavingsMessageShown() {
    assertThat(panel.getTextBox("savingsMessage").isVisible());
    return this;
  }

  public ProjectItemTransferEditionChecker checkSavingsMessageHidden() {
    assertFalse(panel.getTextBox("savingsMessage").isVisible());
    return this;
  }

  public ProjectItemTransferEditionChecker validateAndCheckFromAccountError(String message) {
    doValidateAndCheckError(message, panel.getComboBox("fromAccount"));
    return this;
  }

  public ProjectItemTransferEditionChecker validateAndCheckToAccountError(String message) {
    doValidateAndCheckError(message, panel.getComboBox("toAccount"));
    return this;
  }

  public ConfirmationDialogChecker validateAndCheckConfirmation() {
    ConfirmationDialogChecker confirmation = ConfirmationDialogChecker.open(panel.getButton("validate").triggerClick());
    assertTrue(enclosingPanel.containsSwingComponent(JPanel.class, "projectItemEditionPanel"));
    return confirmation;
  }
}
