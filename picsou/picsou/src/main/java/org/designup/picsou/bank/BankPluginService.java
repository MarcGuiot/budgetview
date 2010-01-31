package org.designup.picsou.bank;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.ImportedTransaction;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BankPluginService {
  private Map<Integer, BankPlugin> specific = new HashMap<Integer, BankPlugin>();

  public void apply(ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {

    Set<Key> createdAcountsKey = changeSet.getCreated(Account.TYPE);
    for (Key key : createdAcountsKey) {
      Glob newAccount = localRepository.get(key);
      Integer bankId = newAccount.get(Account.BANK);
      BankPlugin bankPlugin = specific.get(bankId);
      if (bankPlugin != null) {
        bankPlugin.apply(referenceRepository, localRepository, changeSet);
      }
      else {
        GlobList existingAccounts = referenceRepository.getAll(Account.TYPE,
                                                               GlobMatchers.fieldEquals(Account.NUMBER, newAccount.get(Account.NUMBER)));
        if (existingAccounts.size() == 1) {
          Glob realAccount = existingAccounts.getFirst();
          updateImportedTransaction(localRepository, newAccount, realAccount);
        }
        if (existingAccounts.size() == 2) {
          //
        }
      }
    }
  }

  public static void updateImportedTransaction(GlobRepository localRepository, Glob newAccount,
                                               Glob existingAccount) {
    Integer existingAccountId;
    Date previousDate = existingAccount.get(Account.POSITION_DATE);
    Date updateDate = newAccount.get(Account.POSITION_DATE);
    if ((updateDate != null) && ((previousDate == null)
                                 || updateDate.equals(previousDate)
                                 || updateDate.after(previousDate))) {
      localRepository.update(existingAccount.getKey(),
                             value(Account.POSITION_DATE, updateDate),
                             value(Account.POSITION, newAccount.get(Account.POSITION)),
                             value(Account.TRANSACTION_ID, null));
    }

    GlobUtils.copy(localRepository, newAccount, existingAccount, Account.UPDATE_MODE);

    existingAccountId = existingAccount.get(Account.ID);
    GlobList transactions = localRepository.getAll(ImportedTransaction.TYPE,
                                                   GlobMatchers.fieldEquals(ImportedTransaction.ACCOUNT, newAccount.get(Account.ID)));
    for (Glob transaction : transactions) {
      localRepository.update(transaction.getKey(), ImportedTransaction.ACCOUNT, existingAccountId);
    }
    localRepository.delete(newAccount.getKey());
  }

  public void add(Integer bankId, BankPlugin bankPlugin) {
    BankPlugin actualPlugin = specific.get(bankId);
    if (actualPlugin == null || actualPlugin.getVersion() < bankPlugin.getVersion()) {
      specific.put(bankId, bankPlugin);
    }
  }
}
