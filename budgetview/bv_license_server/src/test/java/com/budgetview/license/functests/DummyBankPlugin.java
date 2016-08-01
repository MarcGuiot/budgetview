package com.budgetview.license.functests;

import com.budgetview.bank.BankPlugin;
import com.budgetview.bank.BankPluginService;
import com.budgetview.model.ImportedTransaction;
import com.budgetview.model.BankEntity;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.directory.Directory;

public class DummyBankPlugin implements BankPlugin {

  public DummyBankPlugin(GlobRepository globRepository, Directory directory) {
    BankPluginService bankPluginService = directory.get(BankPluginService.class);
    Glob bankEntity = globRepository.get(Key.create(BankEntity.TYPE, 4321));
    bankPluginService.add(bankEntity.get(BankEntity.BANK), this);
  }

  public boolean apply(Glob importedAccount, Glob newAccount, GlobList transactions, ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
    GlobList list = localRepository.getAll(ImportedTransaction.TYPE);
    for (Glob glob : list) {
      localRepository.update(glob.getKey(), ImportedTransaction.AMOUNT, -234.);
    }
    return true;
  }

  public void postApply(GlobList list, Glob account, GlobRepository repository, GlobRepository localRepository, ChangeSet set) {
  }

  public int getVersion() {
    return 1;
  }
}
