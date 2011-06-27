package org.designup.picsou.bank.specific;

import org.designup.picsou.bank.BankPluginService;
import org.designup.picsou.gui.model.CurrentAccountInfo;
import org.designup.picsou.model.*;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

public class LaPoste extends AbstractBankPlugin {

  public LaPoste(GlobRepository repository, Directory directory) {
    BankPluginService bankPluginService = directory.get(BankPluginService.class);
    Glob bankEntity = repository.get(Key.create(BankEntity.TYPE, 20041));
    bankPluginService.add(bankEntity.get(BankEntity.BANK), this);
  }

  public boolean useCreatedAccount() {
    return false;
  }

  public void apply(Glob newAccount, ReadOnlyGlobRepository referenceRepository,
                    GlobRepository localRepository, MutableChangeSet changeSet) {
    GlobList transactions = localRepository.getAll(ImportedTransaction.TYPE,
                                                   GlobMatchers.fieldEquals(ImportedTransaction.ACCOUNT, newAccount.get(Account.ID)));
    for (Glob transaction : transactions) {
      localRepository.update(transaction.getKey(), ImportedTransaction.ACCOUNT, null);
    }
    Glob info = localRepository.findOrCreate(Key.create(CurrentAccountInfo.TYPE, 0));
    Glob bankEntity = localRepository.get(Key.create(BankEntity.TYPE, 20041));
    localRepository.update(info.getKey(), FieldValue.value(CurrentAccountInfo.BANK, bankEntity.get(BankEntity.BANK)),
                           FieldValue.value(CurrentAccountInfo.POSITION_DATE, newAccount.get(Account.POSITION_DATE)),
                           FieldValue.value(CurrentAccountInfo.POSITION, newAccount.get(Account.POSITION)));
//    if (transactions.isEmpty()) {
    localRepository.delete(newAccount.getKey());
//    super.apply(newAccount, referenceRepository, localRepository, changeSet);
//      return;
//    }
//    GlobList existingAccounts = getSameAccount(newAccount, referenceRepository);
//    if (existingAccounts.isEmpty()) {
//      localRepository.update(newAccount.getKey(),
//                             FieldValue.value(Account.NUMBER, null),
//                             FieldValue.value(Account.POSITION, 0.),
//                             FieldValue.value(Account.ACCOUNT_TYPE, null),
//                             FieldValue.value(Account.CARD_TYPE, null));
//    }
//    else if (existingAccounts.size() == 1) {
//      updateImportedTransaction(localRepository, newAccount, existingAccounts.getFirst());
//    }
  }

  public void postApply(GlobList transactions, Glob account, GlobRepository repository, GlobRepository localRepository, ChangeSet set) {
    if (AccountCardType.DEFERRED.getId().equals(account.get(Account.CARD_TYPE))) {
      localRepository.startChangeSet();
      for (Glob transaction : transactions) {
        localRepository.update(transaction.getKey(), Transaction.AMOUNT, -transaction.get(Transaction.AMOUNT));
      }
      localRepository.completeChangeSet();
    }
  }

  public int getVersion() {
    return 0;
  }
}
