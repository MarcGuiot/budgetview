package org.designup.picsou.gui.actions;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.model.Month;
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