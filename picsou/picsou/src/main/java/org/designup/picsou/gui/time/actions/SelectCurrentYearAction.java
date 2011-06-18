package org.designup.picsou.gui.time.actions;

import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
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
