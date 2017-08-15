package com.budgetview.desktop.utils.datacheck.check;

import com.budgetview.desktop.utils.datacheck.DataCheckReport;
import com.budgetview.model.Account;
import com.budgetview.model.Month;
import com.budgetview.model.Series;
import com.budgetview.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.isNotNull;

public class TransactionCheck {
  public static void transactionIsBetweenSeriesDates(Glob transaction, Integer firstMonthForSeries, Integer lastMonthForSeries, DataCheckReport report) {
    Integer month = transaction.get(Transaction.MONTH);
    if (month < firstMonthForSeries || month > lastMonthForSeries) {
      report.addError("Transaction is not in Series dates " + Month.toString(transaction.get(Transaction.POSITION_MONTH),
                                                                             transaction.get(Transaction.POSITION_DAY)),
                      Transaction.toString(transaction));
    }
  }

  public static void savingsTransactionLinkedToProperAccount(Glob transaction, GlobRepository repository, DataCheckReport report) {
    Glob target = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
    if (Account.isSavings(target)) {
      Glob savingsSeries = repository.findLinkTarget(transaction, Transaction.SERIES);
      if (transaction.get(Transaction.AMOUNT) >= 0) {
        Integer toAccount = savingsSeries.get(Series.TO_ACCOUNT);
        if (toAccount != null && !transaction.get(Transaction.ACCOUNT).equals(toAccount)) {
          report.addFix("Savings transaction badly categorized at: " + Month.toString(transaction.get(Transaction.POSITION_MONTH), transaction.get(Transaction.POSITION_DAY)) + ". Uncategorized : you must recategorize it.",
                        Transaction.toString(transaction));
          Transaction.uncategorize(transaction, repository);
        }
      }
      else {
        Integer fromAccount = savingsSeries.get(Series.FROM_ACCOUNT);
        if (fromAccount != null && !transaction.get(Transaction.ACCOUNT).equals(fromAccount)) {
          report.addFix("Savings transaction badly categorized at: " +
                        Month.toString(transaction.get(Transaction.POSITION_MONTH), transaction.get(Transaction.POSITION_DAY)) +
                        ". Uncategorized : you must recategorize it.",
                        Transaction.toString(transaction));
          Transaction.uncategorize(transaction, repository);
        }
      }
    }
  }

  public static void allTransactionAreLinkedToSeries(GlobRepository repository, DataCheckReport report) {
    Set<Integer> series = repository.getAll(Series.TYPE).getValueSet(Series.ID);
    for (Glob transaction : repository.getAll(Transaction.TYPE, GlobMatchers.linkTargetIsNull(Transaction.SERIES))) {
      if (!series.contains(transaction.get(Transaction.SERIES))) {
        if (transaction.isTrue(Transaction.PLANNED)) {
          repository.delete(transaction);
        }
        else {
          Transaction.uncategorize(transaction, repository);
        }
        report.addFix("Missing series", Transaction.toString(transaction));
      }
    }
  }

  public static void allSplitTransactionsHaveASource(GlobRepository repository, DataCheckReport report) {
    for (Glob transaction : repository.getAll(Transaction.TYPE, isNotNull(Transaction.SPLIT_SOURCE))) {
      if (repository.findLinkTarget(transaction, Transaction.SPLIT_SOURCE) == null) {
        report.addFix("No split source found, transaction deleted", Transaction.toString(transaction));
        repository.delete(transaction);
      }
    }
  }
}
