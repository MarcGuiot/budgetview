package org.designup.picsou.bank.importer;

import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SynchronizeAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public SynchronizeAction(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    setEnabled(repository.contains(RealAccount.TYPE,
                                   GlobMatchers.fieldEquals(RealAccount.FROM_SYNCHRO, Boolean.TRUE)));
    repository.addChangeListener(new TypeChangeSetListener(RealAccount.TYPE) {
      protected void update(GlobRepository repository) {
        setEnabled(repository.contains(RealAccount.TYPE,
                                       GlobMatchers.fieldEquals(RealAccount.FROM_SYNCHRO, Boolean.TRUE)));
      }
    });
  }

  public void actionPerformed(ActionEvent e) {
    GlobList realAccounts = repository.getAll(RealAccount.TYPE);
    BankSynchroService bankSynchroService = directory.get(BankSynchroService.class);
    GlobList list = bankSynchroService.show(realAccounts, directory, repository);
    if (!list.isEmpty()) {
      ImportFileAction fileAction = ImportFileAction.sync(repository, directory, list);
      fileAction.actionPerformed(null);
    }
  }
}
