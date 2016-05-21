package com.budgetview.gui.budget;

import com.budgetview.gui.budget.components.NameLabelPopupButton;
import com.budgetview.gui.series.SeriesEditor;
import com.budgetview.model.BudgetArea;
import com.budgetview.gui.series.utils.SeriesPopupFactory;
import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.DisposablePopupMenuFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class SeriesEditionButtons {

  private BudgetArea budgetArea;
  private GlobRepository repository;
  private Directory directory;
  private SelectionService selectionService;
  private EditSeriesFunctor editSeriesFunctor;

  public SeriesEditionButtons(final BudgetArea budgetArea,
                              final GlobRepository repository,
                              Directory directory) {
    this.budgetArea = budgetArea;
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
    this.editSeriesFunctor = new EditSeriesFunctor();
  }

  public CreateSeriesAction createSeriesAction() {
    return new CreateSeriesAction();
  }

  public NameLabelPopupButton createSeriesPopupButton(Glob series) {
    DisposablePopupMenuFactory popupFactory = new SeriesPopupFactory(series, editSeriesFunctor, repository, directory);
    return new NameLabelPopupButton(series.getKey(), popupFactory, repository, directory);
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
    SeriesEditor.get(directory).showSeries(series, selectedMonthIds);
  }
}
