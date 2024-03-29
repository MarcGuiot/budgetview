package com.budgetview.desktop.categorization.utils;

import com.budgetview.desktop.series.utils.SeriesMatchers;
import com.budgetview.desktop.utils.MonthMatcher;
import com.budgetview.model.Account;
import com.budgetview.model.AccountCardType;
import com.budgetview.model.Series;
import com.budgetview.model.Transaction;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;

import java.util.Set;

public class CategorizationMatchers {

  public static CategorizationFilter seriesCategorizationFilter(final BudgetArea budgetArea) {
    return new DefaultCategorizationFilter(budgetArea);
  }

  public static CategorizationFilter deferredCardCategorizationFilter() {
    DefaultCategorizationFilter filter = new DefaultCategorizationFilter(BudgetArea.OTHER);
    filter.excludeCardAccounts = true;
    return filter;
  }

  private static class DefaultCategorizationFilter implements CategorizationFilter {
    private GlobList transactions = new GlobList();
    private MonthMatcher monthFilter;
    private boolean excludeCardAccounts = false;

    public DefaultCategorizationFilter(final BudgetArea budgetArea) {
      monthFilter = SeriesMatchers.seriesActiveInPeriod(budgetArea, false, true, true);
    }

    public void filterForTransactions(GlobList transactions) {
      Set<Integer> monthIds = transactions.getValueSet(Transaction.BUDGET_MONTH);
      this.monthFilter.filterMonths(monthIds);
      this.transactions = transactions;
    }

    public boolean matches(Glob series, GlobRepository repository) {
      if (transactions.isEmpty()) {
        return false;
      }
      if (series.get(Series.ID).equals(Series.ACCOUNT_SERIES_ID)) {
        return false;
      }
      if (!monthFilter.matches(series, repository)) {
        return false;
      }

      if ((series.get(Series.BUDGET_AREA).equals(BudgetArea.OTHER.getId()) && series.get(Series.FROM_ACCOUNT) != null) ||
          Utils.equal(Account.MAIN_SUMMARY_ACCOUNT_ID, series.get(Series.TARGET_ACCOUNT))) {
        return transactionsAreAllInMainAccounts(transactions, repository);
      }

      Glob targetAccount = repository.findLinkTarget(series, Series.TARGET_ACCOUNT);
      if (targetAccount == null) {
        return true;
      }
      for (Glob transaction : transactions) {
        if (!isSameAccount(repository, targetAccount, transaction)) {
          return false;
        }
        if (series.get(Series.TO_ACCOUNT) != null && transaction.get(Transaction.AMOUNT) > 0 &&
            !series.get(Series.TO_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
          return false;
        }
        else if ((series.get(Series.FROM_ACCOUNT) != null) &&
                 (transaction.get(Transaction.AMOUNT) < 0) &&
                 !series.get(Series.FROM_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
          return false;
        }
      }
      return true;
    }

    private boolean isSameAccount(GlobRepository repository, Glob account, Glob transaction) {
      Integer transactionAccount = transaction.get(Transaction.ACCOUNT);
      if (transactionAccount.equals(account.get(Account.ID))) {
        return true;
      }
      Glob target = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
      return target != null && account.get(Account.ID).equals(target.get(Account.DEFERRED_TARGET_ACCOUNT));
    }

    public String toString() {
      return "CategorizationFilter(" + monthFilter + ")";
    }

    private boolean transactionsAreAllInMainAccounts(GlobList transactions, GlobRepository repository) {
      for (Glob transaction : transactions) {
        Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
        if (!Account.isMain(account)) {
          return false;
        }
        if (excludeCardAccounts && !AccountCardType.NOT_A_CARD.getId().equals(account.get(Account.CARD_TYPE))) {
          return false;
        }
      }
      return true;
    }
  }
}
