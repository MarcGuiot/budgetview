package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.components.AmountEditorChecker;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

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

  public ProjectItemExpenseEditionChecker setURL(String text) {
    TextBox urlField = panel.getInputTextBox("urlField");
    assertThat(urlField.isVisible());
    urlField.setText(text);
    return this;
  }
}
