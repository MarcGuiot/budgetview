package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Window;

public class CategorizerChecker extends DataChecker {
  private TransactionChecker transactions;

  public static CategorizerChecker init(Window mainWindow) {
    return new CategorizerChecker(mainWindow);
  }

  public CategorizerChecker(Window mainWindow) {
    transactions = new TransactionChecker(mainWindow);
  }

  public CategorizerChecker setRecurring(String label, String name, MasterCategory category, boolean showSeriesInitialization) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    if (index < 0) {
      Assert.fail(label + " not found");
    }
    CategorizationDialogChecker categorizationChecker = transactions.categorize(index);
    categorizationChecker.selectRecurring();
    categorizationChecker.selectRecurringSeries(name, category, showSeriesInitialization);
    categorizationChecker.validate();
    return this;
  }

  public CategorizerChecker setEnvelope(String label, String envelopeName, MasterCategory category, boolean showSeriesInitialization) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    CategorizationDialogChecker categorizationChecker = transactions.categorize(index);
    categorizationChecker.selectEnvelopes();
    categorizationChecker.selectEnvelopeSeries(envelopeName, category, showSeriesInitialization);
    categorizationChecker.validate();
    return this;
  }

  public CategorizerChecker setOccasional(String label, MasterCategory category) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    CategorizationDialogChecker categorizationChecker = transactions.categorize(index);
    categorizationChecker.selectOccasional();
    categorizationChecker.selectOccasionalSeries(category);
    categorizationChecker.validate();
    return this;
  }

  public CategorizerChecker setIncome(String label) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    CategorizationDialogChecker categorizationChecker = transactions.categorize(index);
    categorizationChecker.selectIncome();
    categorizationChecker.selectIncomeSeries("salary", true);
    categorizationChecker.validate();
    return this;
  }
}
