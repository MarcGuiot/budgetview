package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.utils.PicsouMatchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;

import java.util.*;

public class PositionTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {

    if (changeSet.containsCreationsOrDeletions(Transaction.TYPE) ||
        changeSet.containsUpdates(Transaction.AMOUNT) || changeSet.containsChanges(Account.TYPE)) {
      GlobList account = repository.getAll(Account.TYPE);
      updateTransactionPosition(repository, account);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  private void updateTransactionPosition(GlobRepository repository, GlobList updatedAccount) {
    TransactionComparator comparator = TransactionComparator.ASCENDING_BANK;
    GlobMatcher globMatcher = GlobMatchers.ALL;
    SortedSet<Glob> trs = repository.getSorted(Transaction.TYPE, comparator, globMatcher);

    Glob[] transactions = trs.toArray(new Glob[trs.size()]);
    for (Integer accountId : Account.SUMMARY_ACCOUNT_IDS) {
      updatedAccount.remove(repository.get(Key.create(Account.TYPE, accountId)));
    }
    boolean mainPositionComputed = true;
    boolean savingsPositionComputed = true;
    for (Glob account : updatedAccount) {
      if (account.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
        mainPositionComputed &= computeAccountPosition(repository, comparator, transactions, account);
      }
      else {
        savingsPositionComputed &= computeAccountPosition(repository, comparator, transactions, account);
      }
    }

    if (mainPositionComputed) {
      GlobList accounts = repository.getAll(Account.TYPE);
      GlobList tmp = new GlobList();
      for (Glob account : accounts) {
        if (account.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())
            && Account.MAIN_SUMMARY_ACCOUNT_ID != account.get(Account.ID)
            && Account.ALL_SUMMARY_ACCOUNT_ID != account.get(Account.ID)) {
          tmp.add(account);
        }
      }
      computeTotalPosition(repository, transactions,
                           new SameAccountChecker(AccountType.MAIN.getId(), repository), tmp);
    }
    if (savingsPositionComputed) {
      GlobList accounts = repository.getAll(Account.TYPE);
      GlobList tmp = new GlobList();
      for (Glob account : accounts) {
        if (account.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())
            && Account.SAVINGS_SUMMARY_ACCOUNT_ID != account.get(Account.ID)
            && Account.ALL_SUMMARY_ACCOUNT_ID != account.get(Account.ID)) {
          tmp.add(account);
        }
      }
      computeTotalPosition(repository, transactions,
                           new SameAccountChecker(AccountType.SAVINGS.getId(), repository), tmp);
    }
  }

  private boolean computeAccountPosition(GlobRepository repository, TransactionComparator comparator,
                                         Glob[] transactions, Glob account) {
    Glob firstTransaction = null;
    Integer transactionId = account.get(Account.TRANSACTION_ID);
    int pivot;
    Integer lastUpdateTransactionId = transactionId;
    double positionBefore = 0;
    double positionAfter;
    if (transactionId == null) {
      pivot = transactions.length - 1;
      Double position = account.get(Account.POSITION);
      if (position == null) {
        Log.write("Missing balance for account " + account.get(Account.NAME));
        return false;
      }
      Date positionDate = account.get(Account.POSITION_DATE);
      positionAfter = position;
      for (; pivot >= 0; pivot--) {
        Glob transaction = transactions[pivot];
        Integer transactionAccount = transaction.get(Transaction.ACCOUNT);
        if (checkSameAccount(account, transactionAccount) && !transaction.get(Transaction.PLANNED)
            && (positionDate == null || !Month.toDate(transaction.get(Transaction.BANK_MONTH),
                                                      transaction.get(Transaction.BANK_DAY)).after(positionDate))) {
          positionBefore = positionAfter - transaction.get(Transaction.AMOUNT);
          repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, positionAfter);
          lastUpdateTransactionId = transaction.get(Transaction.ID);
          break;
        }
      }
    }
    else {
      Glob current = repository.get(Key.create(Transaction.TYPE, transactionId));
      pivot = Arrays.binarySearch(transactions, current, comparator);
      positionAfter = current.get(Transaction.ACCOUNT_POSITION);
      positionBefore = positionAfter - current.get(Transaction.AMOUNT);
    }

    for (int i = pivot - 1; i >= 0; i--) {
      Glob transaction = transactions[i];
      Integer transactionAccount = transaction.get(Transaction.ACCOUNT);
      if (checkSameAccount(account, transactionAccount)) {
        repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, positionBefore);
        positionBefore = positionBefore - transaction.get(Transaction.AMOUNT);
        firstTransaction = transaction;
      }
    }
    int month = TimeService.getCurrentMonth();
    int day = TimeService.getCurrentDay();
    for (int i = pivot + 1; i < transactions.length; i++) {
      Glob transaction = transactions[i];
      Integer transactionAccount = transaction.get(Transaction.ACCOUNT);
      if (checkSameAccount(account, transactionAccount)) {
        positionAfter = positionAfter + transaction.get(Transaction.AMOUNT);
        repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, positionAfter);
        if (!transaction.get(Transaction.PLANNED) &&
            Transaction.isTransactionBeforeOrEqual(transaction, month, day)) {
          lastUpdateTransactionId = transaction.get(Transaction.ID);
        }
        if (firstTransaction == null) {
          firstTransaction = transaction;
        }
      }
    }
    repository.update(account.getKey(), Account.TRANSACTION_ID, lastUpdateTransactionId);
    if (lastUpdateTransactionId != null) {
      Glob lastTransaction = repository.get(Key.create(Transaction.TYPE, lastUpdateTransactionId));
      repository.update(account.getKey(),
                        FieldValue.value(Account.POSITION_DATE,
                                         Month.toDate(lastTransaction.get(Transaction.BANK_MONTH),
                                                      lastTransaction.get(Transaction.BANK_DAY))),
                        FieldValue.value(Account.POSITION, lastTransaction.get(Transaction.ACCOUNT_POSITION))
      );
    }
    if (firstTransaction != null) {
      repository.update(account.getKey(),
                        FieldValue.value(Account.FIRST_POSITION,
                                         firstTransaction.get(Transaction.ACCOUNT_POSITION) - firstTransaction.get(Transaction.AMOUNT)));
    }
    else {
      repository.update(account.getKey(),
                        FieldValue.value(Account.FIRST_POSITION, account.get(Account.POSITION)));
    }
    return true;
  }

  private boolean checkSameAccount(Glob account, Integer transactionAccount) {
    return transactionAccount != null && transactionAccount.equals(account.get(Account.ID));
  }

  private void computeTotalPosition(GlobRepository repository, Glob[] transactions,
                                    SameAccountChecker sameCheckerAccount, final GlobList accounts) {

    Map<Integer, Double> positions = new HashMap<Integer, Double>();

    AccountManagement accountManagement = new AccountManagement(repository, accounts, sameCheckerAccount);
    Glob firstMonth = repository.getSorted(Month.TYPE, new GlobFieldComparator(Month.ID), GlobMatchers.ALL).first();
    if (firstMonth == null) {
      return;
    }

    PicsouMatchers.AccountDateMatcher matcher =
      new PicsouMatchers.AccountDateMatcher(new GlobList(firstMonth));
    for (Glob account : accounts) {
      if (matcher.matches(account, repository) &&
          sameCheckerAccount.isSame(account.get(Account.ID))) {
        Double value = account.get(Account.FIRST_POSITION);
        positions.put(account.get(Account.ID), value);
      }
    }


    Double realPosition = null;
    Date positionDate = null;
    int month = TimeService.getCurrentMonth();
    int currentDay = TimeService.getCurrentDay();
    int index = 0;
    for (; index < transactions.length; index++) {
      Glob transaction = transactions[index];
      if (!sameCheckerAccount.isSame(transaction.get(Transaction.ACCOUNT))) {
        continue;
      }
      if (Transaction.isTransactionBeforeOrEqual(transaction, month, currentDay) && !transaction.get(Transaction.PLANNED)) {

        accountManagement.updateOpenPosition(transaction, index, transactions, positions);
        accountManagement.updateClosePosition(transaction, positions);

        Integer accountId = transaction.get(Transaction.ACCOUNT);
        if (accountId != null) {
          positions.put(accountId, transaction.get(Transaction.ACCOUNT_POSITION));
        }


        double position = 0.;
        for (Double accountPosition : positions.values()) {
          if (accountPosition != null) {
            position += accountPosition;
          }
        }

        realPosition = position;
        positionDate = Month.toDate(transaction.get(Transaction.BANK_MONTH),
                                    transaction.get(Transaction.BANK_DAY));
        repository.update(transaction.getKey(), Transaction.SUMMARY_POSITION, position);
      }
      else {
        break;
      }
    }
    double position = 0.;
    for (; index < transactions.length; index++) {
      Glob transaction = transactions[index];
      if (!sameCheckerAccount.isSame(transaction.get(Transaction.ACCOUNT))) {
        continue;
      }
      accountManagement.updateOpenPosition(transaction, index, transactions, positions);
      accountManagement.updateClosePosition(transaction, positions);

      position += transaction.get(Transaction.AMOUNT);
      double totalPosition = 0;
      for (Double accountPosition : positions.values()) {
        if (accountPosition != null) {
          totalPosition += accountPosition;
        }
      }
      totalPosition += position;
      repository.update(transaction.getKey(), Transaction.SUMMARY_POSITION, totalPosition);
    }

    if (realPosition == null) {
      realPosition = 0.;
      for (Double accountPosition : positions.values()) {
        realPosition += (accountPosition != null ? accountPosition : 0.);
      }
      positionDate = TimeService.getToday();
    }
    repository.update(sameCheckerAccount.getSummary(),
                      FieldValue.value(Account.POSITION, realPosition),
                      FieldValue.value(Account.POSITION_DATE, positionDate));
  }

  static class AccountManagement {
    private int lastCloseIndex = 0;
    private int lastOpenIndex = 0;
    private int[] closeMonth;
    private int[] closeDay;
    private int[] closeId;
    private int[] openMonth;
    private int[] openDay;
    private int[] openId;
    private GlobRepository repository;

    AccountManagement(GlobRepository repository, GlobList accounts, SameAccountChecker sameCheckerAccount) {
      this.repository = repository;
      GlobList closedAccounts = new GlobList();
      GlobList openAccounts = new GlobList();
      for (Glob account : accounts) {
        if (account.get(Account.CLOSED_DATE) != null && sameCheckerAccount.isSame(account.get(Account.ID))) {
          closedAccounts.add(account);
        }
        if (account.get(Account.OPEN_DATE) != null && sameCheckerAccount.isSame(account.get(Account.ID))) {
          openAccounts.add(account);
        }
      }
      closedAccounts.sort(Account.CLOSED_DATE);
      closeMonth = new int[closedAccounts.size()];
      closeDay = new int[closedAccounts.size()];
      closeId = new int[closedAccounts.size()];
      int i = 0;
      for (Glob account : closedAccounts) {
        closeMonth[i] = Month.getMonthId(account.get(Account.CLOSED_DATE));
        closeDay[i] = Month.getDay(account.get(Account.CLOSED_DATE));
        closeId[i] = account.get(Account.ID);
        i++;
      }

      openAccounts.sort(Account.OPEN_DATE);
      openMonth = new int[openAccounts.size()];
      openDay = new int[openAccounts.size()];
      openId = new int[openAccounts.size()];
      int j = 0;
      for (Glob account : openAccounts) {
        openMonth[j] = Month.getMonthId(account.get(Account.OPEN_DATE));
        openDay[j] = Month.getDay(account.get(Account.OPEN_DATE));
        openId[j] = account.get(Account.ID);
        j++;
      }
    }

    private void updateClosePosition(Glob transaction, Map<Integer, Double> positions) {
      while (lastCloseIndex < closeMonth.length &&
             (closeMonth[lastCloseIndex] < transaction.get(Transaction.BANK_MONTH) ||
              (closeMonth[lastCloseIndex] == transaction.get(Transaction.BANK_MONTH) &&
               closeDay[lastCloseIndex] < transaction.get(Transaction.BANK_DAY)))) {
        positions.remove(closeId[lastCloseIndex]);
        lastCloseIndex++;
      }
    }

    private void updateOpenPosition(Glob transaction, int index, Glob[] transactions, Map<Integer, Double> positions) {
      while (lastOpenIndex < openMonth.length &&
             (openMonth[lastOpenIndex] < transaction.get(Transaction.BANK_MONTH) ||
              (openMonth[lastOpenIndex] == transaction.get(Transaction.BANK_MONTH) &&
               openDay[lastOpenIndex] <= transaction.get(Transaction.BANK_DAY)))) {
        int tmpAccountId = openId[lastOpenIndex];
        Glob account = repository.find(Key.create(Account.TYPE, tmpAccountId));
        positions.put(tmpAccountId, account.get(Account.POSITION));
        // on cherche si il y a une operation dans le futur pour ce comptes.
        for (int k = index; k < transactions.length; k++) {
          if (transactions[k].get(Transaction.ACCOUNT) == tmpAccountId) {
            positions.put(tmpAccountId, transactions[k].get(Transaction.ACCOUNT_POSITION) - transactions[k].get(Transaction.AMOUNT));
            break;
          }
        }
        lastOpenIndex++;
      }
    }


  }
}
