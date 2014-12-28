package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.AmountEditorChecker;
import org.uispec4j.ComboBox;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ProjectItemExpenseEditionChecker extends ProjectItemEditionChecker<ProjectItemExpenseEditionChecker> {
  public ProjectItemExpenseEditionChecker(Panel panel) {
    super(panel);
  }

  public ProjectItemExpenseEditionChecker setMonthAmount(double amount) {
    AmountEditorChecker.init(panel, "monthAmountEditor").set(amount);
    return this;
  }

  public ProjectItemExpenseEditionChecker setAmount(double amount) {
    AmountEditorChecker.init(panel, "amountEditor").set(amount);
    return this;
  }

  public ProjectItemExpenseEditionChecker checkAmount(double amount) {
    AmountEditorChecker.init(panel, "amountEditor").checkAmount(amount);
    return this;
  }

  public ProjectItemExpenseEditionChecker setTargetAccount(String accountName) {
    panel.getComboBox("accountSelection").select(accountName);
    return this;
  }

  public ProjectItemExpenseEditionChecker checkTargetAccountChoices(String... accounts) {
    ComboBox combo = panel.getComboBox("accountSelection");
    assertThat(combo.contentEquals(accounts));
    assertThat(combo.isVisible());
    assertFalse(panel.getTextBox("accountLabel").isVisible());
    return this;
  }

  public ProjectItemExpenseEditionChecker checkTargetAccountCombo(String account) {
    ComboBox combo = panel.getComboBox("accountSelection");
    assertThat(combo.selectionEquals(account));
    assertThat(combo.isVisible());
    assertFalse(panel.getTextBox("accountLabel").isVisible());
    return this;
  }

  public ProjectItemExpenseEditionChecker checkTargetAccountLabel(String account) {
    TextBox accountLabel = panel.getTextBox("accountLabel");
    assertThat(accountLabel.textEquals(account));
    assertThat(accountLabel.isVisible());
    assertFalse(panel.getComboBox("accountSelection").isVisible());
    return this;
  }

  public ProjectItemExpenseEditionChecker setURL(String text) {
    TextBox urlField = panel.getInputTextBox("urlField");
    assertThat(urlField.isVisible());
    urlField.setText(text);
    return this;
  }
}
