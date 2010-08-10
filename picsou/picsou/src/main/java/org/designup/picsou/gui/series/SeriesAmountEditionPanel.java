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
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
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
  private boolean selectionInProgress;
  private SeriesAmountChartPanel chart;
  private boolean autoSelectFutureMonths;
  private SeriesEditorAccess seriesEditorAccess;

  public interface SeriesEditorAccess {
    void openSeriesEditor(Key seriees, Set<Integer> selectedMonthIds);
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
          changeSeries(selection.getAll(Series.TYPE).getFirst().getKey());
        }
        if (selection.isRelevantForType(Month.TYPE)) {
          Set<Integer> selectedMonths = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
          doSelectMonths(SeriesAmountEditionPanel.this.repository.find(currentSeries), selectedMonths);
        }
      }
    }, Month.TYPE);

    createPanel();

    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if ((currentSeries != null) && changeSet.containsChanges(currentSeries)) {
          FieldValues previousValue = changeSet.getPreviousValue(currentSeries);
          if (previousValue.contains(Series.FROM_ACCOUNT) || previousValue.contains(Series.TO_ACCOUNT)) {
            updatePositiveOrNegativeRadio();
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

    builder.addLabel("dateLabel", SeriesBudget.TYPE, new SeriesAmountLabelStringifier());

    chart = new SeriesAmountChartPanel(repository, directory);
    builder.add("chart", chart.getChart());

    amountEditor = new AmountEditor(SeriesBudget.AMOUNT, repository, directory, true, 0.0);
    builder.add("amountEditor", amountEditor.getNumericEditor());
    builder.add("positiveAmounts", amountEditor.getPositiveRadio());
    builder.add("negativeAmounts", amountEditor.getNegativeRadio());

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
                      new SeriesBudgetSliderAdapter(amountEditor, repository, directory));

    builder.addButton("editSeries", Series.TYPE, new SeriesPeriodicityAndScopeStringifier(), new OpenSeriesEditorCallback());

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
  }

  public void changeSeries(Key seriesKey) {
    currentSeries = seriesKey;
    autoSelectFutureMonths = false;
    propagationCheckBox.setSelected(false);
    chart.init(null, null);
  }

  public void selectMonths(Set<Integer> months) {

    if (currentSeries == null) {
      return;
    }

    Glob series = repository.get(currentSeries);
    currentSeries = series.getKey();
    currentMonth = Utils.max(months);

    chart.init(currentSeries.get(Series.ID), currentMonth);

    updatePositiveOrNegativeRadio();

    doSelectMonths(series, months);

    amountEditor.selectAll();
  }

  private void updatePositiveOrNegativeRadio() {
    Glob series = repository.get(currentSeries);
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    double multiplier = Account.computeAmountMultiplier(series, repository);
    boolean isUsuallyPositive = budgetArea.isIncome() ||
                                (budgetArea == BudgetArea.SAVINGS && multiplier > 0);
    amountEditor.update(isUsuallyPositive, budgetArea == BudgetArea.SAVINGS);
  }

  private void toto(Glob series) {
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    amountEditor.update(isUsuallyPositive(series, budgetArea),
                        budgetArea == BudgetArea.SAVINGS);
  }

  private boolean isUsuallyPositive(Glob series, BudgetArea budgetArea) {
    double multiplier = Account.computeAmountMultiplier(series, repository);
    return budgetArea.isIncome() || (budgetArea == BudgetArea.SAVINGS && multiplier > 0);
  }

  private void doSelectMonths(Glob series, Set<Integer> monthIds) {
    if (selectionInProgress) {
      return;
    }

    selectionInProgress = true;
    try {
      selectedMonthIds = monthIds;
      GlobSelectionBuilder selection = GlobSelectionBuilder.init();
      Integer seriesId = series.get(Series.ID);
      for (Integer monthId : getMonths(monthIds)) {
        GlobList list = repository
          .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
          .findByIndex(SeriesBudget.MONTH, monthId)
          .getGlobs();
        list.filterSelf(GlobMatchers.isTrue(SeriesBudget.ACTIVE), repository);
        selection.add(list, SeriesBudget.TYPE);
        selection.add(repository.get(Key.create(Month.TYPE, monthId)));
      }
      selectionService.select(selection.get());
    }
    finally {
      selectionInProgress = false;
    }
  }

  private void updateAutoSelect(boolean enabled) {

    autoSelectFutureMonths = enabled;

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

  public void applyChanges(boolean containsChanges) {
    Glob series = repository.get(currentSeries);
    if (series.isTrue(Series.IS_AUTOMATIC) && containsChanges) {
      repository.update(currentSeries, Series.IS_AUTOMATIC, false);
    }
    if (propagationCheckBox.isSelected()) {
      propagateValue(SeriesAmountEditionPanel.this.currentMonth);
    }
  }

  private void propagateValue(Integer startMonth) {
    final Double amount = amountEditor.getValue();
    repository.safeApply(SeriesBudget.TYPE,
                         and(
                           isTrue(SeriesBudget.ACTIVE),
                           fieldStrictlyGreaterThan(SeriesBudget.MONTH, startMonth)),
                         update(SeriesBudget.AMOUNT, Utils.zeroIfNull(amount)));
  }

  public class OpenSeriesEditorCallback implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      seriesEditorAccess.openSeriesEditor(currentSeries, selectedMonthIds);
    }
  }
}