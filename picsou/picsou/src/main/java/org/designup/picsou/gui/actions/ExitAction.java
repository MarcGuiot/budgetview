package org.designup.picsou.gui.actions;

import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExitAction extends AbstractAction {
  private JFrame frame;

  public ExitAction(Directory directory) {
    super(Lang.get("exit"));
    this.frame = directory.get(JFrame.class);
  }

  public void actionPerformed(ActionEvent e) {
    frame.dispose();
    System.exit(0);
  }
}
