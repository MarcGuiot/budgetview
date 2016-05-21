package com.budgetview.gui.utils.dev;

import com.budgetview.gui.time.TimeService;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.Month;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;

import static org.globsframework.model.FieldValue.value;

public class AddSixDaysAction extends AbstractAction {

  public static final String LABEL = "Add 6 days";
  private GlobRepository repository;

  public AddSixDaysAction(GlobRepository repository) {
    super(LABEL);
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    Date nextDate = Month.addDays(CurrentMonth.getAsDate(repository), 6);
    TimeService.setCurrentDate(nextDate);
    repository.update(CurrentMonth.KEY,
                      value(CurrentMonth.CURRENT_MONTH, Month.getMonthId(nextDate)),
                      value(CurrentMonth.CURRENT_DAY, Month.getDay(nextDate)));
  }
}
