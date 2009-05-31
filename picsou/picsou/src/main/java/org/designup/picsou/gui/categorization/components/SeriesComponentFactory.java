package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SubSeries;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class SeriesComponentFactory extends AbstractSeriesComponentFactory {
  private BudgetArea budgetArea;

  public SeriesComponentFactory(BudgetArea budgetArea, JRadioButton invisibleSelector,
                                SeriesEditionDialog seriesEditionDialog,
                                GlobRepository localRepository,
                                Directory directory) {
    super(invisibleSelector, seriesEditionDialog, localRepository, directory);
    this.budgetArea = budgetArea;
  }

  public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
    String seriesLabel = seriesStringifier.toString(new GlobList(series), repository);
    final Key seriesKey = series.getKey();
    final JRadioButton selector = createSeriesSelector(seriesLabel, seriesKey, null);
    buttonGroup.add(selector);

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
          updateToggleSelection(selector, transactions, seriesKey);
        }
      }
    };
    repository.addChangeListener(seriesUpdateListener);

    final GlobSelectionListener listener = new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList transactions = selection.getAll(Transaction.TYPE);
        updateToggleSelection(selector, transactions, seriesKey);
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

    GlobsPanelBuilder.addRepeat("subSeriesRepeat", SubSeries.TYPE,
                                GlobMatchers.fieldEquals(SubSeries.SERIES, series.get(Series.ID)),
                                new GlobFieldComparator(SubSeries.ID), repository, cellBuilder,
                                new SubSeriesComponentFactory(seriesLabel, "subSeriesSelector", budgetArea));

    updateToggleSelection(selector, selectionService.getSelection(Transaction.TYPE), seriesKey);
  }

  private void updateToggleSelection(JToggleButton selector, GlobList transactions, Key seriesKey) {
    Set<Integer> transactionSeriesKeys = transactions.getValueSet(Transaction.SERIES);
    Set<Integer> transactionSubSeriesKeys = transactions.getValueSet(Transaction.SUB_SERIES);
    if ((transactionSeriesKeys.size() != 1) || (transactionSubSeriesKeys.size() != 1)) {
      return;
    }

    Integer transactionSubSeriesKey = transactionSubSeriesKeys.iterator().next();
    if (transactionSubSeriesKey != null) {
      return;
    }

    Glob selectorSeries = repository.find(seriesKey);
    if (selectorSeries == null) {
      return;
    }

    Integer transactionSeriesKey = transactionSeriesKeys.iterator().next();

    Glob transactionSeries = repository.find(Key.create(Series.TYPE, transactionSeriesKey));
    if (transactionSeries.get(Series.BUDGET_AREA).equals(selectorSeries.get(Series.BUDGET_AREA)) &&
        !Series.UNCATEGORIZED_SERIES_ID.equals(transactionSeries.get(Series.ID))) {
      boolean select = transactionSeries.getKey().equals(seriesKey);
      selector.setSelected(select);
    }
    else {
      invisibleSelector.setSelected(true);
    }
  }

  private class SubSeriesComponentFactory implements RepeatComponentFactory<Glob> {
    private String seriesName;
    private String name;
    private BudgetArea budgetArea;

    public SubSeriesComponentFactory(String seriesName, String name, BudgetArea budgetArea) {
      this.seriesName = seriesName;
      this.name = name;
      this.budgetArea = budgetArea;
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob subSeries) {
      final Key seriesKey = subSeries.getTargetKey(SubSeries.SERIES);
      String name = subSeries.get(SubSeries.NAME);

      final Key subSeriesKey = subSeries.getKey();
      final JRadioButton selector = createSeriesSelector(name, seriesKey, subSeriesKey);
      final DefaultChangeSetListener subSeriesUpdateListener = new DefaultChangeSetListener() {
        public void globsChanged(ChangeSet changeSet, GlobRepository repository1) {
          if (changeSet.containsChanges(subSeriesKey)) {
            Glob subSeries = repository1.find(subSeriesKey);
            if (subSeries != null) {
              selector.setText(subSeriesStringifier.toString(subSeries, repository1));
            }
          }
        }
      };
      repository.addChangeListener(subSeriesUpdateListener);
      selector.setName(seriesName + ":" + name);
      buttonGroup.add(selector);
      cellBuilder.add(this.name, selector);

      final SubSeriesUpdater updater =
        new SubSeriesUpdater(selector, invisibleSelector, seriesKey, subSeriesKey,
                             budgetArea, repository, selectionService);
      cellBuilder.addDisposeListener(new Disposable() {
        public void dispose() {
          updater.dispose();
          buttonGroup.remove(selector);
          repository.removeChangeListener(subSeriesUpdateListener);
        }
      });
    }
  }

}
