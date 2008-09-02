package org.designup.picsou.gui.series;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;

public class EditSeriesAction extends AbstractAction {
  private Directory directory;
  private SeriesEditionDialog seriesEditionDialog;
  private BudgetArea budgetArea;

  public EditSeriesAction(GlobRepository repository,
                          Directory directory,
                          SeriesEditionDialog seriesEditionDialog,
                          final BudgetArea budgetArea) {
    super(Lang.get("budgetview.editAll"));
    this.directory = directory;
    this.seriesEditionDialog = seriesEditionDialog;
    this.budgetArea = budgetArea;
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        update(repository);
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update(repository);
      }
    });
  }

  private void update(GlobRepository repository) {
    GlobList series = repository.getAll(Series.TYPE, fieldEquals(Series.BUDGET_AREA, budgetArea.getId()));
    setEnabled(!series.isEmpty());
  }

  public void actionPerformed(ActionEvent e) {
    seriesEditionDialog.show(budgetArea, getSelectedMonthIds());
  }

  protected Set<Integer> getSelectedMonthIds() {
    int currentMonthId = directory.get(TimeService.class).getCurrentMonthId();
    return Collections.singleton(currentMonthId);
  }
}
