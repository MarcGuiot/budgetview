package com.budgetview.gui.time.actions;

import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SelectSinceLastJanuaryAction extends AbstractMonthSelectionAction {
  public SelectSinceLastJanuaryAction(GlobRepository repository, Directory directory) {
    super(Lang.get("selectMonth.sinceLastJanuary"), repository, directory);
  }

  protected Iterable<Integer> getSelection(int currentMonthId) {
    return Month.range((currentMonthId / 100) * 100 + 1, currentMonthId);
  }
}
