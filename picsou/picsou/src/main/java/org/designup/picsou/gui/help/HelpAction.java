package org.designup.picsou.gui.help;

import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.*;

public class HelpAction extends AbstractAction {
  private String helpRef;
  private Directory directory;
  private Window owner;

  public HelpAction(String label, String helpRef, Directory directory) {
    this(label, helpRef, directory, directory.get(JFrame.class));
  }

  public HelpAction(String label, String helpRef, Directory directory, Window owner) {
    super(label);
    this.helpRef = helpRef;
    this.directory = directory;
    this.owner = owner;
  }

  public void actionPerformed(ActionEvent e) {
    directory.get(HelpService.class).show(helpRef, owner);
  }
}
