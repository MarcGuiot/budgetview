package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.startup.components.LogoutService;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class LogoutAction extends AbstractAction {
  private LogoutService logoutService;

  public LogoutAction(LogoutService logoutService) {
    super(Lang.get("gotoLogin"));
    this.logoutService = logoutService;
  }

  public void actionPerformed(ActionEvent e) {
    logoutService.logout();
  }
}
