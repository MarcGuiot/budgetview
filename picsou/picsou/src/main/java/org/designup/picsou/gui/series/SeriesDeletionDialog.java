package org.designup.picsou.gui.series;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
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
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.SortedSet;

public class SeriesDeletionDialog {
  private PicsouDialog dialog;
  private boolean ok;
  private GlobsPanelBuilder builder;
  private final Glob currentSeries;
  private GlobRepository repository;
  private Directory localDirectory;

  public SeriesDeletionDialog(Glob currentSeries, GlobList transactionsForSeries,
                              GlobRepository repository,
                              Directory directory,
                              Window parent) {
    this.currentSeries = currentSeries;
    this.repository = repository;

    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());

    builder = new GlobsPanelBuilder(SeriesEditionDialog.class, "/layout/series/seriesDeletionDialog.splits",
                                    repository, localDirectory);

    JEditorPane introMessage = GuiUtils.createReadOnlyHtmlComponent(Lang.get("seriesDeletion.message", currentSeries.get(Series.NAME)));
    builder.add("introMessage", introMessage);

    SortedSet<Integer> transactionMonths = transactionsForSeries.getSortedSet(Transaction.MONTH);
    Integer firstMonth = transactionMonths.first();
    Integer lastMonth = transactionMonths.last();

    // Transfer
    builder.add("transfer", new TransferAction(transactionsForSeries, firstMonth));
    GlobListView seriesList = builder.addList("seriesList", Series.TYPE);
    builder.add("seriesFilter", GlobListViewFilter.init(seriesList).setDefaultMatcher(new SeriesFilter()));

    // Set end date
    builder.add("setEndDate", new SetEndDateAction(lastMonth));
    JEditorPane setEndDateMessage =
      GuiUtils.createReadOnlyHtmlComponent(Lang.get("seriesDeletion.setEndDate.message",
                                                    Month.getFullLabel(lastMonth)));
    builder.add("setEndDateMessage", setEndDateMessage);

    // Uncategorize
    builder.add("uncategorize", new UncategorizeAction());

    dialog = PicsouDialog.createWithButton(parent, true, builder.<JPanel>load(),
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
      for (Glob transaction : transactionsForSeries) {
        repository.update(transaction.getKey(), Transaction.SERIES, targetSeriesId);
      }
      updateStartDate();
      updateEndDate();
      repository.completeChangeSet();
      ok = true;
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
  }

  private class SetEndDateAction extends AbstractAction {
    private Integer lastMonth;

    private SetEndDateAction(Integer lastMonth) {
      super(Lang.get("seriesDeletion.setEndDate.button"));
      this.lastMonth = lastMonth;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      repository.update(currentSeries.getKey(), Series.LAST_MONTH, lastMonth);
      ok = false;
      dialog.setVisible(false);
    }
  }

  private class UncategorizeAction extends AbstractAction {
    private UncategorizeAction() {
      super(Lang.get("seriesDeletion.uncategorize.button"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      ok = true;
      dialog.setVisible(false);
    }
  }

  private void cancel() {
    ok = false;
    dialog.setVisible(false);
  }

  public boolean show() {
    ok = false;
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
    return ok;
  }

  private class SeriesFilter implements GlobMatcher {
    public boolean matches(Glob series, GlobRepository repository) {
      return (series != null)
             && !series.getKey().equals(currentSeries.getKey())
             && !BudgetArea.SAVINGS.getId().equals(series.get(Series.BUDGET_AREA));
    }
  }
}
