package org.designup.picsou.bank.specific;

import org.designup.picsou.bank.BankPluginService;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountCardType;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.model.ImportedTransaction;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

public class BankPopulaire extends AbstractBankPlugin {

  public BankPopulaire(GlobRepository globRepository, Directory directory) {
    BankPluginService bankPluginService = directory.get(BankPluginService.class);
    Glob bankEntity = globRepository.get(Key.create(BankEntity.TYPE, 10807));
    bankPluginService.add(bankEntity.get(BankEntity.BANK), this);
  }

  public void apply(Glob account, ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
    GlobList transactions = localRepository.getAll(ImportedTransaction.TYPE,
                                                   GlobMatchers.fieldEquals(ImportedTransaction.ACCOUNT, account.get(Account.ID)));
    if (transactions.isEmpty()) {
      localRepository.delete(account.getKey());
      return;
    }
    String name = transactions.getFirst().get(ImportedTransaction.OFX_NAME);
    if (name != null && name.toUpperCase().startsWith("FACTURETTE CB")) {
      GlobList existingAccounts =
        referenceRepository.getAll(Account.TYPE,
                                   GlobMatchers.and(
                                     GlobMatchers.fieldEquals(Account.NUMBER, account.get(Account.NUMBER)),
                                     GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId())));
      if (existingAccounts.isEmpty()) {
        localRepository.update(account.getKey(),
                               FieldValue.value(Account.POSITION_DATE, null),
                               FieldValue.value(Account.POSITION, null),
                               FieldValue.value(Account.TRANSACTION_ID, null),
                               FieldValue.value(Account.NAME, Account.getName(account.get(Account.NUMBER), Boolean.TRUE)),
                               FieldValue.value(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
      }
      else if (existingAccounts.size() == 1) {
        localRepository.update(account.getKey(), FieldValue.value(Account.POSITION_DATE, null));
        updateImportedTransaction(localRepository, account, existingAccounts.getFirst());
      }
    }
    else {
      GlobList existingAccounts =
        referenceRepository.getAll(Account.TYPE,
                                   GlobMatchers.and(
                                     GlobMatchers.fieldEquals(Account.NUMBER, account.get(Account.NUMBER)),
                                     GlobMatchers.not(GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()))));
      if (existingAccounts.size() == 1) {
        updateImportedTransaction(localRepository, account, existingAccounts.getFirst());
      }
    }
  }

  public int getVersion() {
    return 1;
  }
}
