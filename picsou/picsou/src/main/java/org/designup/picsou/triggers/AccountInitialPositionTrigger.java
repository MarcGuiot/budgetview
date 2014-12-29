package org.designup.picsou.triggers;

import org.designup.picsou.gui.transactions.utils.TransactionMatchers;
import org.designup.picsou.model.*;
import org.globsframework.model.*;
import org.globsframework.model.utils.BreakException;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.globsframework.model.FieldValue.value;

public class AccountInitialPositionTrigger extends AbstractChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Account.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (!Account.isUserCreatedAccount(key.get(Account.ID))) {
          return;
        }
        if (values.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
          return;
        }
        Date openDate = values.get(Account.OPEN_DATE);
        FirstMonthGlobFunctor callback = new FirstMonthGlobFunctor();
        repository.apply(Month.TYPE, GlobMatchers.ALL, callback);
        int id;
        int day = 1;
        if (openDate == null || Month.getMonthId(openDate) < callback.firstMonth) {
          id = callback.firstMonth;
        }
        else {
          id = Month.getMonthId(openDate);
          day = Month.getDay(openDate);
        }

        Double amount = values.get(Account.POSITION_WITH_PENDING);
        if (amount == null){
          amount = values.get(Account.LAST_IMPORT_POSITION, 0.);
        }
        createOpenTransaction(id, day, amount, repository, key);
        Date closeDate = values.get(Account.CLOSED_DATE);
        if (closeDate != null) {
          int closeMonthId = Month.getMonthId(closeDate);
          day = Month.getDay(closeDate);
          if (closeMonthId > callback.lastMonth) {
            closeMonthId = callback.lastMonth;
          }
          createCloseTransaction(repository, key, day, closeMonthId, values.get(Account.CLOSE_POSITION));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (!Account.isUserCreatedAccount(key.get(Account.ID))) {
          return;
        }
        Glob account = repository.get(key);
        if (account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
          return;
        }
        updateAccountTransaction(repository, account);
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
    final FistLastDateChangeSetVisitor visitor = new FistLastDateChangeSetVisitor(repository);
    changeSet.safeVisit(Transaction.TYPE, visitor);
    visitor.updateAccountDates();
    changeSet.safeVisit(Month.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        for (Glob account : repository.getAll(Account.TYPE)) {
          if (Account.isUserCreatedAccount(account)) {
            if (!account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
              updateAccountTransaction(repository, account);
            }
          }
        }
        throw new BreakException();
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  public static Glob createOpenTransaction(int monthId, int day, final Double amount, GlobRepository repository, final Key accountKey) {
    Glob openTransaction = repository.create(Transaction.TYPE,
                                             value(Transaction.ACCOUNT, accountKey.get(Account.ID)),
                                             value(Transaction.LABEL, "open account"),
                                             value(Transaction.AMOUNT, amount),
                                             value(Transaction.ACCOUNT_POSITION, amount),
                                             value(Transaction.POSITION_DAY, day),
                                             value(Transaction.BANK_DAY, day),
                                             value(Transaction.BUDGET_DAY, day),
                                             value(Transaction.DAY, day),
                                             value(Transaction.POSITION_MONTH, monthId),
                                             value(Transaction.BANK_MONTH, monthId),
                                             value(Transaction.MONTH, monthId),
                                             value(Transaction.BUDGET_MONTH, monthId),
                                             value(Transaction.SERIES, Series.ACCOUNT_SERIES_ID),
                                             value(Transaction.TRANSACTION_TYPE,
                                                   TransactionType.OPEN_ACCOUNT_EVENT.getId()));
    repository.update(accountKey, Account.OPEN_TRANSACTION, openTransaction.get(Transaction.ID));
    return openTransaction;
  }

  public static void createCloseTransaction(GlobRepository repository, Key key, int day, int closeMonthId, final Double amount) {
    Glob closeTransaction = repository.create(Transaction.TYPE,
                                              value(Transaction.ACCOUNT, key.get(Account.ID)),
                                              value(Transaction.LABEL, "close account"),
                                              value(Transaction.AMOUNT, amount),
                                              value(Transaction.ACCOUNT_POSITION, 0.),
                                              value(Transaction.POSITION_DAY, day),
                                              value(Transaction.BANK_DAY, day),
                                              value(Transaction.BUDGET_DAY, day),
                                              value(Transaction.DAY, day),
                                              value(Transaction.POSITION_MONTH, closeMonthId),
                                              value(Transaction.BANK_MONTH, closeMonthId),
                                              value(Transaction.BUDGET_MONTH, closeMonthId),
                                              value(Transaction.MONTH, closeMonthId),
                                              value(Transaction.SERIES, Series.ACCOUNT_SERIES_ID),
                                              value(Transaction.TRANSACTION_TYPE, TransactionType.CLOSE_ACCOUNT_EVENT.getId()));
    repository.update(key, Account.CLOSED_TRANSACTION, closeTransaction.get(Transaction.ID));
  }


  private void updateAccountTransaction(GlobRepository repository, Glob account) throws Exception {
    Date openDate = account.get(Account.OPEN_DATE);
    FirstMonthGlobFunctor callback = new FirstMonthGlobFunctor();
    repository.apply(Month.TYPE, GlobMatchers.ALL, callback);

    FirstAndLastDateGlobFunctor firstAndLastDateGlobFunctor = new FirstAndLastDateGlobFunctor(openDate, account.get(Account.CLOSED_DATE));
    repository.apply(Transaction.TYPE,
                     GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)),
                                      TransactionMatchers.realTransactions(account.get(Account.ID))), firstAndLastDateGlobFunctor);

    int monthId;
    int day = 1;
    if (openDate == null || Month.getMonthIdFromFullDate(firstAndLastDateGlobFunctor.first) < callback.firstMonth) {
      monthId = callback.firstMonth;
    }
    else {
      monthId = Month.getMonthIdFromFullDate(firstAndLastDateGlobFunctor.first);
      day = Month.getDayFromFullDate(firstAndLastDateGlobFunctor.first);
    }
    Glob openTransaction = repository.findLinkTarget(account, Account.OPEN_TRANSACTION);
    if (openTransaction != null) {
      shiftTransactionTo(repository, openTransaction, monthId, day, openTransaction.get(Transaction.AMOUNT),
                         openTransaction.get(Transaction.ACCOUNT_POSITION));
    }
    else {
      createOpenTransaction(monthId, day, firstAndLastDateGlobFunctor.amount, repository, account.getKey());
    }
    Date closeDate = account.get(Account.CLOSED_DATE);
    if (closeDate != null) {
      int closeMonthId = Month.getMonthId(closeDate);
      if (closeMonthId > callback.lastMonth) {
        closeMonthId = callback.lastMonth;
        day = Month.getDay(closeDate);
      }
      else {
        closeMonthId = Month.getMonthIdFromFullDate(firstAndLastDateGlobFunctor.last);
        day = Month.getDayFromFullDate(firstAndLastDateGlobFunctor.last);
      }
      Glob closeTransaction = repository.findLinkTarget(account, Account.CLOSED_TRANSACTION);
      if (closeTransaction == null) {
        createCloseTransaction(repository, account.getKey(), day, closeMonthId, account.get(Account.CLOSE_POSITION));
      }
      else if (closeTransaction.get(Transaction.POSITION_MONTH) != closeMonthId || closeTransaction.get(Transaction.POSITION_DAY) != day) {
        shiftTransactionTo(repository, closeTransaction, closeMonthId, day, closeTransaction.get(Transaction.AMOUNT), 0.);
      }
    }
    else {
      Glob closeTransaction = repository.findLinkTarget(account, Account.CLOSED_TRANSACTION);
      if (closeTransaction != null) {
        repository.delete(closeTransaction);
      }
    }
  }


  public static void shiftTransactionTo(GlobRepository repository, Glob transaction, Integer monthId, Integer day,
                                        Double amount, Double accountPosition) {
    repository.update(transaction.getKey(),
                      value(Transaction.AMOUNT, amount),
                      value(Transaction.ACCOUNT_POSITION, accountPosition),
                      value(Transaction.POSITION_DAY, day),
                      value(Transaction.BANK_DAY, day),
                      value(Transaction.BUDGET_DAY, day),
                      value(Transaction.DAY, day),
                      value(Transaction.MONTH, monthId),
                      value(Transaction.POSITION_MONTH, monthId),
                      value(Transaction.BUDGET_MONTH, monthId),
                      value(Transaction.BANK_MONTH, monthId));
  }

  private static class FirstMonthGlobFunctor implements GlobFunctor {
    private int firstMonth;
    private int lastMonth;

    public FirstMonthGlobFunctor() {
      firstMonth = Integer.MAX_VALUE;
      lastMonth = 0;
    }

    public void run(Glob glob, GlobRepository repository) throws Exception {
      Integer id = glob.get(Month.ID);
      if (id < firstMonth) {
        firstMonth = id;
      }
      if (id > lastMonth) {
        lastMonth = id;
      }
    }
  }

  private static class FistLastDateChangeSetVisitor implements ChangeSetVisitor {
    Map<Integer, Integer> firstDates;
    Map<Integer, Integer> lastDates;
    private final GlobRepository repository;

    public FistLastDateChangeSetVisitor(GlobRepository repository) {
      this.repository = repository;
      firstDates = new HashMap<Integer, Integer>();
      lastDates = new HashMap<Integer, Integer>();
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      updateFistAndLastDate(values);
    }

    private void updateFistAndLastDate(FieldValues values) {
      Integer monthId = values.get(Transaction.POSITION_MONTH);
      Integer day = values.get(Transaction.POSITION_DAY);
      int trDate = Month.toFullDate(monthId, day);
      Integer accountId = values.get(Transaction.ACCOUNT);
      if (Account.isUserCreatedAccount(accountId)) {
        Integer firstDate = firstDates.get(accountId);
        if (firstDate == null || trDate < firstDate) {
          {
            firstDates.put(accountId, trDate);
          }
        }
        Integer lastDate = lastDates.get(accountId);
        if (lastDate == null || trDate > lastDate) {
          lastDates.put(accountId, trDate);
        }
      }
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      if (values.contains(Transaction.POSITION_DAY) || values.contains(Transaction.POSITION_MONTH) || values.contains(Transaction.ACCOUNT)) {
        Glob transaction = repository.get(key);
        updateFistAndLastDate(transaction);
      }
    }

    public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
    }

    public void updateAccountDates() {
      for (Map.Entry<Integer, Integer> accountAndFirstDate : firstDates.entrySet()) {
        Glob account = repository.get(Key.create(Account.TYPE, accountAndFirstDate.getKey()));
        if (account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
          continue;
        }
        Glob openTransaction = repository.findLinkTarget(account, Account.OPEN_TRANSACTION);
        int openDate = Month.toFullDate(openTransaction.get(Transaction.POSITION_MONTH), openTransaction.get(Transaction.POSITION_DAY));
        Integer fistDate = accountAndFirstDate.getValue();
        if (openDate > fistDate) {
          shiftTransactionTo(repository, openTransaction, Month.getMonthIdFromFullDate(fistDate),
                             Month.getDayFromFullDate(fistDate),
                             openTransaction.get(Transaction.AMOUNT),
                             openTransaction.get(Transaction.ACCOUNT_POSITION));
//          SortedSet<Glob> sorted = repository.getSorted(Transaction.TYPE, TransactionComparator.ASCENDING_ACCOUNT,
//                                                        GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountAndFirstDate.getKey()),
//                                                                         GlobMatchers.fieldLessOrEqual(Transaction.POSITION_MONTH, openDate)));
//          double amount = 0;
//          for (Glob glob : sorted) {
//            if (glob.get(Transaction.ID).equals(account.get(Account.OPEN_TRANSACTION))) {
//              shiftTransactionTo(repository, glob, Month.getMonthIdFromFullDate(fistDate),
//                                 Month.getDayFromFullDate(fistDate),
//                                 amount + glob.get(Transaction.AMOUNT),
//                                 amount + glob.get(Transaction.AMOUNT));
//              break;
//            }
//            if (!glob.get(Transaction.ID).equals(account.get(Account.CLOSED_TRANSACTION))) {
//              amount += glob.get(Transaction.AMOUNT);
//            }
//          }
//          if (account.get(Account.OPEN_DATE) != null) {
//            repository.update(account.getKey(), Account.OPEN_DATE, Month.toDate(Month.getMonthIdFromFullDate(fistDate),
//                                                                                Month.getDayFromFullDate(fistDate)));
//          }
        }
        Integer lastDate = lastDates.get(accountAndFirstDate.getKey());
        if (account.get(Account.CLOSED_DATE) != null &&
            Month.toFullDate(account.get(Account.CLOSED_DATE)) < lastDate) {
          Glob closeTransaction = repository.findLinkTarget(account, Account.CLOSED_TRANSACTION);
          int monthId = Month.getMonthIdFromFullDate(lastDate);
          int day = Month.getDayFromFullDate(lastDate);
          if (closeTransaction != null) {
            shiftTransactionTo(repository, closeTransaction, monthId,
                               day, closeTransaction.get(Transaction.AMOUNT),
                               0.);
          }
          repository.update(account.getKey(), Account.CLOSED_DATE, Month.toDate(monthId, day));
        }
      }
    }
  }

  private static class FirstAndLastDateGlobFunctor implements GlobFunctor {
    int first = Integer.MAX_VALUE;
    int last = 0;
    Double amount;

    public FirstAndLastDateGlobFunctor(Date openDate, Date closeDate) {
      if (openDate != null) {
        first = Month.toFullDate(openDate);
      }
      if (closeDate != null) {
        last = Month.toFullDate(closeDate);
      }
    }

    public void run(Glob glob, GlobRepository repository) throws Exception {
      int date = Month.toFullDate(glob.get(Transaction.POSITION_MONTH), glob.get(Transaction.POSITION_DAY));
      if (date < first) {
        first = date;
        amount = glob.get(Transaction.ACCOUNT_POSITION, 0) - glob.get(Transaction.AMOUNT, 0);
      }
      if (date > last) {
        last = date;
      }
    }
  }
}
