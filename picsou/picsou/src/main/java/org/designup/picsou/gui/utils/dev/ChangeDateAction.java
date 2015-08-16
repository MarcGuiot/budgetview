package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Dates;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;

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
                      value(CurrentMonth.CURRENT_MONTH, Month.getMonthIdFromFullDate(fullDate)),
                      value(CurrentMonth.CURRENT_DAY, Month.getDayFromFullDate(fullDate)));
  }
}
