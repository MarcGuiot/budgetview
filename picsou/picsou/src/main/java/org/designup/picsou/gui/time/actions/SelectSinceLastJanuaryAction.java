package org.designup.picsou.gui.time.actions;

import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SelectSinceLastJanuaryAction extends AbstractMonthSelectionAction {
  public SelectSinceLastJanuaryAction(GlobRepository repository, Directory directory) {
    super(Lang.get("selectMonth.sinceLastJanuary"), repository, directory);
  }

  protected Iterable<Integer> getSelection(int currentMonthId) {
    return Month.range((currentMonthId / 100) * 100 + 1, currentMonthId);
  }
}
