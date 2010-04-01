package org.designup.picsou.bank.specific;

import org.designup.picsou.bank.BankPluginService;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountCardType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.directory.Directory;

public class CaisseEpargne extends AbstractBankPlugin {

  public CaisseEpargne(GlobRepository globRepository, Directory directory) {
    BankPluginService bankPluginService = directory.get(BankPluginService.class);
    Glob bankEntity = globRepository.get(Key.create(BankEntity.TYPE, 17515));
    bankPluginService.add(bankEntity.get(BankEntity.BANK), this);
  }

  public void apply(Glob newAccount, ReadOnlyGlobRepository referenceRepository,
                    GlobRepository localRepository, MutableChangeSet changeSet) {
    GlobList existingAccounts =
      referenceRepository.getAll(Account.TYPE,
                                 GlobMatchers.fieldEquals(Account.NUMBER, newAccount.get(Account.NUMBER)));
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
