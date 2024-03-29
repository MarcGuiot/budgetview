package com.budgetview.triggers;

import com.budgetview.desktop.time.TimeService;
import com.budgetview.desktop.transactions.utils.TransactionMatchers;
import com.budgetview.model.*;
import com.budgetview.shared.model.AccountType;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.collections.MapOfMaps;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class PositionTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    final Set<Integer> accountIds = new HashSet<Integer>();
    final Set<Integer> deferredAccountIds = new HashSet<Integer>();
    Glob[] transactions = null;
    if (changeSet.containsChanges(Transaction.TYPE) ||
        changeSet.containsUpdates(Account.POSITION_WITH_PENDING) ||
        changeSet.containsChanges(Account.TYPE)) {

      transactions = Transaction.getAllSortedByPositionDate(repository, GlobMatchers.ALL, TransactionComparator.ASCENDING_ACCOUNT);

      Set<Integer> allDeferredAccountIds = getAllDeferredAccounts(repository);

      final Set<Integer> updatedOperations = new HashSet<Integer>();
      final Set<Integer> createdAccount = new HashSet<Integer>();
      changeSet.safeVisit(Account.TYPE, new AccountChangesetVisitor(accountIds, deferredAccountIds, createdAccount, repository));
      TransactionChangeSetVisitor transactionChangeSetVisitor =
        new TransactionChangeSetVisitor(updatedOperations, accountIds, deferredAccountIds, repository);
      changeSet.safeVisit(Transaction.TYPE, transactionChangeSetVisitor);

      Set<Key> created = changeSet.getCreated(AccountPositionMode.TYPE);
      if (created.size() == 1) {
        Glob glob = repository.get(created.iterator().next());
        boolean shouldUpdatePosition = glob.get(AccountPositionMode.UPDATE_ACCOUNT_POSITION, true);
        if (shouldUpdatePosition) {
          updateTransactionImpactAccounts(repository, changeSet, updatedOperations, accountIds, createdAccount, false,
                                          transactions, allDeferredAccountIds);
        }
        else {
          updateTransactionButNotAccountPosition(repository, accountIds, transactions, allDeferredAccountIds);
        }
      }
      else {
        boolean isImport = changeSet.containsChanges(TransactionImport.TYPE);
        updateTransactionImpactAccounts(repository, changeSet, updatedOperations, accountIds, createdAccount, isImport,
                                        transactions, allDeferredAccountIds);
      }
      if (accountIds.isEmpty() && deferredAccountIds.isEmpty()) {
        return;
      }
      // si le compte a debit differe n'a pas de compte associé, on force la position du compte
      for (Integer id : deferredAccountIds) {
        Glob deferredAccount = repository.get(KeyBuilder.newKey(Account.TYPE, id));
        if (deferredAccount.get(Account.DEFERRED_TARGET_ACCOUNT) == null) {
          computeDeferredPositionForNotAssociatedAccount(repository, transactions, deferredAccount);
        }
      }

      updateDeferredAccount(repository, deferredAccountIds, transactions);

      computeTotal(repository, transactions);
    }
    if (changeSet.containsChanges(CurrentMonth.TYPE)) {
      if (transactions == null) {
        TransactionComparator comparator = TransactionComparator.ASCENDING_ACCOUNT;
        transactions = repository.getSorted(Transaction.TYPE, comparator, GlobMatchers.ALL);
      }

      Set<Integer> allDeferredAccountIds = getAllDeferredAccounts(repository);

      Set<Integer> allAccountIds =
        repository.getAll(Account.TYPE,
                          and(not(fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId())),
                              not(contained(Account.ID, accountIds))))
          .getValueSet(Account.ID);
      updateTransactionImpactAccounts(repository, changeSet, Collections.<Integer>emptySet(), allAccountIds,
                                      Collections.<Integer>emptySet(), false, transactions, allDeferredAccountIds);

      updateDeferredAccount(repository, allDeferredAccountIds, transactions);
      computeTotal(repository, transactions);
    }
  }

  private Set<Integer> getAllDeferredAccounts(GlobRepository repository) {
    return repository.getAll(Account.TYPE,
                             fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId())).getValueSet(Account.ID);
  }

  public static void computeTotal(GlobRepository repository, final Glob[] transactions) {
    computeTotalPosition(repository, transactions);
  }

  private void updateDeferredAccount(GlobRepository repository, Set<Integer> deferredAccountIds, Glob[] allTransactions) {
    Glob month = repository.get(CurrentMonth.KEY);
    Integer monthId = month.get(CurrentMonth.LAST_TRANSACTION_MONTH);
    Integer day = month.get(CurrentMonth.LAST_TRANSACTION_DAY);
    GlobList globs = repository.findByIndex(Transaction.POSITION_MONTH_INDEX, monthId);
    for (Integer id : deferredAccountIds) {
      Glob lastTransaction = null;
      double total = 0;
      for (Glob glob : globs) {
        if (glob.get(Transaction.ORIGINAL_ACCOUNT).equals(id)) {
          total += glob.get(Transaction.AMOUNT, 0.);
          lastTransaction = glob;
        }
      }
      if (lastTransaction != null) {
        repository.update(KeyBuilder.newKey(Account.TYPE, id),
                          value(Account.POSITION_DATE,
                                Month.toDate(lastTransaction.get(Transaction.POSITION_MONTH),
                                             lastTransaction.get(Transaction.POSITION_DAY))),
                          value(Account.POSITION_WITH_PENDING, total));
      }
      else {
        repository.update(KeyBuilder.newKey(Account.TYPE, id),
                          value(Account.POSITION_DATE, Month.toDate(monthId, day)),
                          value(Account.POSITION_WITH_PENDING, 0.));
      }
    }
  }

  private Glob[] extractAccountTransaction(Glob[] allTransactions, Integer accountId) {
    int count = 0;
    for (Glob transaction : allTransactions) {
      if (accountId.equals(transaction.get(Transaction.ACCOUNT))) {
        count++;
      }
    }
    Glob[] transactions = new Glob[count];
    int i = 0;
    for (Glob transaction : allTransactions) {
      if (accountId.equals(transaction.get(Transaction.ACCOUNT))) {
        transactions[i] = transaction;
        ++i;
      }
    }
    return transactions;
  }

  private static void computeTotalPosition(GlobRepository repository, Glob[] transactions) {
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    GlobMatcher futureOperations = getMatcherForFutureOperations(currentMonth.get(CurrentMonth.CURRENT_MONTH), currentMonth.get(CurrentMonth.CURRENT_DAY));

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
                           new SameAccountChecker(AccountType.MAIN.getId(), repository), futureOperations);
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
                           new SameAccountChecker(AccountType.SAVINGS.getId(), repository), futureOperations);
    }
  }


  private void updateTransactionButNotAccountPosition(GlobRepository repository, Set<Integer> accountIds, Glob[] allTransactions, Set<Integer> deferredAccountIds) {
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    for (Integer id : accountIds) {
      if (Account.SUMMARY_ACCOUNT_IDS.contains(id)) {
        continue;
      }
      Glob account = repository.find(Key.create(Account.TYPE, id));
      if (account != null) {
        Double currentPos = account.get(Account.POSITION_WITH_PENDING);
        if (currentPos != null) {
          GlobMatcher futureMatcher = getMatcherForFutureOperations(id, currentMonth.get(CurrentMonth.CURRENT_MONTH),
                                                                    currentMonth.get(CurrentMonth.CURRENT_DAY));

          Glob[] positions = extractAccountTransaction(allTransactions, id);
          int pivot;
          for (pivot = positions.length - 1; pivot >= 0; pivot--) {
            if (!futureMatcher.matches(positions[pivot], repository)) {
              break;
            }
          }
          double tmp = currentPos;
          IsOpenClose openClose = new IsOpenClose();
          for (int j = pivot; j >= 0; j--) {
            Glob position = positions[j];
            if (!openClose.isOpenOrClose(position)) {
              repository.update(position.getKey(), Transaction.ACCOUNT_POSITION, tmp);
              tmp -= position.get(Transaction.AMOUNT, 0.);
            }
          }
          tmp = currentPos;
          for (int j = pivot + 1; j < positions.length; j++) {
            Glob position = positions[j];
            if (!openClose.isOpenOrClose(position)) {
              tmp += position.get(Transaction.AMOUNT, 0.);
              repository.update(position.getKey(), Transaction.ACCOUNT_POSITION, tmp);
            }
          }
          if (openClose.closeOperation != null) {
            repository.update(openClose.closeOperation.getKey(), value(Transaction.AMOUNT,
                                                                       -(positions[positions.length - 2].get(Transaction.ACCOUNT_POSITION))),
                              value(Transaction.ACCOUNT_POSITION, 0.));
          }
          if (openClose.openOperation != null && positions.length > 1) {
            double value = (positions[1].get(Transaction.ACCOUNT_POSITION)) - positions[1].get(Transaction.AMOUNT, 0.);
            repository.update(openClose.openOperation.getKey(), value(Transaction.AMOUNT, value),
                              value(Transaction.ACCOUNT_POSITION, 0.));
          }
        }
      }
    }
  }

  static class IsOpenClose {
    Glob openOperation;
    Glob closeOperation;

    boolean isOpenOrClose(Glob position) {
      Integer tt = position.get(Transaction.TRANSACTION_TYPE);
      if (tt != null && (tt.equals(TransactionType.OPEN_ACCOUNT_EVENT.getId())
                         || tt.equals(TransactionType.CLOSE_ACCOUNT_EVENT.getId()))) {
        if (tt.equals(TransactionType.CLOSE_ACCOUNT_EVENT.getId())) {
          closeOperation = position;
        }
        else {
          openOperation = position;
        }
        return true;
      }
      return false;
    }
  }

  private void updateTransactionImpactAccounts(GlobRepository repository, ChangeSet changeSet,
                                               Set<Integer> operations, Set<Integer> accountIds,
                                               Set<Integer> createdAccounts,
                                               boolean isImport, Glob[] allTransactions, Set<Integer> deferredAccountIds) {
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    for (Integer accountId : accountIds) {
      Glob[] transactions = extractAccountTransaction(allTransactions, accountId);
      updateAccount(repository, changeSet, operations, isImport, currentMonth, accountId,
                    createdAccounts.contains(accountId), transactions, deferredAccountIds);
    }
  }

  private void updateAccount(GlobRepository repository, ChangeSet changeSet, Set<Integer> operations,
                             boolean isImport,
                             Glob currentMonth, Integer accountId, boolean isCreation, Glob[] transactions,
                             Set<Integer> deferredAccountIds) {
    if (!Account.SUMMARY_ACCOUNT_IDS.contains(accountId)) {
      ExtractDate extractDate = new ExtractDate(accountId);
      changeSet.safeVisit(Transaction.TYPE, extractDate);
      Integer monthId = currentMonth.get(CurrentMonth.CURRENT_MONTH);
      Integer day = currentMonth.get(CurrentMonth.CURRENT_DAY);
      GlobMatcher futureMatcher = getMatcherForFutureOperations(accountId, monthId, day);
      GlobMatcher realMatcher = TransactionMatchers.realTransactions(accountId, monthId, day);

      Key accountKey = Key.create(Account.TYPE, accountId);
      Glob account = repository.get(accountKey);
      if (isCreation && account.get(Account.LAST_IMPORT_POSITION) != null) {
        // cas particulier du compte sans aucune operations.
        if (transactions.length == 1 || (transactions.length == 2 &&
                                         transactions[1].get(Transaction.TRANSACTION_TYPE).equals(TransactionType.CLOSE_ACCOUNT_EVENT.getId()))) {
          repository.update(transactions[0].getKey(), Transaction.AMOUNT, account.get(Account.LAST_IMPORT_POSITION));
        }
        else {
          for (int i = transactions.length - 1; i >= 0; i--) {
            Glob transaction = transactions[i];
            if (realMatcher.matches(transaction, repository) && !deferredAccountIds.contains(transaction.get(Transaction.ORIGINAL_ACCOUNT))) {
              repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, account.get(Account.LAST_IMPORT_POSITION));
              updateFirstTransactionFromPivotPosition(transactions, i, repository, account, deferredAccountIds);
              break;
            }
          }
        }
      }
      else if (!isCreation && (changeSet.containsChanges(accountKey, Account.PAST_POSITION) ||
                               changeSet.containsChanges(accountKey, Account.LAST_TRANSACTION))) {
        Integer trnId = account.get(Account.LAST_TRANSACTION);
        // si on modifie le solde du compte via une operation particuliere
        if (trnId != null) {
          for (int i = 0; i < transactions.length; i++) {
            Glob transaction = transactions[i];
            if (transaction.get(Transaction.ID).equals(trnId)) {
              if (transaction.get(Transaction.TRANSACTION_TYPE) == TransactionType.OPEN_ACCOUNT_EVENT.getId()) {
                repository.update(transaction.getKey(), Transaction.AMOUNT, account.get(Account.PAST_POSITION));
              }
              else {
                repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, account.get(Account.PAST_POSITION));
                updateFirstTransactionFromPivotPosition(transactions, i, repository, account, deferredAccountIds);
              }
              break;
            }
          }
        }
      }
      if (isImport) {
        // Si la premier operation est une nouvelle operation on part de la fin, sinon on part toujours du debut.
        boolean startFromLast = false;
        for (Glob transaction : transactions) {
          if (realMatcher.matches(transaction, repository) && !deferredAccountIds.contains(transaction.get(Transaction.ORIGINAL_ACCOUNT))) {
            if (operations.contains(transaction.get(Transaction.ID))) {
              startFromLast = true;
            }
            break;
          }
        }
        if (startFromLast) {
          for (int i = transactions.length - 1; i > 0; i--) {
            Glob transaction = transactions[i];
            if (realMatcher.matches(transaction, repository) &&
                !deferredAccountIds.contains(transaction.get(Transaction.ORIGINAL_ACCOUNT))) {
              if (!operations.contains(transaction.get(Transaction.ID))) {
                updateFirstTransactionFromPivotPosition(transactions, i, repository, account, deferredAccountIds);
                break;
              }
            }
          }
        }
      }
      computeAccountPosition(transactions, futureMatcher, realMatcher, accountId, repository, changeSet, deferredAccountIds);
    }
  }

  private void updateFirstTransactionFromPivotPosition(Glob[] transactions, int pivot, GlobRepository repository,
                                                       Glob account, Set<Integer> deferredAccountIds) {
    double beforePosition;
    if (pivot != 0) {
      beforePosition = transactions[pivot].get(Transaction.ACCOUNT_POSITION) - transactions[pivot].get(Transaction.AMOUNT, 0.);
      for (int i = pivot - 1; i > 0; i--) {
        if (!deferredAccountIds.contains(transactions[i].get(Transaction.ORIGINAL_ACCOUNT))) {
          beforePosition -= transactions[i].get(Transaction.AMOUNT, 0.);
        }
      }
      if (!transactions[0].get(Transaction.TRANSACTION_TYPE).equals(TransactionType.OPEN_ACCOUNT_EVENT.getId())) {
        throw new RuntimeException("Bug : first transaction is not open account transaction.");
      }

      AccountInitialPositionTrigger.shiftTransactionTo(repository, transactions[0],
                                                       transactions[0].get(Transaction.POSITION_MONTH),
                                                       transactions[0].get(Transaction.POSITION_DAY),
                                                       beforePosition, beforePosition);
      repository.update(account.getKey(), Account.FIRST_POSITION, beforePosition);
    }

  }

  private GlobMatcher getMatcherForFutureOperations(int accountId, int monthId, int day) {
    return and(fieldEquals(Transaction.ACCOUNT, accountId),
               GlobMatchers.or(
                 GlobMatchers.isTrue(Transaction.PLANNED),
                 fieldEquals(Transaction.TO_RECONCILE, Boolean.TRUE),
                 GlobMatchers.fieldStrictlyGreaterThan(Transaction.POSITION_MONTH, monthId),
                 and(
                   fieldEquals(Transaction.POSITION_MONTH, monthId),
                   GlobMatchers.fieldStrictlyGreaterThan(Transaction.POSITION_DAY, day))
               ));
  }

  private static GlobMatcher getMatcherForFutureOperations(int monthId, int day) {
    return GlobMatchers.or(GlobMatchers.isTrue(Transaction.PLANNED),
                           GlobMatchers.fieldStrictlyGreaterThan(Transaction.POSITION_MONTH, monthId),
                           fieldEquals(Transaction.TO_RECONCILE, Boolean.TRUE),
                           and(
                             fieldEquals(Transaction.POSITION_MONTH, monthId),
                             GlobMatchers.fieldStrictlyGreaterThan(Transaction.POSITION_DAY, day))
    );
  }


  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
//    GlobList account = repository.getAll(Account.TYPE);
//    if (account.isEmpty()){
//      return;
//    }
//    updateTransactionPosition(repository, account);
  }


  private boolean computeDeferredPositionForNotAssociatedAccount(GlobRepository repository, Glob[] transactions, Glob account) {
    int transactionMonthId = 0;
    double amount = 0;
    Glob lastTransaction = null;
    Integer accountId = account.get(Account.ID);
    for (Glob transaction : transactions) {
      if (transaction.get(Transaction.ORIGINAL_ACCOUNT).equals(accountId)) {
        lastTransaction = transaction;
        if (transaction.get(Transaction.POSITION_MONTH) != transactionMonthId) {
          amount = 0;
          transactionMonthId = transaction.get(Transaction.POSITION_MONTH);
        }
        amount += transaction.get(Transaction.AMOUNT, 0.);
        repository.update(transaction.getKey(), FieldValue.value(Transaction.ACCOUNT_POSITION, amount),
                          FieldValue.value(Transaction.SUMMARY_POSITION, 0.));
      }
    }

//    repository.update(account.getKey(), Account.TRANSACTION_ID, lastUpdateTransactionId);
//    if (lastUpdateTransactionId != null) {
//      Glob lastTransaction = repository.get(Key.create(Transaction.TYPE, lastUpdateTransactionId));
    if (lastTransaction != null) {
      repository.update(account.getKey(),
                        value(Account.POSITION_DATE,
                              Month.toDate(lastTransaction.get(Transaction.POSITION_MONTH),
                                           lastTransaction.get(Transaction.POSITION_DAY))),
                        value(Account.POSITION_WITH_PENDING, lastTransaction.get(Transaction.ACCOUNT_POSITION)));
    }

    return true;
  }

  static class AccountUpdate {
    final GlobMatcher futureMatcher;
    final GlobMatcher realMatcher;
    final GlobRepository repository;
    private boolean checkAccountPosition;
    Glob lastTransaction = null;
    Glob lastRealTransaction = null;
    Glob closeTransaction = null;
    Glob openTransaction = null;
    Glob lastOp = null;

    AccountUpdate(GlobMatcher futureMatcher, GlobMatcher realMatcher, GlobRepository repository, boolean checkAccountPosition) {
      this.futureMatcher = futureMatcher;
      this.realMatcher = realMatcher;
      this.repository = repository;
      this.checkAccountPosition = checkAccountPosition;
    }

    void update(Glob transaction) {
      if (openTransaction == null) {
        openTransaction = transaction;
      }
      if (!futureMatcher.matches(transaction, repository)) {
        lastTransaction = transaction;
      }
      if (realMatcher.matches(transaction, repository)) {
        lastRealTransaction = transaction;
      }

      if (transaction.get(Transaction.IMPORT) != null &&
          closeTransaction != null &&
          closeTransaction.get(Transaction.IMPORT) != null &&
          !closeTransaction.get(Transaction.IMPORT).equals(transaction.get(Transaction.IMPORT))) {
        lastOp = closeTransaction;
      }

      closeTransaction = transaction;
    }

    void updateAccount(Glob account) {
      if (lastTransaction != null) {
        Date accountDate = Month.toDate(lastTransaction.get(Transaction.POSITION_MONTH), lastTransaction.get(Transaction.POSITION_DAY));
        repository.update(account.getKey(),
                          value(Account.POSITION_DATE, accountDate),
                          value(Account.POSITION_WITH_PENDING, lastTransaction.get(Transaction.ACCOUNT_POSITION)));
        if (checkAccountPosition) {
          if (account.get(Account.LAST_IMPORT_POSITION) != null &&
              !Amounts.equal(lastTransaction.get(Transaction.ACCOUNT_POSITION), account.get(Account.LAST_IMPORT_POSITION))) {
            Glob accountError = repository.findOrCreate(Key.create(AccountPositionError.TYPE, account.get(Account.ID)));
            repository.update(accountError.getKey(),
                              value(AccountPositionError.UPDATE_DATE, TimeService.getToday()),
                              value(AccountPositionError.CLEARED, false),
                              value(AccountPositionError.IMPORTED_POSITION, account.get(Account.LAST_IMPORT_POSITION)),
                              value(AccountPositionError.LAST_REAL_OPERATION_POSITION, lastTransaction.get(Transaction.ACCOUNT_POSITION)),
                              value(AccountPositionError.LAST_PREVIOUS_IMPORT_DATE,
                                    lastOp != null ? Month.toFullDate(lastOp.get(Transaction.BANK_MONTH),
                                                                      lastOp.get(Transaction.BANK_DAY))
                                                   : null));
          }
          else {
            Glob accountError = repository.find(Key.create(AccountPositionError.TYPE, account.get(Account.ID)));
            if (accountError != null) {
              repository.update(accountError.getKey(), AccountPositionError.CLEARED, true);
            }
          }
        }
      }
      else if (openTransaction != null) {
        Date accountDate = Month.toDate(openTransaction.get(Transaction.POSITION_MONTH), openTransaction.get(Transaction.POSITION_DAY));
        repository.update(account.getKey(),
                          value(Account.POSITION_DATE, accountDate),
                          value(Account.POSITION_WITH_PENDING, openTransaction.get(Transaction.ACCOUNT_POSITION)));
      }

      if (lastRealTransaction != null) {
        repository.update(account.getKey(),
                          value(Account.LAST_TRANSACTION, lastRealTransaction.get(Transaction.ID)),
                          value(Account.PAST_POSITION, lastRealTransaction.get(Transaction.ACCOUNT_POSITION)));
      }
      else if (lastTransaction != null) {
        repository.update(account.getKey(),
                          value(Account.LAST_TRANSACTION, lastTransaction.get(Transaction.ID)),
                          value(Account.PAST_POSITION, lastTransaction.get(Transaction.ACCOUNT_POSITION)));
      }
      else if (openTransaction != null) {
        repository.update(account.getKey(),
                          value(Account.LAST_TRANSACTION, openTransaction.get(Transaction.ID)),
                          value(Account.PAST_POSITION, openTransaction.get(Transaction.ACCOUNT_POSITION)));
      }
      Integer tt = closeTransaction.get(Transaction.TRANSACTION_TYPE);
      if (tt != null && tt == TransactionType.CLOSE_ACCOUNT_EVENT.getId()) {
        repository.update(closeTransaction.getKey(), Transaction.AMOUNT, -(closeTransaction.get(Transaction.ACCOUNT_POSITION))
                                                                         + closeTransaction.get(Transaction.AMOUNT, 0.));
        repository.update(closeTransaction.getKey(), Transaction.ACCOUNT_POSITION, 0.);
      }
      else {
        repository.update(account.getKey(), Account.CLOSE_POSITION, -(closeTransaction.get(Transaction.ACCOUNT_POSITION))
                                                                    + closeTransaction.get(Transaction.AMOUNT, 0.));
      }
    }
  }

  private boolean computeAccountPosition(Glob[] transactions, GlobMatcher futureMatcher,
                                         GlobMatcher realMatcher, Integer accountId, GlobRepository repository,
                                         ChangeSet changeSet, Set<Integer> deferredAccountIds) {
    if (transactions.length == 0) {
      return false;
    }

    int lastTrDate = getLastTransactionDate(repository);
    MapOfMaps<Integer, Integer, Glob> accountDateDeferredOperations = getMonthWithDeferredOperations(repository);
    Glob account = repository.get(Key.create(Account.TYPE, accountId));
    AccountUpdate update = new AccountUpdate(futureMatcher, realMatcher, repository,
                                             changeSet.containsChanges(account.getKey(), Account.LAST_IMPORT_POSITION));
    double positionBefore = 0;
    Glob closeTransaction = null;
    Glob lastTransaction = null;
    for (Glob transaction : transactions) {
      Integer tt = transaction.get(Transaction.TRANSACTION_TYPE);
      if (tt != null && tt.equals(TransactionType.CLOSE_ACCOUNT_EVENT.getId())) {
        closeTransaction = transaction;
      }
      else {
        if (deferredAccountIds.contains(transaction.get(Transaction.ORIGINAL_ACCOUNT))) {
          boolean isPrelevementPresent = accountDateDeferredOperations.containsKey(transaction.get(Transaction.ORIGINAL_ACCOUNT),
                                                                                   transaction.get(Transaction.POSITION_MONTH));
          if (Transaction.fullPositionDate(transaction) > lastTrDate && !isPrelevementPresent) {
            positionBefore = positionBefore + transaction.get(Transaction.AMOUNT, 0.);
            repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, positionBefore);
          }
          else {
            repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, positionBefore);
          }
        }
        else {
          update.update(transaction);
          positionBefore = positionBefore + transaction.get(Transaction.AMOUNT, 0.);
          repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, positionBefore);
        }
        lastTransaction = transaction;
      }
    }
    if (closeTransaction != null) {
      update.update(closeTransaction);
    }
    int lastDate = 0;
    if (closeTransaction != null) {
      if (lastTransaction != null) {
        lastDate = Month.toFullDate(lastTransaction.get(Transaction.POSITION_MONTH),
                                    lastTransaction.get(Transaction.POSITION_DAY));
      }
      int date = Month.toFullDate(closeTransaction.get(Transaction.POSITION_MONTH),
                                  closeTransaction.get(Transaction.POSITION_DAY));
      if (date < lastDate) {
        date = lastDate;
      }
      AccountInitialPositionTrigger.shiftTransactionTo(repository, closeTransaction,
                                                       Month.getMonthIdFromFullDate(date),
                                                       Month.getDayFromFullDate(date),
                                                       -positionBefore, 0.);
    }
    update.updateAccount(account);
    return true;
  }

  private static int getLastTransactionDate(GlobRepository repository) {
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    return Month.toFullDate(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH),
                            currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY));
  }

  private static void computeTotalPosition(GlobRepository repository, Glob[] transactions,
                                           final SameAccountChecker sameCheckerAccount, GlobMatcher futureMatcher) {

    double position = 0;
    Glob realPosition = null;
    boolean canUpdate = true;

    int lastTrDate = getLastTransactionDate(repository);

    Set<Integer> deferredAccounts = repository.getAll(Account.TYPE,
                                                      fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()))
      .getSortedSet(Account.ID);
    MapOfMaps<Integer, Integer, Glob> accountDateDeferredOperations = getMonthWithDeferredOperations(repository);


    for (Glob transaction : transactions) {
      Integer mainAccountId = transaction.get(Transaction.ACCOUNT);
      if (!sameCheckerAccount.isSame(mainAccountId)) {
        continue;
      }
      Integer accountId = transaction.get(Transaction.ORIGINAL_ACCOUNT);
      if (deferredAccounts.contains(accountId) &&
          (Transaction.fullPositionDate(transaction) <= lastTrDate ||
           accountDateDeferredOperations.containsKey(accountId, transaction.get(Transaction.POSITION_MONTH)))) {
        repository.update(transaction.getKey(), Transaction.SUMMARY_POSITION, position);
        continue;
      }
      position += transaction.get(Transaction.AMOUNT, 0.);
      repository.update(transaction.getKey(), Transaction.SUMMARY_POSITION, position);
      boolean isFutureOp = futureMatcher.matches(transaction, repository);
      // !deferredAccounts.contains(accountId) &&
      if (!isFutureOp && canUpdate) {
        realPosition = transaction;
      }
      canUpdate &= !isFutureOp;
    }
    // pour les operations du compte differé :
    // on force explicitement le montant summary au montant summary de l'operation de prelevement si elle existe
//    for (index = 0; index < transactions.length; index++) {
//      Glob transaction = transactions[index];
//      if (!sameCheckerAccount.isSame(transaction.get(Transaction.ACCOUNT))) {
//        continue;
//      }
//      Integer accountId = transaction.get(Transaction.ACCOUNT);
//      if (deferredAccounts.contains(accountId)) {
//        Glob withDrawOperation = isWithDrawBySeriesByMonth.get(accountId, transaction.get(Transaction.POSITION_MONTH));
//        if (withDrawOperation != null) {
//          repository.update(transaction.getKey(), Transaction.SUMMARY_POSITION, withDrawOperation.get(Transaction.SUMMARY_POSITION));
//        }
//      }
//    }

    if (realPosition != null) {
//      repository.update(sameCheckerAccount.getSummary(),
//                        value(Account.POSITION_WITH_PENDING, realPosition.get(Transaction.SUMMARY_POSITION)),
//                        value(Account.POSITION_DATE,
//                              Month.toDate(realPosition.get(Transaction.BANK_MONTH),
//                                           realPosition.get(Transaction.BANK_DAY))));
      GlobList accounts = repository.getAll(Account.TYPE).filter(new GlobMatcher() {
        public boolean matches(Glob item, GlobRepository repository) {
          return sameCheckerAccount.isSame(item.get(Account.ID)) && Account.isUserCreatedAccount(item) &&
                 Account.isNotDeferred(item);
        }
      }, repository);
      double total = 0;
      int lastDate = 0;
      for (Glob account : accounts) {
//        if (!deferredAccounts.contains(account.get(Account.ID))) {
        Date closedDate = account.get(Account.CLOSED_DATE);
        if (closedDate == null || Month.toFullDate(closedDate) > TimeService.getCurrentFullDate()) {
          total += account.get(Account.POSITION_WITH_PENDING, 0);
        }
        int tmp = Month.getFullDate(account.get(Account.POSITION_DATE));
        if (tmp > lastDate) {
          lastDate = tmp;
        }
//        }
      }
      if (lastDate != 0) {
        repository.update(sameCheckerAccount.getSummary(),
                          value(Account.POSITION_WITH_PENDING, total),
                          value(Account.POSITION_DATE,
                                Month.toDate(Month.getMonthIdFromFullDate(lastDate),
                                             Month.getDayFromFullDate(lastDate))));
      }
    }
  }

  private static MapOfMaps<Integer, Integer, Glob> getMonthWithDeferredOperations(GlobRepository repository) {
    GlobList deferredSeries = repository.getAll(Series.TYPE, and(GlobMatchers.isNotNull(Series.FROM_ACCOUNT),
                                                                 GlobMatchers.isNull(Series.TO_ACCOUNT)));
    MapOfMaps<Integer, Integer, Glob> isWithDrawBySeriesByMonth = new MapOfMaps<Integer, Integer, Glob>();
    for (Glob series : deferredSeries) {
      GlobList deferredTransactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
        .getGlobs();
      for (Glob transaction : deferredTransactions) {
        isWithDrawBySeriesByMonth.put(series.get(Series.FROM_ACCOUNT), transaction.get(Transaction.POSITION_MONTH), transaction);
      }
    }
    return isWithDrawBySeriesByMonth;
  }

  public static abstract class AbstractAccountChangeSetVisitor implements ChangeSetVisitor {
    protected final Set<Integer> accountIds;
    protected final Set<Integer> deferredAccountIds;
    protected final GlobRepository repository;

    public AbstractAccountChangeSetVisitor(Set<Integer> accountIds, Set<Integer> deferredAccountIds, GlobRepository repository) {
      this.accountIds = accountIds;
      this.deferredAccountIds = deferredAccountIds;
      this.repository = repository;
    }

    protected void update(Integer accountId) {
      if (!accountIds.contains(accountId) && !deferredAccountIds.contains(accountId)) {
        Glob account = repository.find(Key.create(Account.TYPE, accountId));
        if (account != null) {
          if (account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
            deferredAccountIds.add(accountId);
          }
          else {
            accountIds.add(accountId);
          }
        }
      }
    }
  }

  private static class AccountChangesetVisitor extends AbstractAccountChangeSetVisitor {
    private Set<Integer> accountCreated;

    public AccountChangesetVisitor(Set<Integer> accountIds, Set<Integer> deferredAccountIds, Set<Integer> accountCreated, GlobRepository repository) {
      super(accountIds, deferredAccountIds, repository);
      this.accountCreated = accountCreated;
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      accountCreated.add(key.get(Account.ID));
      update(key.get(Account.ID));
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      update(key.get(Account.ID));
    }

    public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      update(key.get(Account.ID));
    }
  }

  private static class TransactionChangeSetVisitor extends AbstractAccountChangeSetVisitor {
    private final Set<Integer> updatedOperations;
    private int firstDate = Integer.MAX_VALUE;
    private int lastDate = Integer.MIN_VALUE;
    private boolean isImport = false;

    public TransactionChangeSetVisitor(Set<Integer> updatedOperations, Set<Integer> accountIds,
                                       Set<Integer> deferredAccountIds, GlobRepository repository) {
      super(accountIds, deferredAccountIds, repository);
      this.updatedOperations = updatedOperations;
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      if (values.get(Transaction.IMPORT) != null) {
        isImport = true;
      }
      update(values.get(Transaction.ACCOUNT));
      update(values.get(Transaction.ORIGINAL_ACCOUNT));
      updatedOperations.add(key.get(Transaction.ID));
      int date = Month.toFullDate(values.get(Transaction.BANK_MONTH), values.get(Transaction.BANK_DAY));
      firstDate = Math.min(firstDate, date);
      lastDate = Math.max(lastDate, date);
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      if (values.contains(Transaction.AMOUNT) || values.contains(Transaction.POSITION_MONTH)
          || values.contains(Transaction.POSITION_DAY)
          || (isDeferredSeries(key, values))) {
        Glob transaction = repository.find(key);
        update(transaction.get(Transaction.ACCOUNT));
        update(transaction.get(Transaction.ORIGINAL_ACCOUNT));
        updatedOperations.add(key.get(Transaction.ID));
      }
      if (values.contains(Transaction.ACCOUNT)) {
        update(values.get(Transaction.ACCOUNT));
        update(values.getPrevious(Transaction.ACCOUNT));
        updatedOperations.add(key.get(Transaction.ID));
      }
      if (values.contains(Transaction.ORIGINAL_ACCOUNT)) {
        update(values.get(Transaction.ORIGINAL_ACCOUNT));
        update(values.getPrevious(Transaction.ORIGINAL_ACCOUNT));
        updatedOperations.add(key.get(Transaction.ID));
      }
    }

    private boolean isDeferredSeries(Key key, FieldValuesWithPrevious values) {
      if (values.contains(Transaction.SERIES)) {
        Glob series = repository.findLinkTarget(repository.get(key), Transaction.SERIES);
        if (series.get(Series.FROM_ACCOUNT) != null && series.get(Series.TO_ACCOUNT) == null) {
          return true;
        }
      }
      return false;
    }

    public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      update(previousValues.get(Transaction.ACCOUNT));
      update(previousValues.get(Transaction.ORIGINAL_ACCOUNT));
      updatedOperations.add(key.get(Transaction.ID));
    }
  }

  private class ExtractDate implements ChangeSetVisitor {
    private int accountId;
    private int lastMonthId = 0;
    private int day = 0;

    public ExtractDate(Integer accountId) {
      this.accountId = accountId;
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      if (values.get(Transaction.ACCOUNT) == accountId) {
        if (values.get(Transaction.POSITION_MONTH) > lastMonthId) {
          lastMonthId = values.get(Transaction.POSITION_MONTH);
          day = values.get(Transaction.POSITION_DAY);
        }
        if (values.get(Transaction.POSITION_MONTH) == lastMonthId && values.get(Transaction.POSITION_DAY) < day) {
          day = values.get(Transaction.POSITION_DAY);
        }
      }
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
    }

    public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
    }
  }
}
