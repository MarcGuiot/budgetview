package com.budgetview.desktop.accounts.actions;

import com.budgetview.desktop.accounts.AccountEditionDialog;
import com.budgetview.desktop.accounts.utils.DeleteAccountHandler;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DeleteAccountAction extends AbstractAction {
  private Glob account;
  private GlobRepository parentRepository;
  private Directory directory;

  public DeleteAccountAction(Glob account, GlobRepository parentRepository, Directory directory) {
    super(Lang.get("accountEdition.delete"));
    this.account = account;
    this.parentRepository = parentRepository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    LocalGlobRepository localRepository = AccountEditionDialog.createLocalRepository(parentRepository);
    DeleteAccountHandler handler = new DeleteAccountHandler(directory.get(JFrame.class),
                                                            parentRepository,
                                                            localRepository,
                                                            directory);
    handler.delete(account, false);
  }
}
