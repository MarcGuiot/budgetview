package com.budgetview.desktop.about;

import com.budgetview.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AboutAction extends AbstractAction {
  private Directory directory;

  public AboutAction(Directory directory) {
    super(Lang.get("about.menu"));
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    AboutDialog dialog = new AboutDialog(directory);
    dialog.show();
  }
}
