package org.designup.picsou.gui.actions;

import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.WindowManager;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitAction extends AbstractAction {
  private JFrame frame;
  private WindowManager windowManager;

  public ExitAction(WindowManager windowManager, Directory directory) {
    super(Lang.get("exit"));
    this.windowManager = windowManager;
    this.frame = directory.get(JFrame.class);
  }

  public void actionPerformed(ActionEvent e) {
    frame.setVisible(false);
    frame.dispose();
    windowManager.shutdown();
  }
}
