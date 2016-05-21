package com.budgetview.gui.time.actions;

import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SelectCurrentYearAction extends AbstractMonthSelectionAction {
  public SelectCurrentYearAction(GlobRepository repository, Directory directory) {
    super(Lang.get("selectMonth.currentYear"), repository, directory);
  }

  protected Iterable<Integer> getSelection(int currentMonthId) {
    return Month.yearRange(currentMonthId);
  }
}
