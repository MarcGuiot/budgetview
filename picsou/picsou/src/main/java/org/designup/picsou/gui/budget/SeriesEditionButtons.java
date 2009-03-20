package org.designup.picsou.gui.budget;

import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.splits.SplitsBuilder;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.EditSeriesAction;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;

import javax.swing.*;
import java.util.Set;
import java.awt.event.ActionEvent;

public class SeriesEditionButtons {

  private BudgetArea budgetArea;
  private GlobRepository repository;
  private Directory directory;
  private SeriesEditionDialog seriesEditionDialog;
  private SelectionService selectionService;
  private String createButtonName = "createSeries";
  private String editButtonName = "editAllSeries";

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

    builder.add(editButtonName,
                new EditSeriesAction(repository, directory, seriesEditionDialog, budgetArea));
  }

  public GlobButtonView createSeriesButton(Glob series) {
    return GlobButtonView.init(Series.TYPE, repository, directory, new EditSeriesFunctor())
      .forceSelection(series);
  }

  public void setNames(String createButtonName, String editButtonName) {
    this.createButtonName = createButtonName;
    this.editButtonName = editButtonName;
  }

  private class CreateSeriesAction extends AbstractAction {
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

}
