package org.designup.picsou.triggers;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.collections.MapOfMaps;

import java.util.*;

import static org.globsframework.model.FieldValue.value;

public class PositionTrigger implements ChangeSetListener {


  static class OperationsPeriod {
    final int firstDate;
    final int lastDate;

    OperationsPeriod(int firstDate, int lastDate) {
      this.firstDate = firstDate;
      this.lastDate = lastDate;
    }
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    final Set<Integer> accountIds = new HashSet<Integer>();
    final Set<Integer> deferredAccountIds = new HashSet<Integer>();
    if (changeSet.containsChanges(Transaction.TYPE) ||
        changeSet.containsUpdates(Account.POSITION_WITH_PENDING) ||
        changeSet.containsChanges(Account.TYPE)) {
      final Set<Integer> updatedOperations = new HashSet<Integer>();
      final Set<Integer> createdAccount = new HashSet<Integer>();
      changeSet.safeVisit(Account.TYPE, new AccountChangesetVisitor(accountIds, deferredAccountIds, createdAccount, repository));
      TransactionChangeSetVisitor transactionChangeSetVisitor = new TransactionChangeSetVisitor(updatedOperations, accountIds, deferredAccountIds, repository);
      changeSet.safeVisit(Transaction.TYPE, transactionChangeSetVisitor);

      Set<Key> created = changeSet.getCreated(AccountPositionMode.TYPE);
      if (created.size() == 1) {
        Glob glob = repository.get(created.iterator().next());
        boolean shouldUpdatePosition = glob.get(AccountPositionMode.UPDATE_ACCOUNT_POSITION, true);
        if (shouldUpdatePosition) {
          updateTransactionImpactAccounts(repository, changeSet, updatedOperations, accountIds, createdAccount, false);
        }
        else {
          updateTransactionButNotAccountPosition(repository, accountIds);
        }
      }
      else {
        boolean isImport = changeSet.containsChanges(TransactionImport.TYPE);
        updateTransactionImpactAccounts(repository, changeSet, updatedOperations, accountIds, createdAccount, isImport);
      }
      if (accountIds.isEmpty() && deferredAccountIds.isEmpty()) {
        return;
      }

      updateDeferredAccount(repository, deferredAccountIds);

      computeTotal(repository);
    }
    if (changeSet.containsChanges(CurrentMonth.TYPE)) {
      Set<Integer> allAccountIds =
        repository.getAll(Account.TYPE,
                          GlobMatchers.and(
                            GlobMatchers.not(GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId())),
                            GlobMatchers.not(GlobMatchers.contained(Account.ID, accountIds))))
          .getValueSet(Account.ID);
      updateTransactionImpactAccounts(repository, changeSet, Collections.<Integer>emptySet(), allAccountIds,
                                      Collections.<Integer>emptySet(), false);
      Set<Integer> allDeferredAccountIds =
        repository.getAll(Account.TYPE,
                          GlobMatchers.and(
                            GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()),
                            GlobMatchers.not(GlobMatchers.contained(Account.ID, deferredAccountIds)))).getValueSet(Account.ID);

      updateDeferredAccount(repository, allDeferredAccountIds);
      computeTotal(repository);
    }
  }

  public static void computeTotal(GlobRepository repository) {
    TransactionComparator comparator = TransactionComparator.ASCENDING_ACCOUNT;
    SortedSet<Glob> trs = repository.getSorted(Transaction.TYPE, comparator, GlobMatchers.ALL);

    Glob[] transactions = trs.toArray(new Glob[trs.size()]);

    computeTotalPosition(repository, transactions);
  }

  private void updateDeferredAccount(GlobRepository repository, Set<Integer> deferredAccountIds) {
    for (Integer id : deferredAccountIds) {
      TransactionComparator comparator = TransactionComparator.ASCENDING_ACCOUNT;
      SortedSet<Glob> trs = repository.getSorted(Transaction.TYPE, comparator,
                                                 GlobMatchers.fieldEquals(Transaction.ACCOUNT, id));

      Glob[] transactions = trs.toArray(new Glob[trs.size()]);

      computeDeferredPosition(repository, transactions, repository.get(Key.create(Account.TYPE, id)));
    }
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


  private void updateTransactionButNotAccountPosition(GlobRepository repository, Set<Integer> accountIds) {
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
          Glob[] positions = getAllTransactionForAccount(id, repository);
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
              tmp -= position.get(Transaction.AMOUNT);
            }
          }
          tmp = currentPos;
          for (int j = pivot + 1; j < positions.length; j++) {
            Glob position = positions[j];
            if (!openClose.isOpenOrClose(position)) {
              tmp += position.get(Transaction.AMOUNT);
              repository.update(position.getKey(), Transaction.ACCOUNT_POSITION, tmp);
            }
          }
          if (openClose.closeOperation != null) {
            repository.update(openClose.closeOperation.getKey(), value(Transaction.AMOUNT,
                                                                       -(positions[positions.length - 2].get(Transaction.ACCOUNT_POSITION))),
                              value(Transaction.ACCOUNT_POSITION, 0.));
          }
          if (openClose.openOperation != null) {
            double value = (positions[1].get(Transaction.ACCOUNT_POSITION)) - positions[1].get(Transaction.AMOUNT);
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
                                               boolean isImport) {
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    for (Integer accountId : accountIds) {
      updateAccount(repository, changeSet, operations, isImport, currentMonth, accountId,
                    createdAccounts.contains(accountId));
    }
  }

  private void updateAccount(GlobRepository repository, ChangeSet changeSet, Set<Integer> operations,
                             boolean isImport,
                             Glob currentMonth, Integer accountId, boolean isCreation) {
    if (!Account.SUMMARY_ACCOUNT_IDS.contains(accountId)) {
      ExtractDate extractDate = new ExtractDate(accountId);
      changeSet.safeVisit(Transaction.TYPE, extractDate);
      Integer monthId = currentMonth.get(CurrentMonth.CURRENT_MONTH);
      Integer day = currentMonth.get(CurrentMonth.CURRENT_DAY);
      GlobMatcher futureMatcher = getMatcherForFutureOperations(accountId, monthId, day);
      GlobMatcher realMatcher = Transaction.getMatcherForRealOperations(accountId, monthId, day);
      Glob[] transactions = getAllTransactionForAccount(accountId, repository);

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
            if (realMatcher.matches(transaction, repository)) {
              repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, account.get(Account.LAST_IMPORT_POSITION));
              updateFirstTransactionFromPivotPosition(transactions, i, repository, account);
              break;
            }
          }
        }
      }
      else if (!isCreation && (changeSet.containsChanges(accountKey, Account.PAST_POSITION) ||
                               changeSet.containsChanges(accountKey, Account.TRANSACTION_ID))) {
        Integer trnId = account.get(Account.TRANSACTION_ID);
        // si on modifie le solde du compte via une operation particulier
        if (trnId != null) {
          for (int i = 0; i < transactions.length; i++) {
            Glob transaction = transactions[i];
            if (transaction.get(Transaction.ID).equals(trnId)) {
              repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, account.get(Account.PAST_POSITION));
              updateFirstTransactionFromPivotPosition(transactions, i, repository, account);
              break;
            }
          }
        }
      }
      if (isImport) {
        // Si la premier operation est une nouvelle operation on part de la fin, sinon on part toujours du debut.
        boolean startFromLast = false;
        for (Glob transaction : transactions) {
          if (realMatcher.matches(transaction, repository)) {
            if (operations.contains(transaction.get(Transaction.ID))) {
              startFromLast = true;
            }
            break;
          }
        }
        if (startFromLast) {
          for (int i = transactions.length - 1; i > 0; i--) {
            Glob transaction = transactions[i];
            if (realMatcher.matches(transaction, repository)) {
              if (!operations.contains(transaction.get(Transaction.ID))) {
                updateFirstTransactionFromPivotPosition(transactions, i, repository, account);
                break;
              }
            }
          }
        }
      }
      computeAccountPosition(transactions, futureMatcher, realMatcher, accountId, repository, changeSet);
    }
  }

  private void updateFirstTransactionFromPivotPosition(Glob[] transactions, int pivot, GlobRepository repository, Glob account) {
    double beforePosition;
    if (pivot != 0) {
      beforePosition = transactions[pivot].get(Transaction.ACCOUNT_POSITION) - transactions[pivot].get(Transaction.AMOUNT);
      for (int i = pivot - 1; i > 0; i--) {
        beforePosition -= transactions[i].get(Transaction.AMOUNT);
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

  /* private void updateFirstTransaction(Glob[] transactions, int pivot, GlobRepository repository, Glob account) {
      double after = transactions[pivot].get(Transaction.ACCOUNT_POSITION) - transactions[pivot].get(Transaction.AMOUNT);
      for (int i = pivot - 1; i >= 1; i--) {
        after -= transactions[i].get(Transaction.AMOUNT);
      }
      repository.update(transactions[0].getKey(), Transaction.AMOUNT, after);
      repository.update(account.getKey(), Account.FIRST_POSITION, after);
    }
  */
  public Glob[] getAllTransactionForAccount(int accountId, GlobRepository repository) {
    SortedSet<Glob> positions = repository.getSorted(Transaction.TYPE, TransactionComparator.ASCENDING_ACCOUNT,
                                                     GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId)));
    return positions.toArray(new Glob[positions.size()]);
  }

  public Glob[] getEffectiveTransactionForAccount(int accountId, GlobRepository repository, int monthId, int day) {
    SortedSet<Glob> positions = repository.getSorted(Transaction.TYPE, TransactionComparator.ASCENDING_ACCOUNT,
                                                     GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId),
                                                                      GlobMatchers.isFalse(Transaction.PLANNED),
                                                                      GlobMatchers.or(
                                                                        GlobMatchers.fieldStrictlyLessThan(Transaction.POSITION_MONTH, monthId),
                                                                        GlobMatchers.and(
                                                                          GlobMatchers.fieldEquals(Transaction.POSITION_MONTH, monthId),
                                                                          GlobMatchers.fieldLessOrEqual(Transaction.POSITION_DAY, day))
                                                                      ))
    );
    return positions.toArray(new Glob[positions.size()]);
  }

  public Glob[] getFutureTransactionForAccount(int accountId, GlobRepository repository, int monthId, int day) {
    SortedSet<Glob> positions = repository.getSorted(Transaction.TYPE, TransactionComparator.ASCENDING_ACCOUNT,
                                                     getMatcherForFutureOperations(accountId, monthId, day)
    );
    return positions.toArray(new Glob[positions.size()]);
  }

  private GlobMatcher getMatcherForFutureOperations(int accountId, int monthId, int day) {
    return GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId),
                            GlobMatchers.or(
                              GlobMatchers.isTrue(Transaction.PLANNED),
                              GlobMatchers.fieldStrictlyGreaterThan(Transaction.POSITION_MONTH, monthId),
                              GlobMatchers.and(
                                GlobMatchers.fieldEquals(Transaction.POSITION_MONTH, monthId),
                                GlobMatchers.fieldStrictlyGreaterThan(Transaction.POSITION_DAY, day))
                            ));
  }

  private static GlobMatcher getMatcherForFutureOperations(int monthId, int day) {
    return GlobMatchers.or(GlobMatchers.isTrue(Transaction.PLANNED),
                           GlobMatchers.fieldStrictlyGreaterThan(Transaction.POSITION_MONTH, monthId),
                           GlobMatchers.and(
                             GlobMatchers.fieldEquals(Transaction.POSITION_MONTH, monthId),
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


  private boolean computeDeferredPosition(GlobRepository repository, Glob[] transactions, Glob account) {
    int transactionMonthId = 0;
    double amount = 0;
    Glob lastTransaction = null;
    for (Glob transaction : transactions) {
      lastTransaction = transaction;
      if (transaction.get(Transaction.POSITION_MONTH) != transactionMonthId) {
        amount = 0;
        transactionMonthId = transaction.get(Transaction.POSITION_MONTH);
      }
      amount += transaction.get(Transaction.AMOUNT);
      repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, amount);
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
        if (checkAccountPosition && account.get(Account.LAST_IMPORT_POSITION) != null &&
            !Amounts.equal(lastTransaction.get(Transaction.ACCOUNT_POSITION), account.get(Account.LAST_IMPORT_POSITION))) {
          Glob accountError = repository.findOrCreate(Key.create(AccountPositionError.TYPE, account.get(Account.ID)));
          repository.update(accountError.getKey(),
                            value(AccountPositionError.UPDATE_DATE, new Date()),
                            value(AccountPositionError.CLEARED, false),
                            value(AccountPositionError.IMPORTED_POSITION, account.get(Account.LAST_IMPORT_POSITION)),
                            value(AccountPositionError.LAST_REAL_OPERATION_POSITION, lastTransaction.get(Transaction.ACCOUNT_POSITION)),
                            value(AccountPositionError.LAST_PREVIOUS_IMPORT_DATE,
                                  lastOp != null ? Month.toFullDate(lastOp.get(Transaction.BANK_MONTH),
                                                                    lastOp.get(Transaction.BANK_DAY))
                                                 : null)
          );
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
                          value(Account.TRANSACTION_ID, lastRealTransaction.get(Transaction.ID)),
                          value(Account.PAST_POSITION, lastRealTransaction.get(Transaction.ACCOUNT_POSITION)));

      }
      else if (lastTransaction != null) {
        repository.update(account.getKey(),
                          value(Account.TRANSACTION_ID, lastTransaction.get(Transaction.ID)),
                          value(Account.PAST_POSITION, lastTransaction.get(Transaction.ACCOUNT_POSITION)));
      }
      else if (openTransaction != null) {
        repository.update(account.getKey(),
                          value(Account.TRANSACTION_ID, openTransaction.get(Transaction.ID)),
                          value(Account.PAST_POSITION, openTransaction.get(Transaction.ACCOUNT_POSITION)));
      }
      Integer tt = closeTransaction.get(Transaction.TRANSACTION_TYPE);
      if (tt != null && tt == TransactionType.CLOSE_ACCOUNT_EVENT.getId()) {
        repository.update(closeTransaction.getKey(), Transaction.AMOUNT, -(closeTransaction.get(Transaction.ACCOUNT_POSITION))
                                                                         + closeTransaction.get(Transaction.AMOUNT));
        repository.update(closeTransaction.getKey(), Transaction.ACCOUNT_POSITION, 0.);
      }
      else {
        repository.update(account.getKey(), Account.CLOSE_POSITION, -(closeTransaction.get(Transaction.ACCOUNT_POSITION))
                                                                    + closeTransaction.get(Transaction.AMOUNT));
      }
    }
  }

  private boolean computeAccountPosition(Glob[] transactions, GlobMatcher futureMatcher,
                                         GlobMatcher realMatcher, Integer accountId, GlobRepository repository,
                                         ChangeSet changeSet) {
    if (transactions.length == 0) {
      return false;
    }

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
        positionBefore = positionBefore + transaction.get(Transaction.AMOUNT);
        repository.update(transaction.getKey(), Transaction.ACCOUNT_POSITION, positionBefore);
        update.update(transaction);
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

  private static void computeTotalPosition(GlobRepository repository, Glob[] transactions,
                                           SameAccountChecker sameCheckerAccount, GlobMatcher futureMatcher) {

    GlobList deferredSeries = repository.getAll(Series.TYPE, GlobMatchers.and(GlobMatchers.isNotNull(Series.FROM_ACCOUNT),
                                                                              GlobMatchers.isNull(Series.TO_ACCOUNT)));
    Set<Integer> deferredAccounts = repository.getAll(Account.TYPE,
                                                      GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()))
      .getSortedSet(Account.ID);
    MapOfMaps<Integer, Integer, Boolean> isWithDrawBySeriesByMonth = new MapOfMaps<Integer, Integer, Boolean>();
    for (Glob series : deferredSeries) {
      GlobList deferredTransactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
        .getGlobs();
      for (Glob transaction : deferredTransactions) {
        isWithDrawBySeriesByMonth.put(series.get(Series.FROM_ACCOUNT), transaction.get(Transaction.POSITION_MONTH), Boolean.TRUE);
      }
    }

    double position = 0;
    int index = 0;
    Glob realPosition = null;
    boolean canUpdate = true;
    for (; index < transactions.length; index++) {
      Glob transaction = transactions[index];
      if (!sameCheckerAccount.isSame(transaction.get(Transaction.ACCOUNT))) {
        continue;
      }
      Integer accountId = transaction.get(Transaction.ACCOUNT);
      if (deferredAccounts.contains(accountId) && isWithDrawBySeriesByMonth.get(accountId, transaction.get(Transaction.POSITION_MONTH)) != null) {
        repository.update(transaction.getKey(), Transaction.SUMMARY_POSITION, position);
        continue;
      }
      position += transaction.get(Transaction.AMOUNT);
      repository.update(transaction.getKey(), Transaction.SUMMARY_POSITION, position);
      boolean isFutureOp = futureMatcher.matches(transaction, repository);
      if (!deferredAccounts.contains(accountId) && !isFutureOp && canUpdate) {
        realPosition = transaction;
      }
      canUpdate &= !isFutureOp;
    }

    if (realPosition != null) {
      repository.update(sameCheckerAccount.getSummary(),
                        value(Account.POSITION_WITH_PENDING, realPosition.get(Transaction.SUMMARY_POSITION)),
                        value(Account.POSITION_DATE,
                              Month.toDate(realPosition.get(Transaction.BANK_MONTH),
                                           realPosition.get(Transaction.BANK_DAY))));
    }
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
      updatedOperations.add(key.get(Transaction.ID));
      int date = Month.toFullDate(values.get(Transaction.BANK_MONTH), values.get(Transaction.BANK_DAY));
      firstDate = Math.min(firstDate, date);
      lastDate = Math.max(lastDate, date);
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      if (values.contains(Transaction.AMOUNT) || values.contains(Transaction.POSITION_MONTH)
          || values.contains(Transaction.POSITION_DAY) || (isDeferredSeries(key, values))) {
        update(repository.find(key).get(Transaction.ACCOUNT));
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
