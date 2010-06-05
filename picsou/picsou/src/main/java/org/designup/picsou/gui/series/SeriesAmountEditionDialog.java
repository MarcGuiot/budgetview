package org.designup.picsou.gui.series;

import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.SeriesPeriodicityAndScopeStringifier;
import org.designup.picsou.gui.series.edition.AlignSeriesBudgetAmountsAction;
import org.designup.picsou.gui.series.edition.SeriesBudgetSliderAdapter;
import org.designup.picsou.gui.series.utils.SeriesAmountLabelStringifier;
import org.designup.picsou.gui.signpost.actions.SetSignpostStatusAction;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

import static org.globsframework.model.utils.GlobFunctors.update;
import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesAmountEditionDialog {

  private AmountEditor amountEditor;
  private SelectionService selectionService;
  private JCheckBox propagationCheckBox;
  private PicsouDialog dialog;

  private LocalGlobRepository localRepository;
  private Directory directory;
  private GlobRepository parentRepository;
  private SeriesEditionDialog seriesEditionDialog;

  private Key currentSeries;
  private Integer maxMonth;
  private Set<Integer> selectedMonthIds;

  public SeriesAmountEditionDialog(GlobRepository parentRepository, Directory parentDirectory,
                                   SeriesEditionDialog seriesEditionDialog) {

    this.parentRepository = parentRepository;
    this.localRepository = LocalGlobRepositoryBuilder.init(parentRepository).get();

    Directory directory = new DefaultDirectory(parentDirectory);
    selectionService = new SelectionService();
    directory.add(selectionService);

    this.seriesEditionDialog = seriesEditionDialog;

    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesAmountEditionDialog.class,
                                                      "/layout/series/seriesAmountEditionDialog.splits",
                                                      localRepository, directory);

    builder.addLabel("dateLabel", SeriesBudget.TYPE, new SeriesAmountLabelStringifier());

    amountEditor = new AmountEditor(SeriesBudget.AMOUNT, localRepository, directory, true, 0.0);
    builder.add("amountEditor", amountEditor.getNumericEditor());
    builder.add("positiveAmounts", amountEditor.getPositiveRadio());
    builder.add("negativeAmounts", amountEditor.getNegativeRadio());

    propagationCheckBox = new JCheckBox();
    builder.add("propagate", propagationCheckBox);

    AlignSeriesBudgetAmountsAction alignAction = new AlignSeriesBudgetAmountsAction(localRepository, directory);
    builder.add("alignValue", alignAction);
    builder.add("actualAmountLabel", alignAction.getActualAmountLabel());

    builder.addSlider("slider",
                      SeriesBudget.AMOUNT,
                      new SeriesBudgetSliderAdapter(amountEditor, localRepository, directory));

    builder.addButton("editSeries", Series.TYPE, new SeriesPeriodicityAndScopeStringifier(), new OpenSeriesEditorCallback());

    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);

    OkAction okAction = new OkAction();
    amountEditor.addAction(okAction);
    dialog.addPanelWithButtons(builder.<JPanel>load(), okAction, new CancelAction());
    dialog.setAutoFocusOnOpen(amountEditor.getNumericEditor().getComponent());
    dialog.addOnWindowClosedAction(new SetSignpostStatusAction(SignpostStatus.SERIES_AMOUNT_CLOSED, parentRepository));
    dialog.pack();
  }

  public void show(Glob series, Set<Integer> months) {
    if (series != null && series.isTrue(Series.IS_MIRROR)) {
      series = parentRepository.findLinkTarget(series, Series.MIRROR_SERIES);
    }

    this.currentSeries = series.getKey();
    this.maxMonth = Utils.max(months);
    loadGlobs(series);

    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    amountEditor.update(isUsuallyPositive(series, budgetArea),
                        budgetArea == BudgetArea.SAVINGS);

    select(series, months);

    propagationCheckBox.setSelected(false);
    amountEditor.selectAll();

    SignpostStatus.setCompleted(SignpostStatus.SERIES_AMOUNT_SHOWN, parentRepository);

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
    localRepository.reset(globsToLoad, Series.TYPE, SeriesBudget.TYPE, Account.TYPE);
  }

  private GlobList getLinkedAccounts(Glob series) {
    GlobList accounts = new GlobList();
    accounts.addNotNull(parentRepository.findLinkTarget(series, Series.FROM_ACCOUNT),
                        parentRepository.findLinkTarget(series, Series.TO_ACCOUNT));
    return accounts;
  }

  private GlobList getBudgets(Glob series) {
    return parentRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID)).getGlobs();
  }

  private void select(Glob series, Set<Integer> monthIds) {
    selectedMonthIds = monthIds;
    GlobSelectionBuilder selection = GlobSelectionBuilder.init();

    selection.add(series);

    Integer seriesId = series.get(Series.ID);
    for (Integer monthId : monthIds) {
      selection.add(
        localRepository
          .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
          .findByIndex(SeriesBudget.MONTH, monthId)
          .getGlobs(),
        SeriesBudget.TYPE);
    }

    selectionService.select(selection.get());
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
        final Double amount = amountEditor.getValue();
        localRepository.safeApply(SeriesBudget.TYPE,
                                  and(
                                    isTrue(SeriesBudget.ACTIVE),
                                    fieldStrictlyGreaterThan(SeriesBudget.MONTH, maxMonth)),
                                  update(SeriesBudget.AMOUNT, Utils.zeroIfNull(amount)));
      }

      localRepository.commitChanges(false);
      dialog.setVisible(false);
    }
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
