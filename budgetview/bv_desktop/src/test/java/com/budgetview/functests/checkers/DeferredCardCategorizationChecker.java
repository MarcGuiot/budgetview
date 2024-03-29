package com.budgetview.functests.checkers;

import com.budgetview.shared.model.BudgetArea;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;

public class DeferredCardCategorizationChecker extends SpecialCaseCategorizationChecker<DeferredCardCategorizationChecker> {

  private BudgetAreaCategorizationChecker budgetAreaChecker;

  public DeferredCardCategorizationChecker(Panel panel, CategorizationChecker categorizationChecker) {
    super(panel);
    this.budgetAreaChecker = new BudgetAreaCategorizationChecker(categorizationChecker, BudgetArea.OTHER, panel);
  }

  public ImportDialogChecker importAccount() {
    TextBox message = getSpecialCasePanel().getTextBox();
    return ImportDialogChecker.open(message.triggerClickOnHyperlink("import the corresponding card account"));
  }

  public DeferredCardCategorizationChecker checkMessage(String text) {
    checkSpecialCaseMessage(text);
    return this;
  }

  public DeferredCardCategorizationChecker checkContainsNoSeries() {
    budgetAreaChecker.checkContainsNoSeries();
    return this;
  }

  public DeferredCardCategorizationChecker checkActiveSeries(String seriesName) {
    budgetAreaChecker.checkSeriesIsActive(seriesName);
    return this;
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
