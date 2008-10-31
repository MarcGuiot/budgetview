package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SingleCategorySeriesComponentFactory extends AbstractSeriesComponentFactory {

  public SingleCategorySeriesComponentFactory(JRadioButton invisibleSelector, SeriesEditionDialog seriesEditionDialog,
                                              GlobRepository localRepository, Directory directory) {
    super(invisibleSelector, seriesEditionDialog, localRepository, directory);
  }

  public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
    String seriesLabel = seriesStringifier.toString(new GlobList(series), repository);
    final Key seriesKey = series.getKey();
    final Key categoryKey = series.getTargetKey(Series.DEFAULT_CATEGORY);
    final JRadioButton selector = createSeriesSelector(seriesLabel, seriesKey, categoryKey);

    final DefaultChangeSetListener seriesUpdateListener = new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(seriesKey)) {
          Glob series = repository.find(seriesKey);
          if (series != null) {
            selector.setText(seriesStringifier.toString(new GlobList(series), repository));
          }
        }
        if (changeSet.containsChanges(Transaction.TYPE)) {
          GlobList transactions = selectionService.getSelection(Transaction.TYPE);
          updateSelector(selector, transactions, seriesKey);
        }
      }
    };
    repository.addChangeListener(seriesUpdateListener);

    final GlobSelectionListener listener = new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList transactions = selection.getAll(Transaction.TYPE);
        updateSelector(selector, transactions, seriesKey);
      }
    };
    selectionService.addListener(listener, Transaction.TYPE);

    cellBuilder.add("seriesToggle", selector);

    JButton editSeriesButton = new JButton(new EditSeriesAction(seriesKey));
    editSeriesButton.setName("editSeries:" + seriesLabel);
    cellBuilder.add("editSeries", editSeriesButton);

    cellBuilder.addDisposeListener(new Disposable() {
      public void dispose() {
        repository.removeChangeListener(seriesUpdateListener);
        selectionService.removeListener(listener);
        buttonGroup.remove(selector);
      }
    });

    updateSelector(selector, selectionService.getSelection(Transaction.TYPE), seriesKey);
  }

  private void updateSelector(JToggleButton selector, GlobList transactions, Key seriesKey) {
    if (transactions.size() != 1) {
      return;
    }

    Glob selectorSeries = repository.find(seriesKey);
    if (selectorSeries == null) {
      return;
    }

    Glob transactionSeries = repository.findLinkTarget(transactions.get(0), Transaction.SERIES);
    if (transactionSeries.get(Series.BUDGET_AREA).equals(selectorSeries.get(Series.BUDGET_AREA)) &&
        !Series.UNCATEGORIZED_SERIES_ID.equals(transactionSeries.get(Series.ID))) {
      boolean select = transactionSeries.getKey().equals(seriesKey);
      selector.setSelected(select);
    }
    else {
      invisibleSelector.setSelected(true);
    }
  }
}
