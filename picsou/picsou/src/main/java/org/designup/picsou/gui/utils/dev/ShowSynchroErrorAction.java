package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.gui.importer.components.SynchroErrorDialog;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowSynchroErrorAction extends AbstractAction {

  private Directory directory;

  public ShowSynchroErrorAction(Directory directory) {
    super("Show synchro error");
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    SynchroErrorDialog.show("Blah\nblah\nblah...", SynchroErrorDialog.Mode.LOGIN, directory.get(JFrame.class), directory);
  }
}
