package org.designup.picsou.bank.specific;

import org.designup.picsou.bank.BankPlugin;
import org.designup.picsou.bank.BankPluginService;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountCardType;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.model.ImportedTransaction;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class BankPopulaire implements BankPlugin {

  public BankPopulaire(GlobRepository globRepository, Directory directory) {
    BankPluginService bankPluginService = directory.get(BankPluginService.class);
    Glob bankEntity = globRepository.get(Key.create(BankEntity.TYPE, 10807));
    bankPluginService.add(bankEntity.get(BankEntity.BANK), this);
  }

  public void apply(ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
    Set<Key> createdAcountsKey = changeSet.getCreated(Account.TYPE);
    for (Key key : createdAcountsKey) {
      Glob account = localRepository.get(key);

      GlobList transactions = localRepository.getAll(ImportedTransaction.TYPE, GlobMatchers.fieldEquals(ImportedTransaction.ACCOUNT, account.get(Account.ID)));
      if (transactions.isEmpty()) {
        localRepository.delete(key);
        continue;
      }
      String name = transactions.getFirst().get(ImportedTransaction.OFX_NAME);
      if (name != null && name.toUpperCase().startsWith("FACTURETTE CB")) {
        GlobList existingAccounts =
          referenceRepository.getAll(Account.TYPE,
                                     GlobMatchers.and(
                                       GlobMatchers.fieldEquals(Account.NUMBER, account.get(Account.NUMBER)),
                                       GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId())));
        if (existingAccounts.isEmpty()) {
          localRepository.update(key,
                                 FieldValue.value(Account.POSITION_DATE, null),
                                 FieldValue.value(Account.POSITION, null),
                                 FieldValue.value(Account.TRANSACTION_ID, null),
                                 FieldValue.value(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
        }
        else if (existingAccounts.size() == 1) {
          localRepository.update(account.getKey(), FieldValue.value(Account.POSITION_DATE, null));
          BankPluginService.updateImportedTransaction(localRepository, account, existingAccounts.getFirst());
        }
      }
      else {
        GlobList existingAccounts =
          referenceRepository.getAll(Account.TYPE,
                                     GlobMatchers.and(
                                       GlobMatchers.fieldEquals(Account.NUMBER, account.get(Account.NUMBER)),
                                       GlobMatchers.not(GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()))));
        if (existingAccounts.size() == 1) {
          BankPluginService.updateImportedTransaction(localRepository, account, existingAccounts.getFirst());
        }
      }
    }
  }

  public int getVersion() {
    return 1;
  }
}
