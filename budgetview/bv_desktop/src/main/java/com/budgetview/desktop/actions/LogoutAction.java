package com.budgetview.desktop.actions;

import com.budgetview.desktop.startup.components.LogoutService;
import com.budgetview.utils.Lang;

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
