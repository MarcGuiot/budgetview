package org.designup.picsou.gui.help;

import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HelpAction extends AbstractAction {
  private String helpRef;
  private Directory directory;

  public HelpAction(String title, String helpRef, Directory directory) {
    super(title);
    this.helpRef = helpRef;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    directory.get(HelpService.class).show(helpRef, directory.get(JFrame.class));
  }
}
