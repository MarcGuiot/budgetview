package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class CategorizationSeriesComponentFactory implements RepeatComponentFactory<Glob> {
  protected JRadioButton invisibleSelector;
  protected ButtonGroup buttonGroup = new ButtonGroup();

  protected GlobListStringifier seriesStringifier;
  protected GlobStringifier subSeriesStringifier;
  protected GlobStringifier budgetAreaStringifier;

  protected SeriesEditionDialog seriesEditionDialog;
  protected GlobRepository repository;
  protected Directory directory;
  protected Window parent;
  protected SelectionService selectionService;

  protected GlobList currentTransactions = GlobList.EMPTY;

  private BudgetArea budgetArea;

  public CategorizationSeriesComponentFactory(BudgetArea budgetArea,
                                              JRadioButton invisibleSelector,
                                              SeriesEditionDialog seriesEditionDialog,
                                              GlobRepository repository,
                                              Directory directory) {
    this.invisibleSelector = invisibleSelector;
    this.buttonGroup.add(invisibleSelector);

    this.seriesEditionDialog = seriesEditionDialog;
    this.repository = repository;
    this.directory = directory;
    this.parent = directory.get(JFrame.class);

    DescriptionService descriptionService = directory.get(DescriptionService.class);
    seriesStringifier = descriptionService.getListStringifier(Series.TYPE);
    subSeriesStringifier = descriptionService.getStringifier(SubSeries.TYPE);
    budgetAreaStringifier = descriptionService.getStringifier(BudgetArea.TYPE);

    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        currentTransactions = selection.getAll(Transaction.TYPE);
      }
    }, Transaction.TYPE);
    this.budgetArea = budgetArea;
  }

  public void registerComponents(RepeatCellBuilder cellBuilder, final Glob series) {
    String seriesName = seriesStringifier.toString(new GlobList(series), repository);
    final Key seriesKey = series.getKey();
    final JRadioButton selector = createSeriesSelector(seriesName, seriesKey, null);
    buttonGroup.add(selector);

    final DefaultChangeSetListener seriesUpdateListener = new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(seriesKey)) {
          Glob series = repository.find(seriesKey);
          if (series != null) {
            String label = seriesStringifier.toString(new GlobList(series), repository);
            setText(selector, label);
          }
        }
        if (changeSet.containsChanges(Transaction.TYPE)) {
          GlobList transactions = selectionService.getSelection(Transaction.TYPE);
          updateToggleSelection(selector, transactions, seriesKey);
        }
      }
    };
    setText(selector, seriesName);
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
    editSeriesButton.setName("editSeries:" + seriesName);
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
                                new SubSeriesComponentFactory(seriesName, "subSeriesSelector", budgetArea));

    updateToggleSelection(selector, selectionService.getSelection(Transaction.TYPE), seriesKey);
  }

  protected JRadioButton createSeriesSelector(final String label,
                                              final Key seriesKey,
                                              final Key subSeriesKey) {
    JRadioButton radio = new JRadioButton(new AbstractAction(label) {
      public void actionPerformed(ActionEvent e) {
        try {
          repository.startChangeSet();
          for (Glob transaction : currentTransactions) {
            repository.setTarget(transaction.getKey(), Transaction.SERIES, seriesKey);
            repository.setTarget(transaction.getKey(), Transaction.SUB_SERIES, subSeriesKey);
          }
        }
        finally {
          repository.completeChangeSet();
        }
      }
    });
    radio.setName(label);
    return radio;
  }

  protected class EditSeriesAction extends AbstractAction {

    private Key seriesKey;

    protected EditSeriesAction(Key seriesKey) {
      this.seriesKey = seriesKey;
    }

    public void actionPerformed(ActionEvent e) {
      Glob series = repository.get(seriesKey);
      seriesEditionDialog.show(series, selectionService.getSelection(Month.TYPE).getValueSet(Month.ID));
    }
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
      String subSeriesName = subSeries.get(SubSeries.NAME);

      final Key subSeriesKey = subSeries.getKey();
      final JRadioButton selector = createSeriesSelector(subSeriesName, seriesKey, subSeriesKey);
      final DefaultChangeSetListener subSeriesUpdateListener = new DefaultChangeSetListener() {
        public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
          if (changeSet.containsChanges(subSeriesKey)) {
            Glob subSeries = repository.find(subSeriesKey);
            if (subSeries != null) {
              setText(selector, seriesName, subSeriesStringifier.toString(subSeries, repository));
            }
          }
        }
      };
      repository.addChangeListener(subSeriesUpdateListener);
      setText(selector, seriesName, subSeriesName);
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

  private void setText(JRadioButton selector, String seriesName) {
    selector.setText(seriesName);
    selector.setName(seriesName);
  }

  private void setText(JRadioButton selector, String seriesName, String subSeriesName) {
    selector.setText(subSeriesName);
    selector.setName(seriesName + ":" + subSeriesName);
  }

}
