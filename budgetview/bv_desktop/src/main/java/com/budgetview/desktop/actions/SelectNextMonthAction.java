package com.budgetview.desktop.actions;

import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SelectNextMonthAction extends AbstractMonthSelectionAction {

  public SelectNextMonthAction(GlobRepository repository, Directory directory) {
    super(Lang.get("nextMonth"), Lang.get("nextMonth.tooltip"), repository, directory);
  }

  protected int getTargetMonth() {
    return Month.next(lastMonth);
  }
}
