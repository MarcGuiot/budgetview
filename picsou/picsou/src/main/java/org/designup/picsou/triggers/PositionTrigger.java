package org.designup.picsou.triggers;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;

import java.util.*;

public class PositionTrigger implements ChangeSetListener {

  // donnée menmbre utilisé temporairement
  private double positionBefore;
  private double positionAfter;
  private Integer lastUpdateTransactionId;
  private int pivot;

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {

    if (changeSet.containsCreationsOrDeletions(Transaction.TYPE) ||
        changeSet.containsUpdates(Transaction.AMOUNT) || changeSet.containsChanges(Account.TYPE)
        || changeSet.containsUpdates(Transaction.DAY)) {
      GlobList account = repository.getAll(Account.TYPE);
      updateTransactionPosition(repository, account);
    }
    else if (changeSet.containsChanges(Transaction.TYPE)) {
      Set<Key> seriesKey = changeSet.getUpdated(Transaction.SERIES);
      for (Key transaction : seriesKey) {
        Glob glob = repository.findLinkTarget(repository.get(transaction), Transaction.SERIES);
        if (glob != null) {
          if (glob.get(Series.BUDGET_AREA).equals(BudgetArea.OTHER.getId()) &&
              glob.get(Series.FROM_ACCOUNT) != null) {
            GlobList account = repository.getAll(Account.TYPE);
            updateTransactionPosition(repository, account);
            return;
          }
        }
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
//    GlobList account = repository.getAll(Account.TYPE);
//    if (account.isEmpty()){
//      return;
//    }
//    updateTransactionPosition(repository, account);
  }

  private void updateTransactionPosition(GlobRepository repository, GlobList updatedAccount) {

    GlobList deferredSeries = repository.getAll(Series.TYPE,
                                                GlobMatchers.and(GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                                                                 GlobMatchers.isNotNull(Series.FROM_ACCOUNT)));
    Set<Integer> deferredSeriesId =
      deferredSeries
        .getValueSet(Series.ID);

    Map<Integer, Integer> deferredAccounts = new HashMap<Integer, Integer>();
    for (Glob series : deferredSeries) {
      deferredAccounts.put(series.get(Series.FROM_ACCOUNT), series.get(Series.ID));
    }

    TransactionComparator comparator = TransactionComparator.ASCENDING_ACCOUNT;
    SortedSet<Glob> trs = repository.getSorted(Transaction.TYPE, comparator, GlobMatchers.ALL);

    Glob[] transactions = trs.toArray(new Glob[trs.size()]);
    for (Integer accountId : Account.SUMMARY_ACCOUNT_IDS) {
      updatedAccount.remove(repository.get(Key.create(Account.TYPE, accountId)));
    }
    for (Glob account : updatedAccount) {
      if (AccountType.MAIN.getId().equals(account.get(Account.ACCOUNT_TYPE))) {
        if (AccountCardType.DEFERRED.getId().equals(account.get(Account.CARD_TYPE))) {
          computeDefferedPosition(repository, comparator, transactions, account);
        }
        else {
          computeAccountPosition(repository, comparator, transactions, account, deferredSeriesId);
        }
      }
      else {
        computeAccountPosition(repository, comparator, transactions, account, deferredSeriesId);
      }
    }

//    if (mainPositionComputed)
    {
      GlobList accounts = repository.getAll(Account.TYPE);
      GlobList tmp = new GlobList();
      for (Glob account : accounts) {
        if (AccountType.MAIN.getId().equals(account.get(Account.ACCOUNT_TYPE))
            && (Account.MAIN_SUMMARY_ACCOUNT_ID != account.get(Account.ID))
            && (Account.ALL_SUMMARY_ACCOUNT_ID != account.get(Account.ID))) {
          tmp.add(account);
        }
      }
      computeTotalPosition(repository, transactions,
                           new SameAccountChecker(AccountType.MAIN.getId(), repository), tmp, deferredSeriesId, deferredAccounts);
    }
//    if (savingsPositionComputed)
    {
      GlobList accounts = repository.getAll(Account.TYPE);
      GlobList tmp = new GlobList();
      for (Glob account : accounts) {
        if (AccountType.SAVINGS.getId().equals(account.get(Account.ACCOUNT_TYPE))
            && Account.SAVINGS_SUMMARY_ACCOUNT_ID != account.get(Account.ID)
            && Account.ALL_SUMMARY_ACCOUNT_ID != account.get(Account.ID)) {
          tmp.add(account);
        }
      }
      computeTotalPosition(repository, transactions,
                           new SameAccountChecker(AccountType.SAVINGS.getId(), repository), tmp,
                           new HashSet<Integer>(), deferredAccounts);
    }
  }

  private boolean computeDefferedPosition(GlobRepository repository, TransactionComparator comparator,
                                          Glob[] transactions, Glob account) {

    Integer transactionId = account.get(Account.TRANSACTION_ID);
    lastUpdateTransactionId = transactionId;
    positionBefore = 0;
    if (initPivot(repository, comparator, transactions, account, transactionId)) {
      return false;
    }

    Set<Integer> monthDone = new HashSet<Integer>();
    if (pivot != -1) {
      // on update que le mois en cours.
      Glob currentTransaction = transactions[pivot];
      int currentPositionMonthId = currentTransaction.get(Transaction.POSITION_MONTH);
      monthDone.add(currentPositionMonthId);
      for (int i = pivot - 1; i >= 0; i--) {
        Glob transaction = transactions[i];
        Integer transactionAccount = transaction.get(Transaction.ACCOUNT);
        if (checkSameAccount(account, transactionAccount)) {
          if (transaction.get(Transaction.POSITION_MONTH) != currentPositionMonthId) {
            break;
          }
          repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, positionBefore);
          positionBefore = positionBefore - transaction.get(Transaction.AMOUNT);
        }
      }
      int month = TimeService.getCurrentMonth();
      int day = TimeService.getCurrentDay();
      for (int i = pivot + 1; i < transactions.length; i++) {
        Glob transaction = transactions[i];
        Integer transactionAccount = transaction.get(Transaction.ACCOUNT);
        if (checkSameAccount(account, transactionAccount)) {
          if (transaction.get(Transaction.POSITION_MONTH) != currentPositionMonthId) {
            break;
          }
          positionAfter = positionAfter + transaction.get(Transaction.AMOUNT);
          repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, positionAfter);
          if (!transaction.isTrue(Transaction.PLANNED) &&
              Transaction.isPositionTransactionBeforeOrEqual(transaction, month, day)) {
            lastUpdateTransactionId = transaction.get(Transaction.ID);
          }
        }
      }
    }


    Glob series = repository.getAll(Series.TYPE,
                                    GlobMatchers.and(
                                      GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                                      GlobMatchers.fieldEquals(Series.FROM_ACCOUNT, account.get(Account.ID))))
      .getFirst();
    ReadOnlyGlobRepository.MultiFieldIndexed debitTransaction =
      repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID));

    Glob firstTransaction = null;
    int i;
    int transactionMonthId;
    for (i = 0; i < transactions.length; i++) {
      Glob transaction = transactions[i];
      Integer transactionAccount = transaction.get(Transaction.ACCOUNT);
      if (!checkSameAccount(account, transactionAccount)) {
        continue;
      }
      if (firstTransaction == null) {
        firstTransaction = transaction;
      }

      if (monthDone.contains(transaction.get(Transaction.POSITION_MONTH))) {
        continue;
      }
      transactionMonthId = transaction.get(Transaction.POSITION_MONTH);
      Glob debit = debitTransaction.findByIndex(Transaction.POSITION_MONTH, transactionMonthId)
        .getGlobs().getFirst();
      if (debit != null) {
        int j;
        for (j = i; j < transactions.length; j++) {
          Glob glob = transactions[j];
          if (checkSameAccount(account, glob.get(Transaction.ACCOUNT))
              && glob.get(Transaction.POSITION_MONTH) != transactionMonthId) {
            break;
          }
        }
        double amountAfter = debit.get(Transaction.AMOUNT);
        for (int k = j - 1; k >= i; k--) {
          if (checkSameAccount(account, transactions[k].get(Transaction.ACCOUNT))) {
            repository.update(transactions[k].getKey(), Transaction.ACCOUNT_POSITION, amountAfter);
            amountAfter -= transactions[k].get(Transaction.AMOUNT);
          }
        }
        i = j - 1;
        monthDone.add(transactionMonthId);
      }
    }

    double amount = 0;
    transactionMonthId = -1;
    for (Glob transaction : transactions) {
      if (monthDone.contains(transaction.get(Transaction.POSITION_MONTH))) {
        continue;
      }
      if (checkSameAccount(account, transaction.get(Transaction.ACCOUNT))) {
        if (transaction.get(Transaction.POSITION_MONTH) != transactionMonthId) {
          amount = 0;
          transactionMonthId = transaction.get(Transaction.POSITION_MONTH);
        }
        amount += transaction.get(Transaction.AMOUNT);
        repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, amount);
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

  private boolean computeAccountPosition(GlobRepository repository, TransactionComparator comparator,
                                         Glob[] transactions, Glob account, Set<Integer> deferredSeries) {
    Glob firstTransaction = null;
    Integer transactionId = account.get(Account.TRANSACTION_ID);
    lastUpdateTransactionId = transactionId;
    positionBefore = 0;
    if (initPivot(repository, comparator, transactions, account, transactionId)) {
      repository.update(account.getKey(),
                        FieldValue.value(Account.FIRST_POSITION, account.get(Account.POSITION)));
      return false;
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
        if (!transaction.isTrue(Transaction.PLANNED) &&
            Transaction.isPositionTransactionBeforeOrEqual(transaction, month, day)) {
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


  // En qif on repart de la derniere operation
  // En ofx on part du nouveau solde recu donc de la derniere operations
  // On utilise la date de bank pour trouver cette derniere operation
  // Au chargement on connait la derniere operation mais on ne sais pas si elle n'a
  // pas deja ete importé. L'ofx import n'update la balance que si la date est superieur a la
  // derniere date (bizarre : on est donc sur que la derniere operations n'a pas ete deja importé)
  // on utilise la date de bank et non de position :
  // pour un comptes a debit differe la date de position est dans le futur
  private boolean initPivot(GlobRepository repository, TransactionComparator comparator, Glob[] transactions, Glob account, Integer transactionId) {
    if (transactionId == null) {
      pivot = transactions.length - 1;
      Double position = account.get(Account.POSITION);
      if (position == null) {
        Log.write("Missing balance for account " + account.get(Account.NAME));
        return true;
      }
      Date positionDate = account.get(Account.POSITION_DATE);
      int positionMonthId = -1;
      int positionDay = -1;
      if (positionDate != null) {
        positionMonthId = Month.getMonthId(positionDate);
        positionDay = Month.getDay(positionDate);
      }
      positionAfter = position;
      for (; pivot >= 0; pivot--) {
        Glob transaction = transactions[pivot];
        Integer transactionAccount = transaction.get(Transaction.ACCOUNT);
        if (checkSameAccount(account, transactionAccount) && !transaction.isTrue(Transaction.PLANNED)
            && (positionDate == null || (transaction.get(Transaction.BANK_MONTH) < positionMonthId ||
                                         (transaction.get(Transaction.BANK_MONTH) == positionMonthId &&
                                          transaction.get(Transaction.BANK_DAY) <= positionDay)))) {
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
      if (pivot < 0) {
        Log.write("Bug : transaction not found but is present do linear search");
        for (pivot = 0; pivot < transactions.length; pivot++) {
          Glob transaction = transactions[pivot];
          if (transaction == current) {
            Log.write("Transaction found continuing " + GlobPrinter.toString(current));
            break;
          }
        }
        if (pivot == transactions.length) {
          Log.write("transaction not found : " + transactionId + " : " + GlobPrinter.toString(current));
          return true;
        }
      }
      positionAfter = current.get(Transaction.ACCOUNT_POSITION);
      positionBefore = positionAfter - current.get(Transaction.AMOUNT);
    }
    return false;
  }

  private boolean checkSameAccount(Glob account, Integer transactionAccount) {
    return transactionAccount != null && transactionAccount.equals(account.get(Account.ID));
  }

  private void computeTotalPosition(GlobRepository repository, Glob[] transactions,
                                    SameAccountChecker sameCheckerAccount, final GlobList accounts,
                                    Set<Integer> deferredSeriesId, Map<Integer, Integer> deferredAccounts) {

    Map<Integer, Double> positions = new HashMap<Integer, Double>();

    AccountManagement accountManagement = new AccountManagement(repository, accounts, sameCheckerAccount);
    Glob firstMonth = repository.getSorted(Month.TYPE, new GlobFieldComparator(Month.ID), GlobMatchers.ALL).first();
    if (firstMonth == null) {
      return;
    }

    Matchers.AccountDateMatcher matcher =
      new Matchers.AccountDateMatcher(new GlobList(firstMonth));
    for (Glob account : accounts) {
      if (matcher.matches(account, repository) &&
          sameCheckerAccount.isSame(account.get(Account.ID))
          && !account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
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
      if (Transaction.isPositionTransactionBeforeOrEqual(transaction, month, currentDay) && !transaction.isTrue(Transaction.PLANNED)) {

        accountManagement.updateOpenPosition(transaction, index, transactions, positions);
        accountManagement.updateClosePosition(transaction, positions);

        Integer accountId = transaction.get(Transaction.ACCOUNT);
        if (accountId != null) {
          if (!deferredAccounts.containsKey(accountId)) {
            positions.put(accountId, transaction.get(Transaction.ACCOUNT_POSITION));
          }
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
             (closeMonth[lastCloseIndex] < transaction.get(Transaction.POSITION_MONTH) ||
              (closeMonth[lastCloseIndex] == transaction.get(Transaction.POSITION_MONTH) &&
               closeDay[lastCloseIndex] < transaction.get(Transaction.POSITION_DAY)))) {
        positions.remove(closeId[lastCloseIndex]);
        lastCloseIndex++;
      }
    }

    private void updateOpenPosition(Glob transaction, int index, Glob[] transactions, Map<Integer, Double> positions) {
      while (lastOpenIndex < openMonth.length &&
             (openMonth[lastOpenIndex] < transaction.get(Transaction.POSITION_MONTH) ||
              (openMonth[lastOpenIndex] == transaction.get(Transaction.POSITION_MONTH) &&
               openDay[lastOpenIndex] <= transaction.get(Transaction.POSITION_DAY)))) {
        int tmpAccountId = openId[lastOpenIndex];
        Glob account = repository.find(Key.create(Account.TYPE, tmpAccountId));
        Double value = account.get(Account.POSITION);
        if (value != null) {
          positions.put(tmpAccountId, value);
          // on cherche si il y a une operation dans le futur pour ce comptes.
          for (int k = index; k < transactions.length; k++) {
            if (transactions[k].get(Transaction.ACCOUNT) == tmpAccountId) {
              positions.put(tmpAccountId, transactions[k].get(Transaction.ACCOUNT_POSITION) - transactions[k].get(Transaction.AMOUNT));
              break;
            }
          }
        }
        lastOpenIndex++;
      }
    }
  }
}
