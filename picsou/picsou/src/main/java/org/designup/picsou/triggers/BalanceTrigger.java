package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import java.util.*;

public class BalanceTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    GlobList account = null;
    boolean updatePlannedOnly = false;

    if (changeSet.containsCreationsOrDeletions(Transaction.TYPE) ||
        changeSet.containsUpdates(Transaction.AMOUNT)) {
      Set<Key> created = changeSet.getCreated(Transaction.TYPE);
      for (Key key : created) {
        if (!repository.get(key).get(Transaction.PLANNED)) {
          account = repository.getAll(Account.TYPE);
          break;
        }
      }
      if (account == null) {
        Set<Key> deleted = changeSet.getDeleted(Transaction.TYPE);
        for (Key key : deleted) {
          if (!changeSet.getPreviousValue(key).get(Transaction.PLANNED)) {
            account = repository.getAll(Account.TYPE);
            break;
          }
        }
      }
      if (account == null) {
        Set<Key> updated = changeSet.getUpdated(Transaction.AMOUNT);
        for (Key key : updated) {
          if (!repository.get(key).get(Transaction.PLANNED)) {
            account = repository.getAll(Account.TYPE);
            break;
          }
        }
      }

      if (account == null) {
        updatePlannedOnly = true;
        account = GlobList.EMPTY;
      }
    }
    else if (changeSet.containsChanges(Account.TYPE)) {
      Set<Key> keySet = changeSet.getUpdated(Account.TYPE);
      keySet.addAll(changeSet.getCreated(Account.TYPE));
      account = new GlobList();
      for (Key key : keySet) {
        account.add(repository.get(key));
      }
    }
    if (account != null) {
      updateTransactionBalance(repository, account, updatePlannedOnly);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }


  private void updateTransactionBalance(GlobRepository repository, GlobList updatedAccount,
                                        boolean updatePlannedOnly) {
    TransactionComparator comparator = TransactionComparator.ASCENDING_BANK;
    GlobMatcher globMatcher = GlobMatchers.ALL;
    if (updatePlannedOnly) {
      globMatcher = GlobMatchers.fieldEquals(Transaction.PLANNED, true);
    }
    SortedSet<Glob> trs = repository.getSorted(Transaction.TYPE, comparator, globMatcher);

    Glob[] transactions = trs.toArray(new Glob[trs.size()]);
    boolean balanceComputed = true;
    for (Integer accountId : Account.SUMMARY_ACCOUNT) {
      updatedAccount.remove(repository.get(Key.create(Account.TYPE, accountId)));
    }
    for (Glob account : updatedAccount) {
      balanceComputed &= computeAccountBalance(repository, comparator, transactions, account);
    }

    if (balanceComputed || updatePlannedOnly) {
      computeTotalBalance(repository, transactions,
                          new SameCheckerAccount(AccountType.MAIN.getId(), repository), updatePlannedOnly);
      computeTotalBalance(repository, transactions,
                          new SameCheckerAccount(AccountType.SAVINGS.getId(), repository), updatePlannedOnly);
    }
  }

  private boolean computeAccountBalance(GlobRepository repository, TransactionComparator comparator,
                                        Glob[] transactions, Glob account) {
    SameCheckerAccount sameCheckerAccount = new SameCheckerAccount(account, repository);
    Integer transactionId = account.get(Account.TRANSACTION_ID);
    int pivot;
    Integer lastUpdateTransactionId = transactionId;
    double balanceLeft = 0;
    double balanceRigth;
    Integer accountId = account.get(Account.ID);
    if (transactionId == null) {
      pivot = transactions.length - 1;
      Double balance = account.get(Account.BALANCE);
      if (balance == null) {
        return false;
      }
      balanceRigth = balance;
      for (; pivot >= 0; pivot--) {
        Glob transaction = transactions[pivot];
        Integer transactionAccount = transaction.get(Transaction.ACCOUNT);
        if (transactionAccount != null && sameCheckerAccount.isSame(transactionAccount)) {
          balanceLeft = balanceRigth - transaction.get(Transaction.AMOUNT);
          repository.update(transaction.getKey(), Transaction.ACCOUNT_BALANCE, balanceRigth);
          if (!transaction.get(Transaction.PLANNED)) {
            lastUpdateTransactionId = transaction.get(Transaction.ID);
          }
          break;
        }
      }
    }
    else {
      Glob current = repository.get(Key.create(Transaction.TYPE, transactionId));
      pivot = Arrays.binarySearch(transactions, current, comparator);
      balanceRigth = current.get(Transaction.ACCOUNT_BALANCE);
      balanceLeft = balanceRigth - current.get(Transaction.AMOUNT);
    }

    for (int i = pivot - 1; i >= 0; i--) {
      Glob transaction = transactions[i];
      Integer transactionAccount = transaction.get(Transaction.ACCOUNT);
      if (transactionAccount != null && sameCheckerAccount.isSame(accountId)) {
        repository.update(transaction.getKey(), Transaction.ACCOUNT_BALANCE, balanceLeft);
        balanceLeft = balanceLeft - transaction.get(Transaction.AMOUNT);
      }
    }
    for (int i = pivot + 1; i < transactions.length; i++) {
      Glob transaction = transactions[i];
      Integer transactionAccount = transaction.get(Transaction.ACCOUNT);
      if (transactionAccount != null && sameCheckerAccount.isSame(accountId)) {
        balanceRigth = balanceRigth + transaction.get(Transaction.AMOUNT);
        repository.update(transaction.getKey(), Transaction.ACCOUNT_BALANCE, balanceRigth);
        if (!transaction.get(Transaction.PLANNED)) {
          lastUpdateTransactionId = transaction.get(Transaction.ID);
        }
      }
    }
    repository.update(account.getKey(), Account.TRANSACTION_ID, lastUpdateTransactionId);
    if (lastUpdateTransactionId != null) {
      Glob lastTransaction = repository.get(Key.create(Transaction.TYPE, lastUpdateTransactionId));
      repository.update(account.getKey(), Account.BALANCE_DATE,
                        Month.toDate(lastTransaction.get(Transaction.BANK_MONTH),
                                     lastTransaction.get(Transaction.BANK_DAY)));
    }
    return true;
  }

  private void computeTotalBalance(GlobRepository repository, Glob[] transactions,
                                   SameCheckerAccount sameCheckerAccount, boolean updatePlannedOnly) {
    GlobList accounts = repository.getAll(Account.TYPE);
    Map<Integer, Double> balances = new HashMap<Integer, Double>();
    GlobList closedAccount = new GlobList();
    for (Glob account : accounts) {
      if (account.get(Account.CLOSED_DATE) != null && sameCheckerAccount.isSame(account.get(Account.ACCOUNT_TYPE))) {
        closedAccount.add(account);
      }
    }
    closedAccount.sort(Account.CLOSED_DATE);
    int closeMonth[] = new int[closedAccount.size()];
    int closeDay[] = new int[closedAccount.size()];
    int closeId[] = new int[closedAccount.size()];
    int i = 0;
    for (Glob account : closedAccount) {
      closeMonth[i] = Month.getMonthId(account.get(Account.CLOSED_DATE));
      closeDay[i] = Month.getDay(account.get(Account.CLOSED_DATE));
      closeId[i] = account.get(Account.ID);
      i++;
    }
    double balance = 0;
    if (updatePlannedOnly) {
      Double summaryBalance = repository.get(sameCheckerAccount.getSummary())
        .get(Account.BALANCE);
      if (summaryBalance == null) {
        return;
      }
      balance = summaryBalance;
    }

    int lastCloseIndex = 0;
    Double realBalance = null;
    Date balanceDate = null;
    for (Glob transaction : transactions) {
      if (sameCheckerAccount.isSame(transaction.get(Transaction.ACCOUNT))) {
        continue;
      }
      if (!transaction.get(Transaction.PLANNED)) {
        Integer accountId = transaction.get(Transaction.ACCOUNT);
        if (accountId != null) {
          balances.put(accountId, transaction.get(Transaction.ACCOUNT_BALANCE));
        }
        while (lastCloseIndex < closeMonth.length &&
               closeMonth[lastCloseIndex] <= transaction.get(Transaction.BANK_MONTH) &&
               closeDay[lastCloseIndex] < transaction.get(Transaction.BANK_DAY)) {
          balances.remove(closeId[lastCloseIndex]);
          lastCloseIndex++;
        }
        balance = 0;
        for (Double accountBalance : balances.values()) {
          balance += accountBalance;
        }
        realBalance = balance;
        balanceDate = Month.toDate(transaction.get(Transaction.BANK_MONTH),
                                   transaction.get(Transaction.BANK_DAY));
        repository.update(transaction.getKey(), Transaction.BALANCE, balance);
      }
      else {
        balance += transaction.get(Transaction.AMOUNT);
        repository.update(transaction.getKey(), Transaction.BALANCE, balance);
      }
    }
    if (!updatePlannedOnly) {
      repository.update(sameCheckerAccount.getSummary(),
                        FieldValue.value(Account.BALANCE, realBalance),
                        FieldValue.value(Account.BALANCE_DATE, balanceDate));
    }
  }

  static class SameCheckerAccount {
    Set<Integer> mains = new HashSet<Integer>();
    private Key summaryAccountId;

    public SameCheckerAccount(Glob account, GlobRepository repository) {
      update(repository, account.get(Account.ACCOUNT_TYPE));
    }

    public SameCheckerAccount(Integer accountType, GlobRepository repository) {
      update(repository, accountType);
    }

    private void update(GlobRepository repository, Integer accountType) {
      if (accountType.equals(AccountType.MAIN.getId())) {
        summaryAccountId = Account.MAIN_SUMMARY_KEY;
      }
      else {
        summaryAccountId = Account.SAVINGS_SUMMARY_KEY;
      }
      GlobList accounts = repository.getAll(Account.TYPE);
      for (Glob tmp : accounts) {
        if (tmp.get(Account.ACCOUNT_TYPE).equals(accountType)) {
          mains.add(tmp.get(Account.ID));
        }
      }
    }

    public static SameCheckerAccount getSameAsSavings(GlobRepository repository) {
      return new SameCheckerAccount(AccountType.SAVINGS.getId(), repository);
    }

    public static SameCheckerAccount getSameAsMain(GlobRepository repository) {
      return new SameCheckerAccount(AccountType.MAIN.getId(), repository);
    }

    public boolean isSame(Integer accountId) {
      return mains.contains(accountId);
    }

    public Key getSummary() {
      return summaryAccountId;
    }
  }
}
