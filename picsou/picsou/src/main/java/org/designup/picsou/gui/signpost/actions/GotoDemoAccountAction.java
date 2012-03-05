package org.designup.picsou.gui.signpost.actions;

import org.designup.picsou.gui.startup.components.LogoutService;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GotoDemoAccountAction extends AbstractAction {

  private Directory directory;

  public GotoDemoAccountAction(Directory directory) {
    super(Lang.get("signpostView.gotoDemo.button"));
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    directory.get(LogoutService.class).gotoDemoAccount();
  }
}
