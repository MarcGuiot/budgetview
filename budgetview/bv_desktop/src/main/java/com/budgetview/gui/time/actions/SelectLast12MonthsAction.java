package com.budgetview.gui.time.actions;

import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SelectLast12MonthsAction extends AbstractMonthSelectionAction {
  public SelectLast12MonthsAction(GlobRepository repository, Directory directory) {
    super(Lang.get("selectMonth.last12Months"), repository, directory);
  }

  protected Iterable<Integer> getSelection(int currentMonthId) {
    return Month.range(Month.previous(currentMonthId, 11), currentMonthId);
  }
}
