package com.budgetview.desktop.actions;

import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SelectPreviousMonthAction extends AbstractMonthSelectionAction {

  public SelectPreviousMonthAction(GlobRepository repository, Directory directory) {
    super(Lang.get("previousMonth"), Lang.get("previousMonth.tooltip"), repository, directory);
  }

  protected int getTargetMonth() {
    return Month.previous(firstMonth);
  }
}