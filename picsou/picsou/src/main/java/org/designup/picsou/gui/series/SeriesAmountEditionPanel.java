package org.designup.picsou.gui.series;

import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.description.SeriesPeriodicityAndScopeStringifier;
import org.designup.picsou.gui.series.edition.AlignSeriesBudgetAmountsAction;
import org.designup.picsou.gui.series.edition.SeriesBudgetSliderAdapter;
import org.designup.picsou.gui.series.evolution.SeriesAmountChartPanel;
import org.designup.picsou.gui.series.utils.SeriesAmountLabelStringifier;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import static org.globsframework.model.utils.GlobFunctors.update;
import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesAmountEditionPanel {

  private JPanel panel;
  private AmountEditor amountEditor;
  private SelectionService selectionService;
  private JCheckBox propagationCheckBox;

  private GlobRepository repository;

  private Key currentSeries;
  private Integer currentMonth;
  private Set<Integer> selectedMonthIds;
  private Directory directory;
  private boolean selectionInProgress;
  private SeriesAmountChartPanel chart;
  private boolean autoSelectFutureMonths;
  private SeriesEditorAccess seriesEditorAccess;
  private SeriesAmountLabelStringifier selectionStringifier = new SeriesAmountLabelStringifier();

  public interface SeriesEditorAccess {
    void openSeriesEditor(Key series, Set<Integer> selectedMonthIds);
  }

  public SeriesAmountEditionPanel(GlobRepository repository,
                                  Directory directory) {
    this(repository, directory, null);
  }

  public SeriesAmountEditionPanel(GlobRepository repository,
                                  Directory directory,
                                  SeriesEditorAccess seriesEditorAccess) {

    this.repository = repository;
    this.directory = directory;
    this.seriesEditorAccess = seriesEditorAccess;

    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        if (selection.isRelevantForType(Series.TYPE)) {
          Glob first = selection.getAll(Series.TYPE).getFirst();
          if (first == null) {
            clear();
          }
          else {
            changeSeries(first.getKey());
          }
          updateBudgetFromMonth();
        }
        if (selection.isRelevantForType(Month.TYPE)) {
          if (currentSeries != null) {
            Set<Integer> selectedMonths = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
            doSelectMonths(SeriesAmountEditionPanel.this.repository.find(currentSeries), selectedMonths);
          }
        }
      }
    }, Month.TYPE, Series.TYPE);

    createPanel();

    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if ((currentSeries != null) && changeSet.containsChanges(currentSeries)
            && repository.find(currentSeries) != null) {
          FieldValues previousValue = changeSet.getPreviousValue(currentSeries);
          if (previousValue.contains(Series.FROM_ACCOUNT) || previousValue.contains(Series.TO_ACCOUNT)) {
            updatePositiveOrNegativeRadio();
          }
        }
      }
    });
    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsUpdates(SeriesBudget.AMOUNT)) {
          Glob series = repository.get(currentSeries);
          if (series.isTrue(Series.IS_AUTOMATIC)) {
            repository.update(currentSeries, Series.IS_AUTOMATIC, false);
          }
        }
      }
    });
  }

  public JPanel getPanel() {
    return panel;
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesAmountEditionPanel.class,
                                                      "/layout/series/seriesAmountEditionPanel.splits",
                                                      repository, directory);

    builder.addLabel("dateLabel", SeriesBudget.TYPE, selectionStringifier);

    chart = new SeriesAmountChartPanel(repository, directory);
    builder.add("chart", chart.getChart());

    amountEditor = new AmountEditor(SeriesBudget.AMOUNT, repository, directory, true, 0.0);
    builder.add("amountEditor", amountEditor.getPanel());

    propagationCheckBox = new JCheckBox();
    propagationCheckBox.getModel().addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        updateAutoSelect(propagationCheckBox.isSelected());
      }
    });
    builder.add("propagate", propagationCheckBox);

    AlignSeriesBudgetAmountsAction alignAction = new AlignSeriesBudgetAmountsAction(repository, directory);
    builder.add("alignValue", alignAction);
    builder.add("actualAmountLabel", alignAction.getActualAmountLabel());

    builder.addSlider("slider",
                      SeriesBudget.AMOUNT,
                      new SeriesBudgetSliderAdapter(amountEditor, repository));


    final JButton editSeriesButton;
    if (seriesEditorAccess != null) {
      editSeriesButton = GlobButtonView.init(Series.TYPE, repository, directory,
                                             new SeriesPeriodicityAndScopeStringifier(),
                                             new OpenSeriesEditorCallback())
        .getComponent();
    }
    else {
      editSeriesButton = new JButton();
    }
    builder.add("editSeries", editSeriesButton);

    builder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
        editSeriesButton.setVisible(seriesEditorAccess != null);
      }
    });

    this.panel = builder.load();
  }

  public void setOkAction(Action action) {
    amountEditor.addAction(action);
  }

  public JTextField getFocusComponent() {
    return amountEditor.getNumericEditor().getComponent();
  }

  public void selectAmountEditor() {
    getFocusComponent().requestFocus();
  }

  public void clear() {
    changeSeries(null);
    selectionService.clear(SeriesBudget.TYPE);
    chart.init(null, null);
  }

  public void changeSeries(Key seriesKey) {
    propagationCheckBox.setEnabled(seriesKey != null);
    chart.getChart().setEnabled(seriesKey != null);
    amountEditor.setEnabled(seriesKey != null);
    currentSeries = seriesKey;
    setAutoSelectFutureMonths(false);
    propagationCheckBox.setSelected(false);
  }

  private void setAutoSelectFutureMonths(boolean enabled) {
    this.autoSelectFutureMonths = enabled;
    this.selectionStringifier.setAutoSelectFutureMonths(enabled);
  }

  public void selectMonths(Set<Integer> months) {

    if (months == null || months.isEmpty()) {
      return;
    }
    selectedMonthIds = months;
    currentMonth = Utils.min(months);

    updateBudgetFromMonth();
  }

  private void updateBudgetFromMonth() {
    if (currentSeries == null) {
      return;
    }
    Glob series = repository.get(currentSeries);
    currentSeries = series.getKey();

    chart.init(currentSeries.get(Series.ID), currentMonth);

    updatePositiveOrNegativeRadio();

    doSelectMonths(series, selectedMonthIds);

    amountEditor.selectAll();
  }

  private void updatePositiveOrNegativeRadio() {
    Glob series = repository.get(currentSeries);
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    double multiplier = Account.getMultiplierForInOrOutputOfTheAccount(series);
    boolean isUsuallyPositive = budgetArea.isIncome() ||
                                (budgetArea == BudgetArea.SAVINGS && multiplier > 0);
    amountEditor.update(isUsuallyPositive, budgetArea == BudgetArea.SAVINGS);
  }

  private void doSelectMonths(Glob series, Set<Integer> monthIds) {
    if (selectionInProgress) {
      return;
    }
    selectionInProgress = true;

    try {
      GlobSelectionBuilder selection = GlobSelectionBuilder.init();
      Integer seriesId = series.get(Series.ID);
      boolean selectionOK = false;
      for (Integer monthId : getMonths(monthIds)) {
        GlobList list = getBudget(seriesId, monthId);
        if (!list.isEmpty()) {
          selection.add(list, SeriesBudget.TYPE);
          selection.add(repository.get(Key.create(Month.TYPE, monthId)));
          selectionOK = true;
        }
      }
      if (!selectionOK) {
        Integer firstMonth = monthIds.iterator().next();
        int previous = firstMonth;
        int next = firstMonth;
        GlobList list = repository.getAll(Month.TYPE);
        int realFirst = list.getFirst().get(Month.ID);
        int realLast = list.getLast().get(Month.ID);

        while (!selectionOK && (previous > realFirst || next < realLast)) {
          if (previous > realFirst) {
            previous = Month.previous(previous);
            GlobList budget = getBudget(seriesId, previous);
            if (!budget.isEmpty()) {
              selectionOK = true;
              selection.add(budget, SeriesBudget.TYPE);
              selection.add(repository.get(Key.create(Month.TYPE, previous)));
            }
          }
          if (next < realLast) {
            next = Month.next(next);
            GlobList budget = getBudget(seriesId, previous);
            if (!budget.isEmpty()) {
              selectionOK = true;
              selection.add(budget, SeriesBudget.TYPE);
              selection.add(repository.get(Key.create(Month.TYPE, next)));
            }
          }
        }
      }
      selectionService.select(selection.get());
    }
    finally {
      selectionInProgress = false;
    }
  }

  private GlobList getBudget(Integer seriesId, Integer monthId) {
    return SeriesBudget.getAll(seriesId, monthId, repository)
      .filterSelf(GlobMatchers.isTrue(SeriesBudget.ACTIVE), repository);
  }

  private void updateAutoSelect(boolean enabled) {

    setAutoSelectFutureMonths(enabled);

    SortedSet<Integer> monthIds = selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
    if (monthIds.isEmpty()) {
      return;
    }

    Integer firstMonthId = monthIds.first();

    if (autoSelectFutureMonths) {
      propagateValue(firstMonthId);
      GlobList nextMonths =
        repository.getAll(Month.TYPE, fieldGreaterOrEqual(Month.ID, firstMonthId));
      selectionService.select(nextMonths, Month.TYPE);
    }
    else {
      Glob firstMonth = repository.get(Key.create(Month.TYPE, firstMonthId));
      selectionService.select(firstMonth);
    }
  }

  private Collection<Integer> getMonths(Set<Integer> monthIds) {
    if (!autoSelectFutureMonths || monthIds.isEmpty()) {
      return monthIds;
    }

    GlobList nextMonths =
      repository.getAll(Month.TYPE, fieldGreaterOrEqual(Month.ID, Utils.min(monthIds)));
    return nextMonths.getValueSet(Month.ID);
  }

  private void propagateValue(Integer startMonth) {
    final Double amount = amountEditor.getValue();
    repository.safeApply(SeriesBudget.TYPE,
                         and(
                           fieldEquals(SeriesBudget.SERIES, currentSeries.get(Series.ID)),
                           isTrue(SeriesBudget.ACTIVE),
                           fieldGreaterOrEqual(SeriesBudget.MONTH, startMonth)),
                         update(SeriesBudget.AMOUNT, Utils.zeroIfNull(amount)));
  }

  public class OpenSeriesEditorCallback implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      seriesEditorAccess.openSeriesEditor(currentSeries, selectedMonthIds);
    }
  }
}
