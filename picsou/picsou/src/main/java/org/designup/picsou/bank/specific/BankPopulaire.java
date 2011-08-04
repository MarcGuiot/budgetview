package org.designup.picsou.bank.specific;

import org.designup.picsou.bank.BankPluginService;
import org.designup.picsou.model.*;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

public class BankPopulaire extends AbstractBankPlugin {

  public BankPopulaire(GlobRepository globRepository, Directory directory) {
    BankPluginService bankPluginService = directory.get(BankPluginService.class);
    Glob bankEntity = globRepository.get(Key.create(BankEntity.TYPE, 10807));
    bankPluginService.add(bankEntity.get(BankEntity.BANK), this);
  }

  public boolean apply(Glob account, ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
    GlobList transactions = localRepository.getAll(ImportedTransaction.TYPE,
                                                   GlobMatchers.fieldEquals(ImportedTransaction.ACCOUNT, account.get(Account.ID)));
    if (transactions.isEmpty()) {
      localRepository.delete(account.getKey());
      return true;
    }
    String name = transactions.getFirst().get(ImportedTransaction.OFX_NAME);
    if (name != null && name.toUpperCase().startsWith("FACTURETTE CB")) {
      GlobList existingAccounts =
        referenceRepository.getAll(Account.TYPE,
                                   GlobMatchers.and(
                                     GlobMatchers.fieldEquals(Account.NUMBER, account.get(Account.NUMBER)),
                                     GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId())));
      if (existingAccounts.isEmpty()) {
        GlobList all = localRepository.getAll(ImportedTransaction.TYPE,
                                              GlobMatchers.fieldEquals(ImportedTransaction.ACCOUNT, account.get(Account.ID)));
        for (Glob glob : all) {
          localRepository.update(glob.getKey(),
                                 FieldValue.value(ImportedTransaction.OFX_NAME, glob.get(ImportedTransaction.OFX_MEMO)),
                                 FieldValue.value(ImportedTransaction.OFX_MEMO, null));
        }
        Double position = all.getSum(ImportedTransaction.AMOUNT);
        localRepository.update(account.getKey(),
//                               FieldValue.value(Account.POSITION_DATE, null),
                               FieldValue.value(Account.POSITION, position),
                               FieldValue.value(Account.TRANSACTION_ID, null),
                               FieldValue.value(Account.NAME, Account.getName(account.get(Account.NUMBER), Boolean.TRUE)),
                               FieldValue.value(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()),
                               FieldValue.value(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
        return true;
      }
      else if (existingAccounts.size() == 1) {
        localRepository.update(account.getKey(), FieldValue.value(Account.POSITION_DATE, null));
        updateImportedTransaction(localRepository, account, existingAccounts.getFirst());
        return true;
      }
      else if (existingAccounts.size() == 0){
        return true;
      }
    }
    else {
      GlobMatcher globMatcher;
      if (account.get(Account.CARD_TYPE).equals(AccountCardType.UNDEFINED.getId())){
        globMatcher = GlobMatchers.or(GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()),
                                      GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.CREDIT.getId()));
      }
      else {
        globMatcher = GlobMatchers.fieldEquals(Account.CARD_TYPE, account.get(Account.CARD_TYPE));
      }
      GlobList existingAccounts =
        referenceRepository.getAll(Account.TYPE,
                                   GlobMatchers.and(
                                     GlobMatchers.fieldEquals(Account.NUMBER, account.get(Account.NUMBER)),
                                     globMatcher));
      if (existingAccounts.size() == 1) {
        updateImportedTransaction(localRepository, account, existingAccounts.getFirst());
        return true;
      }
      else if (existingAccounts.size() == 0){
        return true;
      }
    }
    return false;
  }

  public int getVersion() {
    return 1;
  }
}
