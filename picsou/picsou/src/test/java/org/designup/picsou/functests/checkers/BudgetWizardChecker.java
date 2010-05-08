package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Button;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class BudgetWizardChecker extends GuiChecker {
  private Panel panel;
  public static final String PANEL_NAME = "wizardPanel";

  public BudgetWizardChecker(Panel budgetSummaryView) {
    this.panel = budgetSummaryView.getPanel(PANEL_NAME);
    assertThat(panel.isVisible());
  }

  public BudgetWizardChecker checkHelpMessageContains(String text) {
    assertThat(panel.getPanel("content").getTextBox("editor").textContains(text));
    return this;
  }

  public BudgetWizardChecker next() {
    panel.getButton("next").click();
    return this;
  }

  public BudgetWizardChecker previous() {
    panel.getButton("previous").click();
    return this;
  }

  public BudgetWizardChecker checkNextButtonText(String text) {
    Button nextButton = panel.getButton("next");
    assertThat(nextButton.textEquals(text));
    return this;
  }
 }
