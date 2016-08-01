package com.budgetview.gui.signpost.actions;

import com.budgetview.gui.startup.components.LogoutService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GotoDemoAccountAction extends AbstractAction {

  private Directory directory;

  public GotoDemoAccountAction(Directory directory) {
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    directory.get(LogoutService.class).gotoDemoAccount();
  }
}
