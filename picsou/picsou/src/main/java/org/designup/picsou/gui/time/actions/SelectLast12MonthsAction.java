package org.designup.picsou.gui.time.actions;

import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
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
