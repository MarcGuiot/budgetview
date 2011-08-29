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

  public boolean apply(Glob importedAccount, Glob account, GlobList transactions, ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
    if (AccountCardType.DEFERRED.getId().equals(account.get(Account.CARD_TYPE))) {
      localRepository.startChangeSet();
      for (Glob transaction : transactions) {
        localRepository.update(transaction.getKey(), ImportedTransaction.AMOUNT, -transaction.get(ImportedTransaction.AMOUNT));
      }
      localRepository.update(account.getKey(),
                             FieldValue.value(Account.POSITION, null),
                             FieldValue.value(Account.POSITION_DATE, null),
                             FieldValue.value(Account.TRANSACTION_ID, null));
      localRepository.completeChangeSet();
    }
    return false;
  }

  public int getVersion() {
    return 0;
  }
}
