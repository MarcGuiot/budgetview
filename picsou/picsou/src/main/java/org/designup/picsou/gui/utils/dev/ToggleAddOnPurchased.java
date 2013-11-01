package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.model.User;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleAddOnPurchased extends AbstractAction {

  private boolean enable;
  private GlobRepository repository;

  public ToggleAddOnPurchased(boolean enable, GlobRepository repository) {
    super("[" + (enable ? "Enable" : "Disable") + " Add-ons]");
    this.enable = enable;
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    repository.update(User.KEY, User.IS_REGISTERED_USER, enable);
  }
}
