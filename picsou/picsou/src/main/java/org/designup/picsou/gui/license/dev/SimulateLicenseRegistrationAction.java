package org.designup.picsou.gui.license.dev;

import org.designup.picsou.model.User;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.FieldValue.value;

public class SimulateLicenseRegistrationAction extends AbstractAction {
  private GlobRepository repository;

  public SimulateLicenseRegistrationAction(GlobRepository repository) {
    super("[simulate license registration]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    repository.startChangeSet();
    repository.update(User.KEY, value(User.IS_REGISTERED_USER, true));
    repository.completeChangeSet();
  }
}
