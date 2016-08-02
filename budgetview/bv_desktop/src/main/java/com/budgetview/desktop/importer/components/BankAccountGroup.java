package com.budgetview.desktop.importer.components;

import com.budgetview.desktop.accounts.utils.AccountMatchers;
import com.budgetview.model.Account;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.RealAccount;
import com.budgetview.model.Synchro;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.model.utils.GlobMatcher;

import java.util.*;

import static org.globsframework.model.utils.GlobMatchers.*;

public class BankAccountGroup {
  private final Glob bank;
  private final GlobList accounts = new GlobList();

  private BankAccountGroup(Glob bank) {
    this.bank = bank;
  }

  public Glob getBank() {
    return bank;
  }

  private void add(Glob account) {
    accounts.add(account);
  }

  public List<Glob> getAccounts() {
    return Collections.unmodifiableList(accounts);
  }

  public static List<BankAccountGroup> getManualAccountGroups(GlobRepository repository) {
    GlobList realAccounts = repository.getAll(RealAccount.TYPE, and(isNull(RealAccount.SYNCHRO), isFalse(RealAccount.FROM_SYNCHRO)));
    GlobList accounts = realAccounts.getTargets(RealAccount.ACCOUNT, repository);
    accounts.filterSelf(AccountMatchers.accountsNotClosedAsOf(CurrentMonth.getAsDate(repository)), repository);
    accounts.sortSelf(new GlobFieldsComparator(Account.ACCOUNT_TYPE, true,
                                               Account.POSITION_DATE, true,
                                               Account.NAME, true));
    List<BankAccountGroup> result = new ArrayList<BankAccountGroup>();
    Map<Glob, BankAccountGroup> groups = new HashMap<Glob, BankAccountGroup>();
    for (Glob account : accounts) {
      if (!hasASynchroFor(account, repository)) {
        Glob bank = repository.findLinkTarget(account, Account.BANK);
        BankAccountGroup group = groups.get(bank);
        if (group == null) {
          group = new BankAccountGroup(bank);
          result.add(group);
          groups.put(bank, group);
        }
        group.add(account);
      }
    }

    return result;
  }

  private static boolean hasASynchroFor(Glob account, GlobRepository repository) {
    GlobList realAccountForAccount = repository.findLinkedTo(account, RealAccount.ACCOUNT);
    for (Glob realAccount : realAccountForAccount) {
      if (realAccount.get(RealAccount.SYNCHRO) != null){
        return true;
      }
    }
    return false;
  }

  public static List<BankAccountGroup> getSynchroAccountGroups(GlobRepository repository) {
    GlobMatcher filter = isNotNull(RealAccount.SYNCHRO);
    GlobList realAccounts = repository.getAll(RealAccount.TYPE, filter);
    Map<Integer, Glob> synchroByAccount = new HashMap<Integer, Glob>();
    for (Glob account : realAccounts) {
      Glob synchro = repository.findLinkTarget(account, RealAccount.SYNCHRO);
      if (synchro != null) {
        synchroByAccount.put(account.get(RealAccount.ACCOUNT), synchro);
      }
    }
    GlobList accounts = realAccounts.getTargets(RealAccount.ACCOUNT, repository);
    accounts.filterSelf(AccountMatchers.accountsNotClosedAsOf(CurrentMonth.getAsDate(repository)), repository);
    accounts.sortSelf(new GlobFieldsComparator(Account.ACCOUNT_TYPE, true,
                                               Account.POSITION_DATE, true,
                                               Account.NAME, true));
    List<BankAccountGroup> result = new ArrayList<BankAccountGroup>();
    Map<Glob, BankAccountGroup> groups = new HashMap<Glob, BankAccountGroup>();
    for (Glob account : accounts) {
      Glob bank = null;
      if (synchroByAccount.containsKey(account.get(Account.ID))) {
        bank = repository.findLinkTarget(synchroByAccount.get(account.get(Account.ID)), Synchro.BANK);
      }
      if (bank == null) {
        bank = repository.findLinkTarget(account, Account.BANK);
      }
      BankAccountGroup group = groups.get(bank);
      if (group == null) {
        group = new BankAccountGroup(bank);
        result.add(group);
        groups.put(bank, group);
      }
      group.add(account);
    }

    return result;
  }
}
