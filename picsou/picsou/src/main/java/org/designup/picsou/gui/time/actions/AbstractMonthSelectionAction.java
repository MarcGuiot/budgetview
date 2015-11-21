package org.designup.picsou.gui.time.actions;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.Month;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class AbstractMonthSelectionAction extends AbstractAction {

  protected GlobRepository repository;
  protected Directory directory;

  public AbstractMonthSelectionAction(String label, GlobRepository repository, Directory directory) {
    super(label);
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    Iterable<Integer> monthIds = getSelection(directory.get(TimeService.class).getCurrentMonthId());
    if (monthIds != null) {
      directory.get(SelectionService.class).select(getMonths(monthIds), Month.TYPE);
    }
  }

  protected GlobList getMonths(Iterable<Integer> monthIds) {
    GlobList result = new GlobList();
    for (Integer monthId : monthIds) {
      Glob month = repository.find(Key.create(Month.TYPE, monthId));
      if (month != null) {
        result.add(month);
      }
    }
    return result;
  }

  protected abstract Iterable<Integer> getSelection(int currentMonthId);
}
