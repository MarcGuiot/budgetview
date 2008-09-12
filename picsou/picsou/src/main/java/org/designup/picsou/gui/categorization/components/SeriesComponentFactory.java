package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SeriesComponentFactory extends AbstractSeriesComponentFactory {
  ButtonGroup seriesGroup = new ButtonGroup();
  private DefaultChangeSetListener seriesUpdateListener;

  public SeriesComponentFactory(JToggleButton invisibleToggle, GlobRepository localRepository, Directory directory) {
    super(invisibleToggle, localRepository, directory);
  }

  public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
    String toggleLabel = seriesStringifier.toString(new GlobList(series), repository);
    final Key seriesKey = series.getKey();
    final Key categoryKey = series.getTargetKey(Series.DEFAULT_CATEGORY);
    final JToggleButton toggle = createSeriesToggle(toggleLabel, seriesKey, categoryKey);
    seriesUpdateListener = new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(seriesKey)) {
          Glob series = repository.find(seriesKey);
          if (series != null) {
            toggle.setText(seriesStringifier.toString(new GlobList(series), repository));
          }
        }
        if (changeSet.containsChanges(Transaction.TYPE)) {
          GlobList transactions = selectionService.getSelection(Transaction.TYPE);
          updateToggle(transactions, toggle, seriesKey);
        }
      }
    };
    repository.addChangeListener(seriesUpdateListener);
    seriesGroup.add(toggle);

    final GlobSelectionListener listener = new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList transactions = selection.getAll(Transaction.TYPE);
        updateToggle(transactions, toggle, seriesKey);
      }
    };
    selectionService.addListener(listener, Transaction.TYPE);

    cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
      public void dispose() {
        repository.removeChangeListener(seriesUpdateListener);
        selectionService.removeListener(listener);
        seriesGroup.remove(toggle);
      }
    });
    cellBuilder.add("seriesToggle", toggle);
  }

  private void updateToggle(GlobList transactions, JToggleButton toggle, Key seriesKey) {
    if (transactions.size() != 1) {
      return;
    }
    Glob transaction = transactions.get(0);
    Glob transactionSeries = repository.findLinkTarget(transaction, Transaction.SERIES);
    if (!Series.UNCATEGORIZED_SERIES_ID.equals(transactionSeries.get(Series.ID))) {
      toggle.setSelected(transactionSeries.getKey().equals(seriesKey));
    }
    else {
      invisibleToggle.setSelected(true);
    }
  }
}
