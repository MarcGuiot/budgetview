package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.signpost.actions.SetSignpostStatusAction;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class SeriesEditionButtons {

  private BudgetArea budgetArea;
  private GlobRepository repository;
  private Directory directory;
  private SelectionService selectionService;
  private String createButtonName = "createSeries";

  public SeriesEditionButtons(final BudgetArea budgetArea,
                              final GlobRepository repository,
                              Directory directory) {
    this.budgetArea = budgetArea;
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
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

  public void setNames(String createButtonName) {
    this.createButtonName = createButtonName;
  }

  private class CreateSeriesAction extends AbstractAction {

    private CreateSeriesAction() {
      super(Lang.get("series.add"));
    }

    public void actionPerformed(ActionEvent e) {
      SeriesEditor.get(directory).showNewSeries(GlobList.EMPTY,
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
    if (repository.contains(Project.TYPE, GlobMatchers.linkedTo(series.getKey(), Project.SERIES))) {

    }
    SeriesEditor.get(directory).showSeries(series, selectedMonthIds);
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
