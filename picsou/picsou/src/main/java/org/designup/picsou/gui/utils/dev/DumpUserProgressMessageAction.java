package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.gui.feedback.UserProgressInfoSender;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DumpUserProgressMessageAction extends AbstractAction {

  private GlobRepository repository;
  private Directory directory;

  public DumpUserProgressMessageAction(GlobRepository repository, Directory directory) {
    super("[Dump user progress message]");
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    System.out.println(UserProgressInfoSender.getMessage(repository, 5));
  }
}
