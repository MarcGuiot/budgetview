package org.designup.picsou.bank;

import org.designup.picsou.bank.specific.AbstractBankPlugin;
import org.designup.picsou.model.Account;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.ReadOnlyGlobRepository;
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

  public void apply(ReadOnlyGlobRepository referenceRepository, GlobRepository localRepository, MutableChangeSet changeSet) {

    Set<Key> createdAcountsKey = changeSet.getCreated(Account.TYPE);
    for (Key key : createdAcountsKey) {
      Glob newAccount = localRepository.get(key);
      Integer bankId = newAccount.get(Account.BANK);
      BankPlugin bankPlugin = specific.get(bankId);
      if (bankPlugin == null) {
        bankPlugin = defaultPlugin;
      }
      bankPlugin.apply(newAccount, referenceRepository, localRepository, changeSet);
    }
  }

  public void add(Integer bankId, BankPlugin bankPlugin) {
    BankPlugin actualPlugin = specific.get(bankId);
    if (actualPlugin == null || actualPlugin.getVersion() < bankPlugin.getVersion()) {
      specific.put(bankId, bankPlugin);
    }
  }
}
