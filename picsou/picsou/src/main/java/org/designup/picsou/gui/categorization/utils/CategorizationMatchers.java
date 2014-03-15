package org.designup.picsou.gui.categorization.utils;

import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.gui.utils.MonthMatcher;
import org.designup.picsou.model.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CategorizationMatchers {
  public static CategorizationFilter deferredCardCategorizationFilter() {
    return seriesCategorizationFilter(BudgetArea.OTHER.getId());
  }

  public static CategorizationFilter seriesCategorizationFilter(final Integer budgetAreaId) {
    return new DefaultCategorizationFilter(budgetAreaId);
  }

  public static class DefaultCategorizationFilter implements CategorizationFilter {
    private List<Glob> transactions = Collections.emptyList();
    private MonthMatcher monthFilter;
    private Integer budgetAreaId;

    public DefaultCategorizationFilter(final Integer budgetAreaId) {
      this.budgetAreaId = budgetAreaId;
      monthFilter = Matchers.seriesActiveInPeriod(budgetAreaId, false, true, true);
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

      if (series.get(Series.BUDGET_AREA).equals(BudgetArea.OTHER.getId()) &&
          series.get(Series.FROM_ACCOUNT) != null) {
        return checkInMain(repository);
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

    private boolean checkInMain(GlobRepository repository) {
      for (Glob transaction : transactions) {
        Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
        if (!Account.isMain(account)) {
          return false;
        }
        if (!AccountCardType.NOT_A_CARD.getId().equals(account.get(Account.CARD_TYPE))) {
          return false;
        }
      }
      return true;
    }

    public void filterDates(GlobList transactions) {
      Set<Integer> monthIds = transactions.getValueSet(Transaction.BUDGET_MONTH);
      monthFilter.filterMonths(monthIds);
      this.transactions = transactions;
    }
  }
}
