package com.budgetview.gui.time.actions;

import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Collections;

public class SelectCurrentMonthAction extends AbstractMonthSelectionAction {
  public SelectCurrentMonthAction(GlobRepository repository, Directory directory) {
    super(Lang.get("selectMonth.currentMonth"), repository, directory);
  }

  protected Iterable<Integer> getSelection(int currentMonthId) {
    return Collections.singleton(currentMonthId);
  }
}
