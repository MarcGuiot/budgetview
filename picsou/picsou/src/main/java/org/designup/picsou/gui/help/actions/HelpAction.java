package org.designup.picsou.gui.help.actions;

import org.designup.picsou.gui.help.HelpService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HelpAction extends AbstractAction {
  private String helpRef;
  private Directory directory;

  public HelpAction(String label, String helpRef, String tooltip, Directory directory) {
    super(label);
    this.helpRef = helpRef;
    this.directory = directory;
    putValue(Action.SHORT_DESCRIPTION, tooltip);
  }

  public void actionPerformed(ActionEvent e) {
    directory.get(HelpService.class).show(helpRef);
  }
}
