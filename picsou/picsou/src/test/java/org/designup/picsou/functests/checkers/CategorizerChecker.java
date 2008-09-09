package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Window;

public class CategorizerChecker extends DataChecker {
  private TransactionChecker transactions;
  private CategorizationDialogChecker categorization;

  public static CategorizerChecker init(Window mainWindow) {
    return new CategorizerChecker(mainWindow);
  }

  public CategorizerChecker(Window mainWindow) {
    transactions = new TransactionChecker(mainWindow);
    categorization = new CategorizationDialogChecker(mainWindow);
  }

  public CategorizerChecker setRecurring(String label, String name, MasterCategory category, boolean showSeriesInitialization) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    if (index < 0) {
      Assert.fail(label + " not found");
    }
    int[] rows = new int[]{index};
    categorization.selectTableRows(rows);
    categorization.selectRecurring();
    categorization.selectRecurringSeries(name, category, showSeriesInitialization);
    categorization.checkClosed();
    return this;
  }

  public CategorizerChecker setEnvelope(String label, String envelopeName, MasterCategory category, boolean showSeriesInitialization) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    int[] rows = new int[]{index};
    categorization.selectTableRows(rows);
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries(envelopeName, category, showSeriesInitialization);
    categorization.checkClosed();
    return this;
  }

  public CategorizerChecker setOccasional(String label, MasterCategory category) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    int[] rows = new int[]{index};
    categorization.selectTableRows(rows);
    categorization.selectOccasional();
    categorization.selectOccasionalSeries(category);
    categorization.checkClosed();
    return this;
  }

  public CategorizerChecker setIncome(String label) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    int[] rows = new int[]{index};
    categorization.selectTableRows(rows);
    categorization.selectIncome();
    categorization.selectIncomeSeries("salary", true);
    categorization.checkClosed();
    return this;
  }
}
