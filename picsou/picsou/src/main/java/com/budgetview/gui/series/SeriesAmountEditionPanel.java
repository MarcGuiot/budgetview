package com.budgetview.gui.series;

import com.budgetview.gui.analysis.SeriesAmountChartPanel;
import com.budgetview.gui.components.AmountEditor;
import com.budgetview.gui.components.charts.histo.diff.HistoDiffLegendPanel;
import com.budgetview.gui.description.stringifiers.MonthListStringifier;
import com.budgetview.gui.series.edition.AlignSeriesBudgetAmountsAction;
import com.budgetview.gui.series.utils.SeriesAmountLabelStringifier;
import com.budgetview.model.*;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.collections.Range;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
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
  private boolean autoSelectFutureMonths;
  private boolean selectionInProgress;
  private boolean showingActiveMonths = false;

  private SeriesAmountLabelStringifier selectionStringifier = new SeriesAmountLabelStringifier();
  private SeriesAmountChartPanel chart;
  private CardHandler cards;
  private JEditorPane disabledMessage;
  private SplitsNode<JPanel> amountPanel;
  private boolean creation;

  public SeriesAmountEditionPanel(GlobRepository repository, Directory directory) {

    this.repository = repository;
    this.directory = directory;

    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(
      new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          if (selection.isRelevantForType(Series.TYPE)) {
            Glob first = selection.getAll(Series.TYPE).getFirst();
            if (first == null) {
              clear();
            }
            else {
              setCurrentSeries(first.getKey(), creation);
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
          FieldValues previousValue = changeSet.getPreviousValues(currentSeries);
          if (previousValue.contains(Series.FROM_ACCOUNT) || previousValue.contains(Series.TO_ACCOUNT)) {
            updatePositiveOrNegativeRadio();
          }
        }

        if (changeSet.containsUpdates(SeriesBudget.PLANNED_AMOUNT) && (currentSeries != null)) {
          Glob series = repository.get(currentSeries);
          if (series.isTrue(Series.IS_AUTOMATIC)) {
            repository.update(currentSeries, Series.IS_AUTOMATIC, false);
          }
        }

        if (changeSet.containsCreationsOrDeletions(SeriesBudget.TYPE) || changeSet.containsUpdates(SeriesBudget.ACTIVE)) {
          if (!showingActiveMonths) {
            selectMonths(selectedMonthIds);
          }
          updateCard(false);
        }

        if ((currentSeries != null) &&
            (changeSet.containsUpdates(Series.FIRST_MONTH) ||
             changeSet.containsUpdates(Series.LAST_MONTH))) {
          updateSelectionAfterRangeChange();
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

    cards = builder.addCardHandler("cards");

    builder.addLabel("dateLabel", SeriesBudget.TYPE, selectionStringifier);

    chart = new SeriesAmountChartPanel(repository, directory);
    builder.add("chart", chart.getChart());

    HistoDiffLegendPanel legendPanel = new HistoDiffLegendPanel(repository, directory);
    legendPanel.show(Lang.get("seriesEdition.chartLegend.actual"), Lang.get("seriesEdition.chartLegend.planned"));
    builder.add("chartLegend", legendPanel.getPanel());

    amountEditor = new AmountEditor(SeriesBudget.PLANNED_AMOUNT, repository, directory, true, 0.0);
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

    disabledMessage = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("disabledMessage", disabledMessage);

    this.amountPanel = builder.add("amountPanel", new JPanel());

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

  public void setCurrentSeries(Key seriesKey, boolean creation) {
    this.creation = creation;
    propagationCheckBox.setEnabled(seriesKey != null);
    chart.getChart().setEnabled(seriesKey != null);
    amountEditor.setEnabled(seriesKey != null);
    currentSeries = seriesKey;

    boolean noValueDefined =
      !repository.contains(SeriesBudget.TYPE,
                           and(linkedTo(currentSeries, SeriesBudget.SERIES),
                               isTrue(SeriesBudget.ACTIVE),
                               isNotNull(SeriesBudget.PLANNED_AMOUNT)));

    boolean canAutoPropagate = (seriesKey != null) && !BudgetArea.EXTRAS.getId().equals(repository.get(seriesKey).get(Series.BUDGET_AREA));

    boolean autoPropagate = noValueDefined && canAutoPropagate;

    setAutoSelectFutureMonths(autoPropagate);
    propagationCheckBox.setSelected(autoPropagate);

    if (autoPropagate) {
      selectMonths(repository.getAll(Month.TYPE).getValueSet(Month.ID));
    }

    amountPanel.applyStyle(noValueDefined && !creation ? "amountPanelHighlighted" : "amountPanelNormal");

    updateCard(true);
  }

  public void clear() {
    setCurrentSeries(null, false);
    selectionService.clear(SeriesBudget.TYPE);
    chart.init(null, null);
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

    updateCard(true);
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

  public void completeBeforeCommit() {

    if (!repository.contains(SeriesBudget.TYPE,
                             and(isNotNull(SeriesBudget.PLANNED_AMOUNT),
                                 linkedTo(currentSeries, SeriesBudget.SERIES)))) {
      return;
    }

    repository.startChangeSet();
    try {
      for (Glob seriesBudget : repository.getAll(SeriesBudget.TYPE,
                                                 and(isNull(SeriesBudget.PLANNED_AMOUNT),
                                                     linkedTo(currentSeries, SeriesBudget.SERIES)))) {
        if (seriesBudget.get(SeriesBudget.PLANNED_AMOUNT) == null) {
          repository.update(seriesBudget.getKey(), SeriesBudget.PLANNED_AMOUNT, 0.00);
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private void updateCard(boolean canDisable) {
    showingActiveMonths = isShowingActiveMonths();
    if (showingActiveMonths || !canDisable) {
      cards.show("standard");
    }
    else {
      cards.show("disabled");
    }
  }

  private boolean isShowingActiveMonths() {
    if (currentSeries == null) {
      disabledMessage.setText(Lang.get("seriesAmountEdition.disabled.noSeries"));
      return false;
    }

    if ((selectedMonthIds == null) || (selectedMonthIds.isEmpty())) {
      disabledMessage.setText(Lang.get("seriesAmountEdition.disabled.noSelectedMonths"));
      return false;
    }

    for (Integer monthId : selectedMonthIds) {
      for (Glob seriesBudget : SeriesBudget.getAll(currentSeries.get(Series.ID), monthId, repository)) {
        if (seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
          return true;
        }
      }
    }

    disabledMessage.setText(Lang.get("seriesAmountEdition.disabled.noActiveMonths",
                                     MonthListStringifier.toString(selectedMonthIds).toLowerCase()));
    return false;
  }

  private void updatePositiveOrNegativeRadio() {
    Glob series = repository.get(currentSeries);
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    double multiplier = Account.getMultiplierForInOrOutputOfTheAccount(series);
    boolean isUsuallyPositive = budgetArea.isIncome() || (budgetArea == BudgetArea.TRANSFER && multiplier > 0);
    amountEditor.update(isUsuallyPositive, budgetArea == BudgetArea.TRANSFER);
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
        selectionOK |= addBudgetToSelection(selection, seriesId, monthId);
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
            selectionOK |= addBudgetToSelection(selection, seriesId, previous);
          }
          if (next < realLast) {
            next = Month.next(next);
            selectionOK |= addBudgetToSelection(selection, seriesId, next);
          }
        }
      }
      selectionService.select(selection.get());
    }
    finally {
      selectionInProgress = false;
    }
  }

  private boolean addBudgetToSelection(GlobSelectionBuilder selection, Integer seriesId, Integer monthId) {
    GlobList budget = getBudget(seriesId, monthId);
    if (budget.isEmpty()) {
      return false;
    }
    selection.add(budget, SeriesBudget.TYPE);
    selection.add(repository.get(Key.create(Month.TYPE, monthId)));
    return true;
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
    repository.startChangeSet();
    try {
      repository.update(currentSeries, Series.IS_AUTOMATIC, false);
      repository.safeApply(SeriesBudget.TYPE,
                           and(
                             fieldEquals(SeriesBudget.SERIES, currentSeries.get(Series.ID)),
                             isTrue(SeriesBudget.ACTIVE),
                             fieldGreaterOrEqual(SeriesBudget.MONTH, startMonth)),
                           update(SeriesBudget.PLANNED_AMOUNT, Utils.zeroIfNull(amount)));
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private void updateSelectionAfterRangeChange() {

    SortedSet<Integer> selectedMonths = selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);

    Glob series = repository.get(currentSeries);
    Range<Integer> seriesRange = new Range<Integer>(series.get(Series.FIRST_MONTH), series.get(Series.LAST_MONTH));
    Integer current = CurrentMonth.getCurrentMonth(repository);

    if (selectedMonths.isEmpty()) {
      if (seriesRange.contains(current)) {
        select(current);
      }
      else {
        select(seriesRange.getMin());
      }
      return;
    }

    Range<Integer> selectionRange = new Range<Integer>(selectedMonths.first(), selectedMonths.last());
    if (!seriesRange.overlaps(selectionRange)) {
      if (seriesRange.contains(current)) {
        select(current);
      }
      else if (seriesRange.after(current)) {
        select(seriesRange.getMin());
      }
      else {
        select(seriesRange.getMax());
      }
    }
  }

  private void select(int monthId) {
    Glob month = repository.find(Key.create(Month.TYPE, monthId));
    if (month != null) {
      selectionService.select(month);
    }
  }
}
