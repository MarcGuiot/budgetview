package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;

public class AddSixDayAction extends AbstractAction {

  private GlobRepository repository;

  public AddSixDayAction(GlobRepository repository) {
    super("[Add 6 days]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    Date date = Month.toDate(currentMonth.get(CurrentMonth.CURRENT_MONTH), currentMonth.get(CurrentMonth.CURRENT_DAY));
    Date nextDate = Month.addDays(date, 6);
    TimeService.setCurrentDate(nextDate);
    repository.update(CurrentMonth.KEY,
                      FieldValue.value(CurrentMonth.CURRENT_MONTH, Month.getMonthId(nextDate)),
                      FieldValue.value(CurrentMonth.CURRENT_DAY, Month.getDay(nextDate)));
  }
}
