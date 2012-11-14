package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.model.AccountPositionError;
import org.globsframework.model.FieldValue;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;

public class AddAccountErrorAction extends AbstractAction {

  private GlobRepository repository;

  public AddAccountErrorAction(GlobRepository repository) {
    super("[add position error]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    repository.create(AccountPositionError.TYPE,
                      FieldValue.value(AccountPositionError.IMPORTED_POSITION, 10.),
                      FieldValue.value(AccountPositionError.LAST_REAL_OPERATION_POSITION, 20.),
                      FieldValue.value(AccountPositionError.UPDATE_DATE, new Date()));
  }
}
