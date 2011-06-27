package org.designup.picsou.bank.specific;

import org.designup.picsou.bank.BankPluginService;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.model.Account;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.directory.Directory;

public class Barclays extends AbstractBankPlugin{

  public Barclays(GlobRepository globRepository, Directory directory) {
    BankPluginService bankPluginService = directory.get(BankPluginService.class);
    Glob bankEntity = globRepository.get(Key.create(BankEntity.TYPE, 24599));
    bankPluginService.add(bankEntity.get(BankEntity.BANK), this);
  }

  public void apply(Glob newAccount, ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
    GlobList existingAccounts = getSameAccount(newAccount, referenceRepository);
    if (existingAccounts.isEmpty()) {
      localRepository.update(newAccount.getKey(),
                             FieldValue.value(Account.POSITION_DATE, null),
                             FieldValue.value(Account.POSITION, null),
                             FieldValue.value(Account.TRANSACTION_ID, null));
    }
    else if (existingAccounts.size() == 1) {
      localRepository.update(newAccount.getKey(), FieldValue.value(Account.POSITION_DATE, null));
      updateImportedTransaction(localRepository, newAccount, existingAccounts.getFirst());
    }
  }

  public int getVersion() {
    return 0;
  }
}
