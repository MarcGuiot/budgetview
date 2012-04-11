package org.designup.picsou.bank;

import org.designup.picsou.bank.specific.AbstractBankPlugin;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.*;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
          localRepository.update(account.getKey(),
                                 FieldValue.value(Account.POSITION, Amounts.extractAmount(amount)),
                                 FieldValue.value(Account.POSITION_DATE, importedAccount.get(RealAccount.POSITION_DATE)),
                                 FieldValue.value(Account.TRANSACTION_ID, null));
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
