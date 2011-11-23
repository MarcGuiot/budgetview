package org.designup.picsou.gui.series;

import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.description.MonthListStringifier;
import org.designup.picsou.gui.description.SeriesPeriodicityAndScopeStringifier;
import org.designup.picsou.gui.series.analysis.SeriesAmountChartPanel;
import org.designup.picsou.gui.series.edition.AlignSeriesBudgetAmountsAction;
import org.designup.picsou.gui.series.edition.SeriesBudgetSliderAdapter;
import org.designup.picsou.gui.series.utils.SeriesAmountLabelStringifier;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.collections.Range;
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
  private CardHandler cards;
  private JEditorPane disabledMessage;
  private boolean showingActiveMonths = false;

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
    this.selectionService.addListener(
      new GlobSelectionListener() {
        public void selectionUpdated(GlobSelection selection) {
          if (selection.isRelevantForType(Series.TYPE)) {
            Glob first = selection.getAll(Series.TYPE).getFirst();
            if (first == null) {
              clear();
            }
            else {
              setCurrentSeries(first.getKey());
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

        if (changeSet.containsUpdates(SeriesBudget.AMOUNT) && (currentSeries != null)) {
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

    disabledMessage = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("disabledMessage", disabledMessage);

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

  public void setCurrentSeries(Key seriesKey) {
    propagationCheckBox.setEnabled(seriesKey != null);
    chart.getChart().setEnabled(seriesKey != null);
    amountEditor.setEnabled(seriesKey != null);
    currentSeries = seriesKey;

    boolean noValueDefined =
      !repository.contains(SeriesBudget.TYPE,
                           and(linkedTo(currentSeries, SeriesBudget.SERIES),
                               isTrue(SeriesBudget.ACTIVE),
                               isNotNull(SeriesBudget.AMOUNT)));

    boolean canAutoPropagate = (seriesKey != null) && !BudgetArea.EXTRAS.getId().equals(repository.get(seriesKey).get(Series.BUDGET_AREA));

    boolean autoPropagate = noValueDefined && canAutoPropagate;

    setAutoSelectFutureMonths(autoPropagate);
    propagationCheckBox.setSelected(autoPropagate);

    if (autoPropagate) {
      selectMonths(repository.getAll(Month.TYPE).getValueSet(Month.ID));
    }

    updateCard(true);
  }

  public void clear() {
    setCurrentSeries(null);
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
                             and(isNotNull(SeriesBudget.AMOUNT),
                                 linkedTo(currentSeries, SeriesBudget.SERIES)))) {
      return;
    }

    repository.startChangeSet();
    try {
      for (Glob seriesBudget : repository.getAll(SeriesBudget.TYPE,
                                                 and(isNull(SeriesBudget.AMOUNT),
                                                     linkedTo(currentSeries, SeriesBudget.SERIES)))) {
        if (seriesBudget.get(SeriesBudget.AMOUNT) == null) {
          repository.update(seriesBudget.getKey(), SeriesBudget.AMOUNT, 0.00);
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
                           update(SeriesBudget.AMOUNT, Utils.zeroIfNull(amount)));
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public class OpenSeriesEditorCallback implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      seriesEditorAccess.openSeriesEditor(currentSeries, selectedMonthIds);
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
