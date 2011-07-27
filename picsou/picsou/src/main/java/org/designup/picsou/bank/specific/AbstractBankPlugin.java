package org.designup.picsou.bank.specific;

import org.designup.picsou.bank.BankPlugin;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.ImportedTransaction;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobUtils;

import java.util.Date;

public abstract class AbstractBankPlugin implements BankPlugin {

  public AbstractBankPlugin() {
  }

  public boolean useCreatedAccount() {
    return true;
  }

  public void apply(Glob newAccount, ReadOnlyGlobRepository referenceRepository,
                    GlobRepository localRepository, MutableChangeSet changeSet) {
    GlobList existingAccounts = getSameAccount(newAccount, referenceRepository);
    if (existingAccounts.size() == 1) {
      Glob realAccount = existingAccounts.getFirst();
      updateImportedTransaction(localRepository, newAccount, realAccount);
    }
    if (existingAccounts.size() == 2) {
      //
    }
  }

  protected GlobList getSameAccount(Glob newAccount, ReadOnlyGlobRepository referenceRepository) {
    return referenceRepository.getAll(Account.TYPE,
                                        GlobMatchers.fieldEquals(Account.NUMBER, newAccount.get(Account.NUMBER)));
  }

  public void postApply(GlobList transactions, Glob account, GlobRepository repository, GlobRepository localRepository, ChangeSet set) {
  }

  static public void updateImportedTransaction(GlobRepository localRepository, Glob newAccount, Glob existingAccount) {
    Integer existingAccountId;
    Date previousDate = existingAccount.get(Account.POSITION_DATE);
    Date updateDate = newAccount.get(Account.POSITION_DATE);
    if ((updateDate != null) && ((previousDate == null)
                                 || updateDate.equals(previousDate)
                                 || updateDate.after(previousDate))) {
      localRepository.update(existingAccount.getKey(),
                             FieldValue.value(Account.POSITION_DATE, updateDate),
                             FieldValue.value(Account.POSITION, newAccount.get(Account.POSITION)),
                             FieldValue.value(Account.TRANSACTION_ID, null));
    }

    GlobUtils.copy(localRepository, newAccount, existingAccount, Account.UPDATE_MODE);

    existingAccountId = existingAccount.get(Account.ID);
    GlobList transactions = localRepository.getAll(ImportedTransaction.TYPE,
                                                   GlobMatchers.fieldEquals(ImportedTransaction.ACCOUNT,
                                                                            newAccount.get(Account.ID)));
    for (Glob transaction : transactions) {
      localRepository.update(transaction.getKey(), ImportedTransaction.ACCOUNT, existingAccountId);
    }
    localRepository.delete(newAccount.getKey());
  }
}
