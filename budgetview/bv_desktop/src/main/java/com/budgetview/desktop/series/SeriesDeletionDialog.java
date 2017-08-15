package com.budgetview.desktop.series;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.description.stringifiers.SeriesNameComparator;
import com.budgetview.model.Account;
import com.budgetview.model.Month;
import com.budgetview.model.Series;
import com.budgetview.model.Transaction;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.gui.views.GlobListViewFilter;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.SortedSet;

import static org.globsframework.model.FieldValue.value;

public class SeriesDeletionDialog {
  private PicsouDialog dialog;
  private Action action;
  private GlobsPanelBuilder builder;
  private final Glob currentSeries;
  private GlobRepository repository;
  private Directory localDirectory;

  public enum Action {
    DELETE,
    SET_DATE,
    CANCEL
  }

  public SeriesDeletionDialog(Glob currentSeries, GlobList transactionsForSeries,
                              GlobRepository repository,
                              Directory directory,
                              Window parent) {
    this.currentSeries = currentSeries;
    this.repository = repository;

    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());

    builder = new GlobsPanelBuilder(SeriesDeletionDialog.class, "/layout/series/seriesDeletionDialog.splits",
                                    repository, localDirectory);

    JEditorPane introMessage = GuiUtils.createReadOnlyHtmlComponent(Lang.get("seriesDeletion.message", currentSeries.get(Series.NAME)));
    builder.add("introMessage", introMessage);

    SortedSet<Integer> transactionMonths = transactionsForSeries.getSortedSet(Transaction.MONTH);
    Integer firstMonth = transactionMonths.first();
    Integer lastMonth = transactionMonths.last();

    // Transfer
    builder.add("transfer", new TransferAction(transactionsForSeries, firstMonth));
    GlobListView seriesList = builder.addList("seriesList", Series.TYPE).setComparator(new SeriesNameComparator());
    builder.add("seriesFilter", GlobListViewFilter.init(seriesList)
      .setDefaultMatcher(new SeriesFilter(currentSeries.get(Series.TARGET_ACCOUNT))));

    // Set end date
    builder.add("setEndDate", new SetEndDateAction(lastMonth));
    JEditorPane setEndDateMessage =
      GuiUtils.createReadOnlyHtmlComponent(Lang.get("seriesDeletion.setEndDate.message",
                                                    Month.getFullLabel(lastMonth, true)));
    builder.add("setEndDateMessage", setEndDateMessage);

    // Uncategorize
    builder.add("uncategorize", new UncategorizeAction());

    dialog = PicsouDialog.createWithButton(this, parent, true, builder.<JPanel>load(),
                                           new AbstractAction(Lang.get("cancel")) {
                                             public void actionPerformed(ActionEvent e) {
                                               cancel();
                                             }
                                           }, directory);
  }

  private class TransferAction extends AbstractAction implements GlobSelectionListener {
    private Glob targetSeries;
    private GlobList transactionsForSeries;
    private Integer firstMonth;

    private TransferAction(GlobList transactionsForSeries, Integer firstMonth) {
      super(Lang.get("seriesDeletion.transfer.button"));
      this.transactionsForSeries = transactionsForSeries;
      this.firstMonth = firstMonth;
      localDirectory.get(SelectionService.class).addListener(this, Series.TYPE);
      updateSelection();
    }

    public void selectionUpdated(GlobSelection selection) {
      updateSelection();
    }

    private void updateSelection() {
      GlobList selectedSeries = localDirectory.get(SelectionService.class).getSelection(Series.TYPE);
      if (selectedSeries.isEmpty() || selectedSeries.size() > 1) {
        targetSeries = null;
      }
      else {
        targetSeries = selectedSeries.getFirst();
      }
      setEnabled(targetSeries != null);
    }

    public void actionPerformed(ActionEvent actionEvent) {
      repository.startChangeSet();
      Integer targetSeriesId = targetSeries.get(Series.ID);
      updateTargetAccount(transactionsForSeries);
      for (Glob transaction : transactionsForSeries) {
        repository.update(transaction.getKey(),
                          value(Transaction.SERIES, targetSeriesId),
                          value(Transaction.SUB_SERIES, null));
      }
      updateStartDate();
      updateEndDate();
      repository.completeChangeSet();
      action = Action.DELETE;
      dialog.setVisible(false);
    }

    private void updateStartDate() {
      if (targetSeries.get(Series.FIRST_MONTH) != null) {
        repository.update(targetSeries.getKey(), Series.FIRST_MONTH, firstMonth);
      }
    }

    private void updateEndDate() {
      if (targetSeries.get(Series.LAST_MONTH) != null) {
        repository.update(targetSeries.getKey(), Series.LAST_MONTH, null);
      }
    }

    private void updateTargetAccount(GlobList transactions) {
      if (Utils.equal(targetSeries.get(Series.TARGET_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
        return;
      }
      Set<Integer> accountIds = transactions.getValueSet(Transaction.ACCOUNT);
      if ((accountIds.size() > 1) ||
          (accountIds.size() == 1 && !Utils.equal(accountIds.iterator().next(), targetSeries.get(Series.TARGET_ACCOUNT)))) {
        repository.update(targetSeries, Series.TARGET_ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID);
      }
    }
  }

  private class SetEndDateAction extends AbstractAction {
    private Integer lastMonth;

    private SetEndDateAction(Integer lastMonth) {
      super(Lang.get("seriesDeletion.setEndDate.button"));
      this.lastMonth = lastMonth;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      repository.update(currentSeries.getKey(), Series.LAST_MONTH, lastMonth);
      action = Action.SET_DATE;
      dialog.setVisible(false);
    }
  }

  private class UncategorizeAction extends AbstractAction {
    private UncategorizeAction() {
      super(Lang.get("seriesDeletion.uncategorize.button"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      action = Action.DELETE;
      dialog.setVisible(false);
    }
  }

  private void cancel() {
    action = Action.CANCEL;
    dialog.setVisible(false);
  }

  public Action show() {
    action = null;
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
    return action;
  }

  private class SeriesFilter implements GlobMatcher {
    private Integer referenceAccount;
    private final boolean referenceIsMain;

    public SeriesFilter(Integer referenceAccount) {
      this.referenceAccount = referenceAccount;
      this.referenceIsMain = Account.isMain(referenceAccount, repository);
    }

    public boolean matches(Glob otherSeries, GlobRepository repository) {
      if (otherSeries == null ||
          otherSeries.getKey().equals(currentSeries.getKey()) ||
          BudgetArea.TRANSFER.getId().equals(otherSeries.get(Series.BUDGET_AREA)) ||
          otherSeries.getKey().get(Series.ID).equals(Series.ACCOUNT_SERIES_ID)) {
        return false;
      }

      Integer otherTargetAccount = otherSeries.get(Series.TARGET_ACCOUNT);
      if (Utils.equal(otherTargetAccount, referenceAccount)) {
        return true;
      }
      if (referenceIsMain && Utils.equal(otherTargetAccount, Account.MAIN_SUMMARY_ACCOUNT_ID)) {
        return true;
      }
      Glob otherAccount = repository.find(org.globsframework.model.Key.create(Account.TYPE, otherTargetAccount));
      boolean otherIsMain = Account.isMain(otherAccount);
      if (referenceIsMain && otherIsMain) {
        return true;
      }
      return false;
    }
  }
}
