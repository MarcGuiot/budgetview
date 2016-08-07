package com.budgetview.desktop.cloud.actions;

import com.budgetview.budgea.model.BudgeaBankField;
import com.budgetview.budgea.model.BudgeaBankFieldValue;
import com.budgetview.budgea.model.BudgeaConnection;
import com.budgetview.budgea.model.BudgeaConnectionValue;
import com.budgetview.desktop.cloud.CloudService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.FieldValue.value;


public class BudgeaDemoAction extends AbstractAction {
  private final GlobRepository repository;
  private final CloudService cloudService;

  public BudgeaDemoAction(GlobRepository repository, Directory directory) {
    super("Run Budgea demo");
    this.repository = repository;
    this.cloudService = directory.get(CloudService.class);
  }


  public void actionPerformed(ActionEvent e) {
    try {
      cloudService.updateBankList(repository);
      GlobPrinter.print(repository.getAll(BudgeaBankField.TYPE, GlobMatchers.fieldEquals(BudgeaBankField.BANK, 40)));
      GlobPrinter.print(repository.getAll(BudgeaBankFieldValue.TYPE, GlobMatchers.fieldIn(BudgeaBankFieldValue.FIELD, 170, 171, 172)));

      Glob connection = repository.create(Key.create(BudgeaConnection.TYPE, 40));
      repository.create(BudgeaConnectionValue.TYPE,
                        value(BudgeaConnectionValue.CONNECTION, 40),
                        value(BudgeaConnectionValue.FIELD, 170), // website
                        value(BudgeaConnectionValue.VALUE, "par"));
      repository.create(BudgeaConnectionValue.TYPE,
                        value(BudgeaConnectionValue.CONNECTION, 40),
                        value(BudgeaConnectionValue.FIELD, 171), // login
                        value(BudgeaConnectionValue.VALUE, "12345678"));
      repository.create(BudgeaConnectionValue.TYPE,
                        value(BudgeaConnectionValue.CONNECTION, 40),
                        value(BudgeaConnectionValue.FIELD, 172), // password
                        value(BudgeaConnectionValue.VALUE, "1234"));
      cloudService.createConnection(connection.getKey(), repository);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
