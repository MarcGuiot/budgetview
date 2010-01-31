package org.designup.picsou.license.functests;

import org.designup.picsou.bank.BankPlugin;
import org.designup.picsou.bank.BankPluginService;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.model.ImportedTransaction;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.directory.Directory;

public class DummyBankPlugin implements BankPlugin {

  public DummyBankPlugin(GlobRepository globRepository, Directory directory) {
    BankPluginService bankPluginService = directory.get(BankPluginService.class);
    Glob bankEntity = globRepository.get(Key.create(BankEntity.TYPE, 4321));
    bankPluginService.add(bankEntity.get(BankEntity.BANK), this);
  }

  public void apply(ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
    GlobList list = localRepository.getAll(ImportedTransaction.TYPE);
    for (Glob glob : list) {
      localRepository.update(glob.getKey(), ImportedTransaction.AMOUNT, -234.);
    }
  }

  public int getVersion() {
    return 1;
  }
}
