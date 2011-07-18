package org.designup.picsou.bank.importer;

import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
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
    setEnabled(repository.contains(RealAccount.TYPE,
                                   GlobMatchers.fieldEquals(RealAccount.IMPORTED, Boolean.TRUE)));
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(RealAccount.TYPE)){
          setEnabled(repository.contains(RealAccount.TYPE,
                                         GlobMatchers.fieldEquals(RealAccount.IMPORTED, Boolean.TRUE)));
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(RealAccount.TYPE)){
          setEnabled(repository.contains(RealAccount.TYPE,
                                         GlobMatchers.fieldEquals(RealAccount.IMPORTED, Boolean.TRUE)));
        }
      }
    });
  }

  public void actionPerformed(ActionEvent e) {
    GlobList realAccounts = repository.getAll(RealAccount.TYPE);
    Set<Integer> banks = new HashSet<Integer>();
    for (Glob realAccount : realAccounts) {
      if (realAccount.get(RealAccount.IMPORTED)) {
        Integer bank = realAccount.get(RealAccount.BANK);
        banks.add(bank);
      }
    }
    BankSynchroService bankSynchroService = directory.get(BankSynchroService.class);
    for (Integer bank : banks) {
      bankSynchroService.show(bank, directory, repository);
    }
  }
}
