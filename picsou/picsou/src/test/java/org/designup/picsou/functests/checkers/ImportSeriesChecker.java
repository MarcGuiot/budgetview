package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.BudgetArea;
import org.uispec4j.Panel;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;

public class ImportSeriesChecker {
  private Window dialog;
  private Panel parent;

  public ImportSeriesChecker(Window dialog, Panel parent) {
    this.dialog = dialog;
    this.parent = parent;
  }

  public ImportSeriesChecker checkContains(String... seriesName) {
    for (String s : seriesName) {
      dialog.getInputTextBox("series_" + s);
    }
    return this;
  }

  public ImportSeriesChecker setIncome(String... series) {
    return set(BudgetArea.INCOME, series);
  }

  public ImportSeriesChecker setRecurring(String... series) {
    return set(BudgetArea.RECURRING, series);
  }

  public ImportSeriesChecker setVariable(String ...series) {
    return set(BudgetArea.VARIABLE, series);
  }

  private ImportSeriesChecker set(BudgetArea budgetArea, String... series) {
    for (String s : series) {
      dialog.getComboBox("choice_" + s).select(budgetArea.getLabel());
    }
    return this;
  }

  public void validate() {
    dialog.getButton("import").click();
    assertFalse(dialog.isVisible());
    ImportDialogChecker.complete(-1, -1, -1, parent);
  }

  public ImportSeriesChecker checkNotContain(String ...series) {
    for (String s : series) {
      Assert.assertNull(dialog.findSwingComponent(JComboBox.class, "choice_" + s));
    }
    return this;
  }

  public void cancelImportSeries() {
    dialog.getButton("do not import").click();
    assertFalse(dialog.isVisible());
    ImportDialogChecker.complete(-1, -1, -1, parent);
  }
}
