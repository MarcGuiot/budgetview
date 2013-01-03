package org.designup.picsou.bank;

import org.designup.picsou.bank.plugins.AbstractBankPlugin;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.Strings;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BankPluginService {
  private Map<Integer, BankPlugin> specific = new HashMap<Integer, BankPlugin>();
  private BankPlugin defaultPlugin = new AbstractBankPlugin() {

    public boolean apply(Glob importedAccount, Glob account, GlobList transactions, ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
      Glob bank = localRepository.find(Key.create(Bank.TYPE, account.get(Account.BANK)));
      if (bank.get(Bank.INVALID_POSITION)) {
// TODO: ??
//      localRepository.update(account.getKey(), Account.);
      }
      else {
        String amount = importedAccount.get(RealAccount.POSITION);
        if (Strings.isNotEmpty(amount)) {
          Date currentDate = account.get(Account.POSITION_DATE);
          Date importDate = importedAccount.get(RealAccount.POSITION_DATE);
          // ce doit etre la derniere date.
          if (importDate != null && (currentDate == null || importDate.after(currentDate))) {
            localRepository.update(account.getKey(),
                                   FieldValue.value(Account.LAST_IMPORT_POSITION, Amounts.extractAmount(amount)));
          }
        }
      }

      return super.apply(importedAccount, account, transactions, referenceRepository, localRepository, changeSet);
    }

    public int getVersion() {
      return 0;
    }
  };

  public boolean apply(Glob account, Glob currentImportedAcount, GlobList transactions, ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
    int bankId = account.get(Account.BANK);
    BankPlugin bankPlugin = specific.get(bankId);
    if (bankPlugin == null) {
      bankPlugin = defaultPlugin;
    }
    return bankPlugin.apply(currentImportedAcount, account, transactions, referenceRepository, localRepository, changeSet);
  }

  public void add(Integer bankId, BankPlugin bankPlugin) {
    BankPlugin actualPlugin = specific.get(bankId);
    if (actualPlugin == null || actualPlugin.getVersion() < bankPlugin.getVersion()) {
      specific.put(bankId, bankPlugin);
    }
  }
}
