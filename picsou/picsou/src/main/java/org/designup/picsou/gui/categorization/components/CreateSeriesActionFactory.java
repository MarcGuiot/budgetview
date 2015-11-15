package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.categorization.CategorizationSelector;
import org.designup.picsou.gui.categorization.utils.FilteredRepeats;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageType;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class CreateSeriesActionFactory {

  private final FilteredRepeats seriesRepeat;
  private final CategorizationSelector categorizationSelector;
  private final SelectionService selectionService;
  private final GlobRepository repository;
  private final Directory directory;

  public CreateSeriesActionFactory(FilteredRepeats seriesRepeat, CategorizationSelector categorizationSelector, SelectionService selectionService, GlobRepository repository, Directory directory) {
    this.seriesRepeat = seriesRepeat;
    this.categorizationSelector = categorizationSelector;
    this.selectionService = selectionService;
    this.repository = repository;
    this.directory = directory;
  }

  public Action createAction(BudgetArea budgetArea, FieldValue... forcedValues) {
    return new CreateSeriesAction(budgetArea, forcedValues);
  }

  public class CreateSeriesAction extends AbstractAction {
    private final BudgetArea budgetArea;
    private FieldValue[] forcedValues;

    public CreateSeriesAction(BudgetArea budgetArea, FieldValue... forcedValues) {
      super(Lang.get("categorization.series.add"));
      this.budgetArea = budgetArea;
      this.forcedValues = forcedValues;
    }

    public void actionPerformed(ActionEvent e) {
      GlobList currentTransactions = categorizationSelector.getCurrentTransactions();

      if (budgetArea != BudgetArea.TRANSFER && containsMainAndSavings(currentTransactions)) {
        showError("categorization.createSeries.mainAndSavings.message");
        return;
      }
      if (budgetArea != BudgetArea.TRANSFER && containsDifferentSavings(currentTransactions)) {
        showError("categorization.createSeries.differentSavings.message");
        return;
      }
      if (budgetArea == BudgetArea.TRANSFER && currentTransactions.getValueSet(Transaction.ACCOUNT).size() > 2) {
        showError("categorization.createSeries.maxTwoAccountsForTransfer.message");
        return;
      }
      if (budgetArea == BudgetArea.TRANSFER && containsInvalidSignsForTransfer(currentTransactions)) {
        showError("categorization.createSeries.incoherentSigns.message");
        return;
      }

      Key key = SeriesEditor.get(directory).showNewSeries(currentTransactions,
                                                          selectionService.getSelection(Month.TYPE),
                                                          budgetArea,
                                                          forcedValues);
      Glob series = repository.find(key);
      if (key != null && series != null) {
        try {
          repository.startChangeSet();
          for (Glob transaction : categorizationSelector.getCurrentTransactions()) {
            if (!categorize(series, transaction)) {
              Glob mirrorSeries = repository.findLinkTarget(series, Series.MIRROR_SERIES);
              if (mirrorSeries != null) {
                categorize(mirrorSeries, transaction);
              }
            }
          }
        }
        finally {
          repository.completeChangeSet();
        }
      }
    }

    public void showError(String messageKey) {
      MessageDialog.show("categorization.createSeries.error.title",
                         MessageType.INFO,
                         directory,
                         messageKey);
    }

    private boolean containsMainAndSavings(GlobList currentTransactions) {
      boolean containsMain = false;
      boolean containsSavings = false;
      for (Glob currentTransaction : currentTransactions) {
        Integer accountId = currentTransaction.get(Transaction.ACCOUNT);
        containsMain |= Account.isMain(accountId, repository);
        containsSavings |= Account.isSavings(accountId, repository);
        if (containsMain && containsSavings) {
          return true;
        }
      }
      return false;
    }

    private boolean containsDifferentSavings(GlobList currentTransactions) {
      boolean containsSavings = false;
      for (Integer accountId : currentTransactions.getValueSet(Transaction.ACCOUNT)) {
        if (Account.isSavings(accountId, repository)) {
          if (containsSavings) {
            return true;
          }
          containsSavings = true;
        }
      }
      return false;
    }

    private boolean containsInvalidSignsForTransfer(GlobList currentTransactions) {
      Integer[] accountIds = currentTransactions.getValueSetArray(Transaction.ACCOUNT);
      if (accountIds.length != 2) {
        return false;
      }
      Integer account1 = accountIds[0];
      Integer account2 = accountIds[1];
      int account1Pos = 0;
      int account1Neg = 0;
      int account2Pos = 0;
      int account2Neg = 0;
      for (Glob transaction : currentTransactions) {
        boolean positive = transaction.get(Transaction.AMOUNT) > 0;
        if (Utils.equal(account1, transaction.get(Transaction.ACCOUNT))) {
          if (positive) {
            account1Pos++;
          }
          else {
            account1Neg++;
          }
        }
        if (Utils.equal(account2, transaction.get(Transaction.ACCOUNT))) {
          if (positive) {
            account2Pos++;
          }
          else {
            account2Neg++;
          }
        }
      }

      return (account1Pos > 0 && account1Neg > 0) ||
             (account2Pos > 0 && account2Neg > 0) ||
             (account1Pos > 0 && account2Pos > 0) ||
             (account1Neg > 0 && account2Neg > 0);
    }

    private boolean categorize(Glob series, final Glob transaction) {
      boolean matchFound = seriesRepeat.updateAndCheckMatch(series, transaction);
      if (matchFound) {
        return false;
      }
      Integer subSeriesId = SeriesEditor.get(directory).getLastSelectedSubSeriesId();
      if (series.get(Series.TARGET_ACCOUNT) == null) {
        SeriesChooserComponentFactory.updateTargetSeries(transaction, series.getKey(), repository);
      }
      repository.update(transaction.getKey(),
                        value(Transaction.SERIES, series.get(Series.ID)),
                        value(Transaction.SUB_SERIES, subSeriesId),
                        value(Transaction.RECONCILIATION_ANNOTATION_SET, !Transaction.isManuallyCreated(transaction)));
      return true;
    }
  }

}
