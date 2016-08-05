package com.budgetview.desktop.cloud.actions;

import com.budgetview.budgea.model.BudgeaBank;
import com.budgetview.budgea.model.BudgeaBankField;
import com.budgetview.budgea.model.BudgeaBankFieldValue;
import com.budgetview.desktop.cloud.CloudService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class BudgeaDemoAction extends AbstractAction {
  private final GlobRepository repository;
  private final CloudService cloudService;

  public BudgeaDemoAction(GlobRepository repository, Directory directory) {
    super("Run cloud demo");
    this.repository = repository;
    this.cloudService = directory.get(CloudService.class);
  }


  public void actionPerformed(ActionEvent e) {
    try {
      System.out.println("Starting...");
      cloudService.updateBankList(repository);
      GlobPrinter.print(repository, BudgeaBank.TYPE, BudgeaBankField.TYPE, BudgeaBankFieldValue.TYPE);

    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
