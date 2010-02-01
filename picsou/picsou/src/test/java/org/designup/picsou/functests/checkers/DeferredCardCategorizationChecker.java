package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.BudgetArea;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;

public class DeferredCardCategorizationChecker extends SpecialCaseCategorizationChecker<DeferredCardCategorizationChecker> {

  private BudgetAreaCategorizationChecker budgetAreaChecker;

  public DeferredCardCategorizationChecker(Panel panel, CategorizationChecker categorizationChecker) {
    super(panel);
    this.budgetAreaChecker = new BudgetAreaCategorizationChecker(categorizationChecker, BudgetArea.OTHER, panel);
  }

  public ImportChecker importAccount() {
    TextBox message = getSpecialCasePanel().getTextBox();
    return ImportChecker.open(message.triggerClickOnHyperlink("import the corresponding card account"));
  }

  public DeferredCardCategorizationChecker checkMessage(String text) {
    UISpecAssert.assertThat(getSpecialCasePanel().getTextBox("message").textEquals(text));
    return this;
  }

  public DeferredCardCategorizationChecker checkContainsNoSeries() {
    budgetAreaChecker.checkContainsNoSeries();
    return this;
  }

  public DeferredCardCategorizationChecker checkActiveSeries(String seriesName) {
    budgetAreaChecker.checkActiveSeries(seriesName);
    return this;
  }

  public void checkSelectedSeries(String seriesName) {
    budgetAreaChecker.checkSeriesIsSelected(seriesName);
  }

  public DeferredCardCategorizationChecker selectSeries(String seriesName) {
    budgetAreaChecker.selectSeries(seriesName);
    return this;
  }

  public DeferredCardCategorizationChecker checkEditSeriesButtonNotVisible() {
    budgetAreaChecker.checkEditSeriesButtonNotVisible();
    return this;
  }
}