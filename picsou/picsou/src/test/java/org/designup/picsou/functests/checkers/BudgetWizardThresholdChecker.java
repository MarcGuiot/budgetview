package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.budget.wizard.PositionThresholdIndicator;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class BudgetWizardThresholdChecker extends BudgetWizardPageChecker {

  public BudgetWizardThresholdChecker(BudgetWizardPageChecker wizardPage) {
    super(wizardPage.gotoPage("Position threshold"));
  }

  public BudgetWizardThresholdChecker checkThreshold(double value, String message, double diff) {
    assertThat(panel.getTextBox("thresholdField").textEquals(toString(value)));
    assertThat(panel.getTextBox("thresholdMessage").textEquals(message));

    PositionThresholdIndicator indicator =
      (PositionThresholdIndicator)panel.getPanel("thresholdIndicator").getAwtComponent();
    Assert.assertEquals(diff, indicator.getDiff(), 0.01);
    return this;
  }

  public BudgetWizardThresholdChecker setThreshold(double value) {
    panel.getTextBox("thresholdField").setText(toString(value));
    return this;
  }

  public BudgetWizardThresholdChecker clearThreshold() {
    panel.getTextBox("thresholdField").clear();
    return this;
  }
}