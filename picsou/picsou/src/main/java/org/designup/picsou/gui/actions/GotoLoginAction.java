package org.designup.picsou.gui.actions;

import org.globsframework.utils.directory.Directory;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.MainPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GotoLoginAction extends AbstractAction {
  private MainPanel mainPanel;

  public GotoLoginAction(MainPanel mainPanel) {
    super(Lang.get("gotoLogin"));
    this.mainPanel = mainPanel;
  }

  public void actionPerformed(ActionEvent e) {
    mainPanel.loggout();
  }
}
