package com.budgetview.desktop.categorization.components;

import com.budgetview.desktop.categorization.actions.EditSeriesAction;
import com.budgetview.desktop.description.stringifiers.SeriesDescriptionStringifier;
import com.budgetview.model.*;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SeriesChooserComponentFactory implements RepeatComponentFactory<Glob> {
  protected JRadioButton invisibleSelector;
  protected ButtonGroup buttonGroup;

  protected GlobListStringifier seriesStringifier;
  protected GlobStringifier subSeriesStringifier;
  protected GlobStringifier budgetAreaStringifier;

  protected GlobRepository repository;
  protected Directory directory;
  protected Window parent;
  protected SelectionService selectionService;
  private Map<Key, SplitsNode<JRadioButton>> seriesToComponent = new HashMap<Key, SplitsNode<JRadioButton>>();

  private GlobList currentTransactions = GlobList.EMPTY;

  private BudgetArea budgetArea;

  public SeriesChooserComponentFactory(BudgetArea budgetArea,
                                       JRadioButton invisibleSelector,
                                       ButtonGroup buttonGroup,
                                       GlobRepository repository,
                                       Directory directory) {
    this.invisibleSelector = invisibleSelector;
    this.buttonGroup = buttonGroup;
    this.buttonGroup.add(invisibleSelector);

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

  public void registerComponents(PanelBuilder cellBuilder, final Glob series) {
    String seriesName = seriesStringifier.toString(new GlobList(series), repository);
    boolean updateTargetAccount = false;
    if (series.get(Series.TARGET_ACCOUNT) == null) {
      updateTargetAccount = true;
    }
    final Key seriesKey = series.getKey();
    final JRadioButton selector = createSeriesSelector(seriesName, seriesKey, null, updateTargetAccount ? series : null);
    buttonGroup.add(selector);

    final DefaultChangeSetListener seriesUpdateListener = new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(seriesKey)) {
          Glob series = repository.find(seriesKey);
          if (series != null) {
            String label = seriesStringifier.toString(new GlobList(series), repository);
            setText(selector, label, series);
            updateSeriesStyle(series);
          }
        }
        if (changeSet.containsChanges(Transaction.TYPE)) {
          GlobList transactions = selectionService.getSelection(Transaction.TYPE);
          updateToggleSelection(selector, transactions, seriesKey);
        }
      }
    };
    setText(selector, seriesName, series);
    repository.addChangeListener(seriesUpdateListener);

    final GlobSelectionListener listener = new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList transactions = selection.getAll(Transaction.TYPE);
        updateToggleSelection(selector, transactions, seriesKey);
      }
    };
    selectionService.addListener(listener, Transaction.TYPE);

    final SplitsNode<JRadioButton> splitsNode = cellBuilder.add("seriesToggle", selector);
    seriesToComponent.put(series.getKey(), splitsNode);
    JButton editSeriesButton = new JButton(new EditSeriesAction(seriesKey, repository, directory));
    editSeriesButton.setName("editSeries:" + seriesName);
    cellBuilder.add("editSeries", editSeriesButton);
    if (budgetArea == BudgetArea.OTHER) {
      editSeriesButton.setVisible(false);
    }

    cellBuilder.addDisposable(new Disposable() {
      public void dispose() {
        repository.removeChangeListener(seriesUpdateListener);
        selectionService.removeListener(listener);
        buttonGroup.remove(selector);
        SplitsNode<JRadioButton> radioButtonSplitsNode = seriesToComponent.get(series.getKey());
        if (radioButtonSplitsNode == splitsNode) {
          seriesToComponent.remove(seriesKey);
        }
      }
    });

    GlobsPanelBuilder.addRepeat("subSeriesRepeat", SubSeries.TYPE,
                                GlobMatchers.fieldEquals(SubSeries.SERIES, series.get(Series.ID)),
                                new GlobFieldsComparator(SubSeries.NAME, SubSeries.ID), repository, cellBuilder,
                                new SubSeriesComponentFactory(seriesName, "subSeriesSelector", budgetArea, updateTargetAccount ? series : null));

    updateToggleSelection(selector, selectionService.getSelection(Transaction.TYPE), seriesKey);
  }

  private void updateSeriesStyle(Glob series) {
    SplitsNode<JRadioButton> button = seriesToComponent.get(series.getKey());
    if (button == null) {
      return;
    }

    boolean atLeastOneIsActivated = false;
    ReadOnlyGlobRepository.MultiFieldIndexed seriesBudgets =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID));
    GlobList currentTransactions = selectionService.getSelection(Transaction.TYPE);
    Set<Integer> months = currentTransactions.getValueSet(Transaction.BUDGET_MONTH);
    for (Integer month : months) {
      Glob seriesBudget =
        seriesBudgets.findByIndex(SeriesBudget.MONTH, month).getGlobs().getFirst();
      if (seriesBudget != null && seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
        atLeastOneIsActivated = true;
        break;
      }
    }
    if (atLeastOneIsActivated) {
      button.applyStyle("SeriesActivated");
    }
    else {
      button.applyStyle("SeriesNotActivated");
    }
  }

  protected JRadioButton createSeriesSelector(final String label,
                                              final Key seriesKey,
                                              final Key subSeriesKey,
                                              final Glob series) {
    JRadioButton radio = new JRadioButton(new AbstractAction(label) {
      public void actionPerformed(ActionEvent e) {
        try {
          repository.startChangeSet();
          for (Glob transaction : currentTransactions) {
            if (series != null) {
              if (series.get(Series.TARGET_ACCOUNT) == null) {
                updateTargetSeries(transaction, seriesKey, SeriesChooserComponentFactory.this.repository);
              }
            }
            Key key = transaction.getKey();
            repository.setTarget(key, Transaction.SERIES, seriesKey);
            repository.setTarget(key, Transaction.SUB_SERIES, subSeriesKey);
            if (!Transaction.isManuallyCreated(transaction)) {
              repository.update(key, Transaction.RECONCILIATION_ANNOTATION_SET, true);
            }
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

  public static void updateTargetSeries(Glob transaction, Key seriesKey, final GlobRepository repository) {
    Integer targetAccountId = transaction.get(Transaction.ACCOUNT);
    Glob target = repository.find(Key.create(Account.TYPE, targetAccountId));
    if (target.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
      targetAccountId = target.get(Account.DEFERRED_TARGET_ACCOUNT);
    }
    if (targetAccountId != null) {
      repository.update(seriesKey, Series.TARGET_ACCOUNT, targetAccountId);
    }
  }

  private void updateToggleSelection(JToggleButton selector, GlobList transactions, Key seriesKey) {
    Glob selectorSeries = repository.find(seriesKey);
    if (selectorSeries == null) {
      return;
    }
    updateSeriesStyle(selectorSeries);

    Set<Integer> transactionSeriesKeys = transactions.getValueSet(Transaction.SERIES);
    Set<Integer> transactionSubSeriesKeys = transactions.getValueSet(Transaction.SUB_SERIES);
    if ((transactionSeriesKeys.size() != 1) || (transactionSubSeriesKeys.size() != 1)) {
      return;
    }


    Integer transactionSeriesKey = transactionSeriesKeys.iterator().next();

    Glob transactionSeries = repository.find(Key.create(Series.TYPE, transactionSeriesKey));
    if (transactionSeries.get(Series.BUDGET_AREA).equals(selectorSeries.get(Series.BUDGET_AREA)) &&
        !Series.UNCATEGORIZED_SERIES_ID.equals(transactionSeries.get(Series.ID))) {
      Integer transactionSubSeriesKey = transactionSubSeriesKeys.iterator().next();
      if (transactionSubSeriesKey == null) {
        boolean select = transactionSeries.getKey().equals(seriesKey);
        selector.setSelected(select);
      }
    }
    else {
      invisibleSelector.setSelected(true);
    }
  }

  private class SubSeriesComponentFactory implements RepeatComponentFactory<Glob> {
    private String seriesName;
    private String name;
    private BudgetArea budgetArea;
    private Glob series;

    public SubSeriesComponentFactory(String seriesName, String name, BudgetArea budgetArea, Glob series) {
      this.seriesName = seriesName;
      this.name = name;
      this.budgetArea = budgetArea;
      this.series = series;
    }

    public void registerComponents(PanelBuilder cellBuilder, final Glob subSeries) {
      final Key seriesKey = subSeries.getTargetKey(SubSeries.SERIES);
      String subSeriesName = subSeries.get(SubSeries.NAME);

      final Key subSeriesKey = subSeries.getKey();
      final JRadioButton selector = createSeriesSelector(subSeriesName, seriesKey, subSeriesKey, series);
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
      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          updater.dispose();
          buttonGroup.remove(selector);
          repository.removeChangeListener(subSeriesUpdateListener);
        }
      });
    }
  }

  private void setText(JRadioButton selector, String seriesName, Glob series) {
    selector.setText(seriesName);
    selector.setName(seriesName);
    selector.setToolTipText(SeriesDescriptionStringifier.toString(series));
  }

  private void setText(JRadioButton selector, String seriesName, String subSeriesName) {
    selector.setText(subSeriesName);
    selector.setName(seriesName + ":" + subSeriesName);
  }

}
