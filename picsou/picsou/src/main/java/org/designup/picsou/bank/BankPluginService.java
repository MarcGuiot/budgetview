package org.designup.picsou.bank;

import org.designup.picsou.bank.specific.AbstractBankPlugin;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.gui.bank.BankChooserDialog;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.delta.MutableChangeSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BankPluginService {
  private Map<Integer, BankPlugin> specific = new HashMap<Integer, BankPlugin>();
  private BankPlugin defaultPlugin = new AbstractBankPlugin() {
    public int getVersion() {
      return 0;
    }
  };

  public boolean useCreatedAccount(Glob account) {
    Integer bankId = account.get(Account.BANK);
    BankPlugin bankPlugin = specific.get(bankId);
    if (bankPlugin == null) {
      bankPlugin = defaultPlugin;
    }
    return bankPlugin.useCreatedAccount();
  }

  public void apply(ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {
    Set<Key> createdAcountsKey = changeSet.getCreated(Account.TYPE);
    for (Key key : createdAcountsKey) {
      Glob account = localRepository.get(key);
      Integer bankId = account.get(Account.BANK);
      if (bankId == null){
        Glob sameAccount = findFirstSameAccount(referenceRepository, account);
        if (sameAccount == null){
          sameAccount = findFirstSameAccount(localRepository, account);
        }
        if (sameAccount != null){
          bankId = sameAccount.get(Account.BANK);
        }
      }
      BankPlugin bankPlugin = specific.get(bankId);
      if (bankPlugin == null) {
        bankPlugin = defaultPlugin;
      }
      bankPlugin.apply(account, referenceRepository, localRepository, changeSet);
    }
  }

  private Glob findFirstSameAccount(ReadOnlyGlobRepository referenceRepository, Glob account) {
    return referenceRepository.getAll(Account.TYPE, GlobMatchers.and(
      GlobMatchers.fieldEquals(Account.NUMBER, account.get(Account.NUMBER)),
      GlobMatchers.not(GlobMatchers.fieldEquals(Account.ID, account.get(Account.ID))))).getFirst();
  }

  public void add(Integer bankId, BankPlugin bankPlugin) {
    BankPlugin actualPlugin = specific.get(bankId);
    if (actualPlugin == null || actualPlugin.getVersion() < bankPlugin.getVersion()) {
      specific.put(bankId, bankPlugin);
    }
  }

  public void postApply(GlobList transactions, GlobRepository referenceRepository, GlobRepository localRepository, ChangeSet importChangeSet) {
    Set<Integer> accountIds = transactions.getValueSet(Transaction.ACCOUNT);
    for (Integer accountId : accountIds) {
      Glob account = localRepository.find(Key.create(Account.TYPE, accountId));
      Integer bankId = account.get(Account.BANK);
      BankPlugin bankPlugin = specific.get(bankId);
      if (bankPlugin == null) {
        bankPlugin = defaultPlugin;
      }
      bankPlugin.postApply(transactions.filter(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId), localRepository),
                           account,
                           referenceRepository, localRepository, importChangeSet);
    }
  }
}
