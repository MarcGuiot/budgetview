package org.designup.picsou.gui.actions;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.Month;
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
