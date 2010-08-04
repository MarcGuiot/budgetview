package org.designup.picsou.gui.series;

import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.SeriesPeriodicityAndScopeStringifier;
import org.designup.picsou.gui.series.edition.AlignSeriesBudgetAmountsAction;
import org.designup.picsou.gui.series.edition.SeriesBudgetSliderAdapter;
import org.designup.picsou.gui.series.evolution.SeriesAmountChartPanel;
import org.designup.picsou.gui.series.utils.SeriesAmountLabelStringifier;
import org.designup.picsou.gui.signpost.actions.SetSignpostStatusAction;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.savings.UpdateMirrorSeriesBudgetChangeSetVisitor;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import static org.globsframework.model.utils.GlobFunctors.update;
import static org.globsframework.model.utils.GlobMatchers.*;
import static org.globsframework.model.utils.GlobMatchers.fieldGreaterOrEqual;

public class SeriesAmountEditionDialog {

  private AmountEditor amountEditor;
  private SelectionService selectionService;
  private JCheckBox propagationCheckBox;
  private PicsouDialog dialog;

  private LocalGlobRepository localRepository;
  private GlobRepository parentRepository;
  private SeriesEditionDialog seriesEditionDialog;

  private Key currentSeries;
  private Integer currentMonth;
  private Set<Integer> selectedMonthIds;
  private Directory localDirectory;
  private boolean selectionInProgress;
  private SeriesAmountChartPanel chart;
  private boolean autoSelectFutureMonths;

  public SeriesAmountEditionDialog(GlobRepository parentRepository, Directory parentDirectory,
                                   SeriesEditionDialog seriesEditionDialog) {

    this.parentRepository = parentRepository;
    this.localRepository = LocalGlobRepositoryBuilder.init(parentRepository).get();

    localDirectory = new DefaultDirectory(parentDirectory);
    selectionService = new SelectionService();
    localDirectory.add(selectionService);

    this.seriesEditionDialog = seriesEditionDialog;

    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        Set<Integer> selectedMonths = selection.getAll(Month.TYPE).getSortedSet(Month.ID);
        select(localRepository.find(currentSeries), selectedMonths);
      }
    }, Month.TYPE);

    createDialog();
  }

  private void createDialog() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesAmountEditionDialog.class,
                                                      "/layout/series/seriesAmountEditionDialog.splits",
                                                      localRepository, localDirectory);

    builder.addLabel("dateLabel", SeriesBudget.TYPE, new SeriesAmountLabelStringifier());

    chart = new SeriesAmountChartPanel(localRepository, localDirectory);
    builder.add("chart", chart.getChart());

    amountEditor = new AmountEditor(SeriesBudget.AMOUNT, localRepository, localDirectory, true, 0.0);
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

    AlignSeriesBudgetAmountsAction alignAction = new AlignSeriesBudgetAmountsAction(localRepository, localDirectory);
    builder.add("alignValue", alignAction);
    builder.add("actualAmountLabel", alignAction.getActualAmountLabel());

    builder.addSlider("slider",
                      SeriesBudget.AMOUNT,
                      new SeriesBudgetSliderAdapter(amountEditor, localRepository, localDirectory));

    builder.addButton("editSeries", Series.TYPE, new SeriesPeriodicityAndScopeStringifier(), new OpenSeriesEditorCallback());

    dialog = PicsouDialog.create(localDirectory.get(JFrame.class), localDirectory);

    OkAction okAction = new OkAction();
    amountEditor.addAction(okAction);
    dialog.addPanelWithButtons(builder.<JPanel>load(), okAction, new CancelAction());
    dialog.setAutoFocusOnOpen(amountEditor.getNumericEditor().getComponent());
    dialog.addOnWindowClosedAction(new SetSignpostStatusAction(SignpostStatus.SERIES_AMOUNT_CLOSED,
                                                               SignpostStatus.SERIES_AMOUNT_SHOWN,
                                                               parentRepository));
    dialog.pack();
  }

  public void show(Glob series, Set<Integer> months) {
    if (series != null && series.isTrue(Series.IS_MIRROR)) {
      series = parentRepository.findLinkTarget(series, Series.MIRROR_SERIES);
    }

    currentSeries = series.getKey();
    currentMonth = Utils.max(months);
    autoSelectFutureMonths = false;
    propagationCheckBox.setSelected(false);

    chart.init(null, null);
    loadGlobs(series);

    chart.init(currentSeries.get(Series.ID), currentMonth);

    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    amountEditor.update(isUsuallyPositive(series, budgetArea),
                        budgetArea == BudgetArea.SAVINGS);

    select(series, months);

    amountEditor.selectAll();

    GuiUtils.showCentered(dialog);
  }

  private boolean isUsuallyPositive(Glob series, BudgetArea budgetArea) {
    double multiplier =
      Account.computeAmountMultiplier(localRepository.findLinkTarget(series, Series.FROM_ACCOUNT),
                                      localRepository.findLinkTarget(series, Series.TO_ACCOUNT),
                                      localRepository);
    return budgetArea.isIncome() || (budgetArea == BudgetArea.SAVINGS && multiplier > 0);
  }

  private void loadGlobs(Glob series) {
    GlobList globsToLoad = new GlobList();
    globsToLoad.add(series);
    globsToLoad.addAll(getLinkedAccounts(series));
    globsToLoad.addAll(getBudgets(series));
    globsToLoad.addAll(parentRepository.getAll(Month.TYPE));
    globsToLoad.addAll(parentRepository.getAll(CurrentMonth.TYPE));
    localRepository.reset(globsToLoad, Series.TYPE, SeriesBudget.TYPE, Account.TYPE, Month.TYPE, CurrentMonth.TYPE);
  }

  private GlobList getLinkedAccounts(Glob series) {
    GlobList accounts = new GlobList();
    accounts.addNotNull(parentRepository.findLinkTarget(series, Series.FROM_ACCOUNT),
                        parentRepository.findLinkTarget(series, Series.TO_ACCOUNT));
    return accounts;
  }

  private GlobList getBudgets(Glob series) {
    GlobList globs = parentRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID)).getGlobs();
    Integer value = series.get(Series.MIRROR_SERIES);
    if (value != null) {
      globs.addAll(parentRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, value).getGlobs());
    }
    return globs;
  }

  private void select(Glob series, Set<Integer> monthIds) {
    if (selectionInProgress) {
      return;
    }

    selectionInProgress = true;
    try {
      selectedMonthIds = monthIds;
      GlobSelectionBuilder selection = GlobSelectionBuilder.init();
      selection.add(series);
      Integer seriesId = series.get(Series.ID);
      for (Integer monthId : getMonths(monthIds)) {
        selection.add(
          localRepository
            .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
            .findByIndex(SeriesBudget.MONTH, monthId)
            .getGlobs(),
          SeriesBudget.TYPE);
        selection.add(localRepository.get(Key.create(Month.TYPE, monthId)));
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
        localRepository.getAll(Month.TYPE, fieldGreaterOrEqual(Month.ID, firstMonthId));
      selectionService.select(nextMonths, Month.TYPE);
    }
    else {
      Glob firstMonth = localRepository.get(Key.create(Month.TYPE, firstMonthId));
      selectionService.select(firstMonth);
    }
  }

  private Collection<Integer> getMonths(Set<Integer> monthIds) {
    if (!autoSelectFutureMonths || monthIds.isEmpty()) {
      return monthIds;
    }

    GlobList nextMonths =
      localRepository.getAll(Month.TYPE, fieldGreaterOrEqual(Month.ID, Utils.min(monthIds)));
    return nextMonths.getValueSet(Month.ID);
  }

  private class OkAction extends AbstractAction {

    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      Glob series = localRepository.get(currentSeries);
      if (series.isTrue(Series.IS_AUTOMATIC) && localRepository.containsChanges()) {
        localRepository.update(currentSeries, Series.IS_AUTOMATIC, false);
      }
      if (propagationCheckBox.isSelected()) {
        propagateValue(SeriesAmountEditionDialog.this.currentMonth);
      }

      ChangeSet changeSet = localRepository.getCurrentChanges();

      localRepository.startChangeSet();
      try {
        changeSet.safeVisit(SeriesBudget.TYPE, new UpdateMirrorSeriesBudgetChangeSetVisitor(localRepository));
      }
      finally {
        localRepository.completeChangeSet();
      }

      localRepository.commitChanges(false);
      dialog.setVisible(false);
    }
  }

  private void propagateValue(Integer startMonth) {
    final Double amount = amountEditor.getValue();
    localRepository.safeApply(SeriesBudget.TYPE,
                              and(
                                isTrue(SeriesBudget.ACTIVE),
                                fieldStrictlyGreaterThan(SeriesBudget.MONTH, startMonth)),
                              update(SeriesBudget.AMOUNT, Utils.zeroIfNull(amount)));
  }
  
  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.rollback();
      dialog.setVisible(false);
    }
  }

  private class OpenSeriesEditorCallback implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      localRepository.rollback();
      dialog.setVisible(false);
      seriesEditionDialog.show(parentRepository.get(currentSeries), selectedMonthIds);
    }
  }
}
