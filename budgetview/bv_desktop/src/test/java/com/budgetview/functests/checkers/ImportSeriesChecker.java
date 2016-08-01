package com.budgetview.functests.checkers;

import com.budgetview.model.BudgetArea;
import com.budgetview.utils.Lang;
import junit.framework.Assert;
import org.uispec4j.Panel;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ImportSeriesChecker {
  private Window dialog;
  private Panel parent;

  public static ImportSeriesChecker init(Trigger trigger, Window importDialog) {
    return new ImportSeriesChecker(WindowInterceptor.getModalDialog(trigger), importDialog);
  }

  public ImportSeriesChecker(Window dialog, Panel parent) {
    this.dialog = dialog;
    this.parent = parent;
    assertThat(dialog.getTextBox("title").textEquals(Lang.get("import.series.title")));
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

  public ImportSeriesChecker setVariable(String... series) {
    return set(BudgetArea.VARIABLE, series);
  }

  public ImportSeriesChecker unset(String... series) {
    for (String s : series) {
      dialog.getComboBox("choice_" + s).select(Lang.get("import.series.uncategorized"));
    }
    return this;
  }

  private ImportSeriesChecker set(BudgetArea budgetArea, String... series) {
    for (String s : series) {
      dialog.getComboBox("choice_" + s).select(budgetArea.getLabel());
    }
    return this;
  }

  public void validateAndFinishImport() {
    dialog.getButton("import").click();
    assertFalse(dialog.isVisible());
    ImportDialogChecker.complete(-1, -1, -1, parent);
  }

  public void validateAndFinishImport(int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount) {
    dialog.getButton("import").click();
    assertFalse(dialog.isVisible());
    ImportDialogChecker.complete(importedTransactionCount, ignoredTransactionCount, autocategorizedTransactionCount, parent);
  }

  public ImportSeriesChecker checkNotContain(String... series) {
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
