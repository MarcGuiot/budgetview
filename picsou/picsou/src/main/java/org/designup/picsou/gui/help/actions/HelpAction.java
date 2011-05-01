package org.designup.picsou.gui.help.actions;

import org.designup.picsou.gui.help.HelpService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;

public class HelpAction extends AbstractAction {
  private String helpRef;
  private Directory directory;
  private Window owner;

  public HelpAction(String label, String helpRef, String tooltip, Directory directory) {
    this(label, helpRef, tooltip,  directory, directory.get(JFrame.class));
  }

  public HelpAction(String label, String helpRef, String tooltip, Directory directory, Window owner) {
    super(label);
    this.helpRef = helpRef;
    this.directory = directory;
    this.owner = owner;
    putValue(Action.SHORT_DESCRIPTION, tooltip);
  }

  public void actionPerformed(ActionEvent e) {
    directory.get(HelpService.class).show(helpRef, owner);
  }
}
