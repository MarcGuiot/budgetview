package com.budgetview.bank.plugins;

import com.budgetview.bank.BankPluginService;
import com.budgetview.model.Account;
import com.budgetview.model.AccountCardType;
import com.budgetview.model.BankEntity;
import com.budgetview.model.ImportedTransaction;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
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
                             FieldValue.value(Account.LAST_IMPORT_POSITION, null));
      localRepository.completeChangeSet();
    }
    return false;
  }

  public int getVersion() {
    return 0;
  }
}
