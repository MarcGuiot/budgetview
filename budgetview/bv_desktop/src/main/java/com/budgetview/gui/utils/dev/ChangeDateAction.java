package com.budgetview.gui.utils.dev;

import com.budgetview.model.CurrentMonth;
import com.budgetview.model.Month;
import com.budgetview.gui.time.TimeService;
import org.globsframework.model.FieldValue;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.FieldValue.value;

public class ChangeDateAction extends AbstractAction {

  public static final String LABEL = "Change current date";
  private GlobRepository repository;

  public ChangeDateAction(GlobRepository repository) {
    super(LABEL);
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    int fullDate = TimeService.getCurrentFullDate();
    repository.update(CurrentMonth.KEY,
                      FieldValue.value(CurrentMonth.CURRENT_MONTH, Month.getMonthIdFromFullDate(fullDate)),
                      value(CurrentMonth.CURRENT_DAY, Month.getDayFromFullDate(fullDate)));
  }
}
