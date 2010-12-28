package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.signpost.actions.SetSignpostStatusAction;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class SeriesEditionButtons {

  private BudgetArea budgetArea;
  private GlobRepository repository;
  private Directory directory;
  private SeriesEditionDialog seriesEditionDialog;
  private SelectionService selectionService;
  private String createButtonName = "createSeries";

  public SeriesEditionButtons(final BudgetArea budgetArea,
                              final GlobRepository repository,
                              Directory directory,
                              final SeriesEditionDialog seriesEditionDialog) {
    this.budgetArea = budgetArea;
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
    this.seriesEditionDialog = seriesEditionDialog;
  }

  public void registerButtons(SplitsBuilder builder) {
    builder.add(createButtonName, new CreateSeriesAction());
  }

  public GlobButtonView createSeriesButton(Glob series) {
    GlobButtonView buttonView =
      GlobButtonView.init(Series.TYPE, repository, directory, new EditSeriesFunctor())
        .forceSelection(series.getKey());
    buttonView.getComponent().addActionListener(
      new SetSignpostStatusAction(SignpostStatus.SERIES_PERIODICITY_SHOWN, repository));
    repository.addChangeListener(new TooltipUpdater(series.getKey(), buttonView));
    return buttonView;
  }

  public void setNames(String createButtonName, String editButtonName) {
    this.createButtonName = createButtonName;
  }

  private class CreateSeriesAction extends AbstractAction {

    private CreateSeriesAction() {
      super(Lang.get("add"));
    }

    public void actionPerformed(ActionEvent e) {
      seriesEditionDialog.showNewSeries(GlobList.EMPTY,
                                        selectionService.getSelection(Month.TYPE),
                                        budgetArea);
    }
  }

  private class EditSeriesFunctor implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      showSeriesEdition(list.getFirst());
    }
  }

  private void showSeriesEdition(Glob series) {
    Set<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getValueSet(Month.ID);
    seriesEditionDialog.show(series, selectedMonthIds);
  }

  private class TooltipUpdater implements ChangeSetListener {
    private Key seriesKey;
    private GlobButtonView buttonView;

    public TooltipUpdater(Key seriesKey, GlobButtonView buttonView) {
      this.seriesKey = seriesKey;
      this.buttonView = buttonView;
      updateTooltip(repository);
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (changeSet.containsChanges(seriesKey)) {
        updateTooltip(repository);
      }
    }

    private void updateTooltip(GlobRepository repository) {
      Glob series = repository.find(seriesKey);
      if (series != null) {
        String description = series.get(Series.DESCRIPTION);
        buttonView.getComponent().setToolTipText(Strings.toSplittedHtml(description, 50));
      }
      else {
        repository.removeChangeListener(this);
      }
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      if (!repository.contains(seriesKey)) {
        repository.removeChangeListener(this);
      }
    }
  }
}
