package com.budgetview.desktop.utils.datacheck.check;

import com.budgetview.desktop.utils.datacheck.DataCheckReport;
import com.budgetview.model.Account;
import com.budgetview.model.CurrentMonth;
import com.budgetview.model.Month;
import com.budgetview.model.Transaction;
import com.budgetview.shared.model.AccountType;
import com.budgetview.utils.TransactionComparator;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Dates;

import java.util.Date;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class AccountCheck {
  public static void accountTotalAlignedWithTransactions(GlobRepository repository, DataCheckReport report) {
    Glob mainSummaryAccount = repository.getAll(Account.TYPE, fieldEquals(Account.ID, Account.MAIN_SUMMARY_ACCOUNT_ID)).getFirst();

    Date mainPosDate = mainSummaryAccount.get(Account.POSITION_DATE);
    GlobList allMainAcounts = repository.getAll(Account.TYPE, fieldEquals(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()));
    for (Glob mainAccount : allMainAcounts) {
      Date date = mainAccount.get(Account.POSITION_DATE);
      if (date != null && mainPosDate.before(date)) {
        report.addError("Main summary date is " + Dates.toString(mainPosDate) +
                        " but is composed with date " + Dates.toString(date),
                        Account.toString(mainAccount));

      }
    }

    TransactionComparator comparator = TransactionComparator.ASCENDING_ACCOUNT;

    Glob[] transactions = repository.getSorted(Transaction.TYPE, comparator, GlobMatchers.ALL);
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    Date lastTransactionDate = Month.toDate(currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH),
                                            currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY));
    for (Glob transaction : transactions) {
      Integer accountId = transaction.get(Transaction.ACCOUNT);
      Date positionDate = Month.toDate(transaction.get(Transaction.POSITION_MONTH), transaction.get(Transaction.POSITION_DAY));
      if (transaction.isTrue(Transaction.PLANNED)) {
        if (positionDate.before(lastTransactionDate)) {
          report.addError("Planned before current date " + Dates.toString(positionDate) + " / " +
                          Dates.toString(lastTransactionDate),
                          Transaction.toString(transaction));
        }
      }
      else {
        if (Account.SUMMARY_ACCOUNT_IDS.contains(transaction.get(Transaction.ACCOUNT))) {
          report.addError("Transaction in summary account", Transaction.toString(transaction));
        }
        if (positionDate.after(lastTransactionDate)) {
          report.addError("Current position date before last transaction " + Dates.toString(positionDate) + " / " +
                          Dates.toString(lastTransactionDate),
                          Transaction.toString(transaction));
        }
        Date bankDate = Month.toDate(transaction.get(Transaction.POSITION_MONTH), transaction.get(Transaction.POSITION_DAY));
        if (bankDate.after(lastTransactionDate)) {
          report.addError("Current bank date before last transaction " + Dates.toString(positionDate) + " / " +
                          Dates.toString(lastTransactionDate),
                          Transaction.toString(transaction));
        }
        if (accountId == Account.MAIN_SUMMARY_ACCOUNT_ID) {
          report.addError("Main summary contains transactions", Transaction.toString(transaction));
        }
      }
    }
  }
}
