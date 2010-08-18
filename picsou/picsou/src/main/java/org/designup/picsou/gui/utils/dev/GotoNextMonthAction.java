package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GotoNextMonthAction extends AbstractAction {

  private GlobRepository repository;

  public GotoNextMonthAction(GlobRepository repository) {
    super("[Goto to 10 of next month]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    Integer currentMonthId = Month.next(currentMonth.get(CurrentMonth.CURRENT_MONTH));
    repository.update(CurrentMonth.KEY,
                      FieldValue.value(CurrentMonth.CURRENT_MONTH, currentMonthId),
                      FieldValue.value(CurrentMonth.CURRENT_DAY, 10));
  }
}
