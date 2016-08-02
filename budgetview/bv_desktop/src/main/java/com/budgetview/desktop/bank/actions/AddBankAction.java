package com.budgetview.desktop.bank.actions;

import com.budgetview.desktop.bank.BankEditionDialog;
import com.budgetview.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AddBankAction extends AbstractAction {
  
  private BankEditionDialog dialog;
  private Window owner;
  private GlobRepository repository;
  private Directory directory;

  public AddBankAction(Window owner, GlobRepository repository, Directory directory) {
    super(Lang.get("bank.add.action"));
    this.owner = owner;
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    if (dialog == null) {
      dialog = new BankEditionDialog(owner, repository, directory);
    }
    Key createdBankKey = dialog.showNewBank();
    if (createdBankKey != null) {
      SelectionService selectionService = directory.get(SelectionService.class);
      selectionService.select(repository.get(createdBankKey));
    }
  }
}
