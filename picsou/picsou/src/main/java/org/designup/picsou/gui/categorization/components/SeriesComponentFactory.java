package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SeriesComponentFactory extends AbstractSeriesComponentFactory {
  ButtonGroup seriesGroup = new ButtonGroup();

  public SeriesComponentFactory(JToggleButton invisibleToggle, GlobRepository localRepository, Directory directory) {
    super(invisibleToggle, localRepository, directory);
  }

  public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
    String toggleLabel = seriesStringifier.toString(series, repository);
    final Key seriesKey = series.getKey();
    final Key categoryKey = series.getTargetKey(Series.DEFAULT_CATEGORY);
    final JToggleButton toggle = createSeriesToggle(toggleLabel, seriesKey, categoryKey);
    seriesGroup.add(toggle);

    final GlobSelectionListener listener = new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList transactions = selection.getAll(Transaction.TYPE);
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
    };
    selectionService.addListener(listener, Transaction.TYPE);

    cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
      public void dispose() {
        selectionService.removeListener(listener);
        seriesGroup.remove(toggle);
      }
    });
    cellBuilder.add("seriesToggle", toggle);
  }
}
