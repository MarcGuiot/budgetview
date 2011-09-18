package org.designup.picsou.bank.importer;

import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;
import org.globsframework.metamodel.GlobType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class SynchronizeAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public SynchronizeAction(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    setEnabled(repository.contains(RealAccount.TYPE));
    repository.addChangeListener(new TypeChangeSetListener(RealAccount.TYPE) {
      protected void update(GlobRepository repository) {
        setEnabled(repository.contains(RealAccount.TYPE));
      }
    });
  }

  public void actionPerformed(ActionEvent e) {
    GlobList realAccounts = repository.getAll(RealAccount.TYPE);
    BankSynchroService bankSynchroService = directory.get(BankSynchroService.class);
    GlobList list = bankSynchroService.show(realAccounts, directory, repository);
    if (!list.isEmpty()){
      ImportFileAction fileAction = ImportFileAction.sync(repository, directory, list);
      fileAction.actionPerformed(null);
    }
  }
}
