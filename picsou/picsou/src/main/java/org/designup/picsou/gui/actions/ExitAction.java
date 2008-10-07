package org.designup.picsou.gui.actions;

import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitAction extends AbstractAction {
  private JFrame frame;

  public ExitAction(Directory directory) {
    super(Lang.get("exit"));
    this.frame = directory.get(JFrame.class);
  }

  public void actionPerformed(ActionEvent e) {
    frame.setVisible(false);
    frame.dispose();
    //   System.exit(0);
  }
}
