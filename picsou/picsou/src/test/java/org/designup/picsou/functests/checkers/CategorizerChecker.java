package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.MasterCategory;
import org.uispec4j.Window;

public class CategorizerChecker extends DataChecker {
  private Window mainWindow;
  private TransactionChecker transactions;
  private TransactionDetailsChecker transactionDetails;

  public CategorizerChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
    transactions = new TransactionChecker(mainWindow);
    transactionDetails = new TransactionDetailsChecker(mainWindow);
  }

  public void setRecurring(String label, String name, boolean showSeriesInitialization) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    if (index < 0) {
      Assert.fail(label + " not found");
    }
    CategorizationDialogChecker categorizationChecker = transactions.categorize(index);
    categorizationChecker.selectRecurring();
    categorizationChecker.selectRecurringSeries(name, showSeriesInitialization);
    categorizationChecker.validate();
  }

  public void setEnvelope(String label, String envelopName, MasterCategory category, boolean showSerieInitialization) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    CategorizationDialogChecker categorizationChecker = transactions.categorize(index);
    categorizationChecker.selectEnvelopes();
    categorizationChecker.selectEnvelopeSeries(envelopName, category, showSerieInitialization);
    categorizationChecker.validate();
  }

  public void setOccasional(String label, MasterCategory category) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    CategorizationDialogChecker categorizationChecker = transactions.categorize(index);
    categorizationChecker.selectOccasional();
    categorizationChecker.selectOccasionalSeries(category);
    categorizationChecker.validate();

  }

  public void setIncome(String label) {
    int index = transactions.getTable().getRowIndex(TransactionView.LABEL_COLUMN_INDEX, label);
    CategorizationDialogChecker categorizationChecker = transactions.categorize(index);
    categorizationChecker.selectIncome();
    categorizationChecker.selectIncomeSeries("salary", true);
    categorizationChecker.validate();
  }
}
