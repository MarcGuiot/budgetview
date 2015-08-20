package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.gui.feedback.UsageDataSender;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DumpUsageDataAction extends AbstractAction {

  private GlobRepository repository;

  public DumpUsageDataAction(GlobRepository repository) {
    super("Dump usage data message");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    System.out.println(UsageDataSender.getMessage(repository, 5));
  }
}
