package org.designup.picsou.bank.importer;

import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class SynchronizeAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public SynchronizeAction(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    updateStatus(repository);
    repository.addChangeListener(new TypeChangeSetListener(RealAccount.TYPE) {
      protected void update(GlobRepository repository) {
        updateStatus(repository);
      }
    });
  }

  private void updateStatus(GlobRepository repository) {
    setEnabled(repository.contains(RealAccount.TYPE, fieldEquals(RealAccount.FROM_SYNCHRO, Boolean.TRUE)));
  }

  public void actionPerformed(ActionEvent e) {
    GlobList realAccounts = repository.getAll(RealAccount.TYPE);
    BankSynchroService bankSynchroService = directory.get(BankSynchroService.class);
    GlobList list = bankSynchroService.show(directory.get(JFrame.class), realAccounts, directory, repository);
    if (!list.isEmpty()) {
      ImportFileAction fileAction = ImportFileAction.sync(repository, directory, list);
      fileAction.actionPerformed(null);
    }
  }
}
