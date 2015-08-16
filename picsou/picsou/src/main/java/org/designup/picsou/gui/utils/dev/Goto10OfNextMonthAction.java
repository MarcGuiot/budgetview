package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.gui.time.TimeService;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;

import static org.globsframework.model.FieldValue.value;

public class Goto10OfNextMonthAction extends AbstractAction {

  public static final String LABEL = "Goto 10 of next month";
  private GlobRepository repository;

  public Goto10OfNextMonthAction(GlobRepository repository) {
    super(LABEL);
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    Integer currentMonthId = Month.next(currentMonth.get(CurrentMonth.CURRENT_MONTH));
    repository.update(CurrentMonth.KEY,
                      value(CurrentMonth.CURRENT_MONTH, currentMonthId),
                      value(CurrentMonth.CURRENT_DAY, 10));
    Date date = Month.toDate(currentMonth.get(CurrentMonth.CURRENT_MONTH),
                             currentMonth.get(CurrentMonth.CURRENT_DAY));
    TimeService.setCurrentDate(date);
  }
}
