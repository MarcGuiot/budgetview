package com.budgetview.desktop.utils.dev;

import com.budgetview.desktop.feedback.UsageDataSender;
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
