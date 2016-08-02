package com.budgetview.desktop.accounts.utils;

import com.budgetview.desktop.components.filtering.FilterClearer;
import com.budgetview.desktop.components.filtering.FilterManager;
import com.budgetview.desktop.model.PeriodSeriesStat;
import com.budgetview.desktop.transactions.utils.TransactionMatchers;
import com.budgetview.model.Account;
import com.budgetview.model.AccountCardType;
import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class AccountFilter {

  public static final String ACCOUNT_FILTER = "accounts";
  private SelectionService selectionService;

  public static AccountFilter initForTransactions(final FilterManager filterManager, final GlobRepository repository,
                                                  Directory directory) {
    return new AccountFilter(filterManager, repository, directory, new AccountsToMatcherConverter() {
      public GlobMatcher convert(GlobList selectedAccounts) {
        final Set<Integer> accountIds = selectedAccounts.getValueSet(Account.ID);
        GlobMatcher  matcher = null;
        for (Glob account : selectedAccounts) {
          if (account.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
            matcher = GlobMatchers.or(matcher, GlobMatchers.fieldEquals(Transaction.ORIGINAL_ACCOUNT, account.get(Account.ID)));
          }
        }
        return selectedAccounts.isEmpty() ?
               GlobMatchers.ALL :
               GlobMatchers.or(matcher, TransactionMatchers.transactionsForAccounts(accountIds, repository));
      }
    });
  }

  public static AccountFilter initForPeriodStat(final FilterManager filterManager, final GlobRepository repository,
                                                  Directory directory) {
    return new AccountFilter(filterManager, repository, directory, new AccountsToMatcherConverter() {
      public GlobMatcher convert(GlobList selectedAccounts) {
        return PeriodSeriesStat.statsForAccounts(selectedAccounts.getValueSet(Account.ID));
      }
    });
  }

  private interface AccountsToMatcherConverter {
    GlobMatcher convert(GlobList selectedAccounts);
  }

  private AccountFilter(final FilterManager filterManager, final GlobRepository repository,
                        Directory directory, final AccountsToMatcherConverter converter) {

    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList selectedAccounts = selectionService.getSelection(Account.TYPE);
        GlobMatcher matcher = converter.convert(selectedAccounts);
        if (selectedAccounts.isEmpty()) {
          filterManager.remove(ACCOUNT_FILTER);
        }
        else {
          String label;
          if (selectedAccounts.size() == 1) {
            label = Lang.get("filter.account.one", selectedAccounts.getFirst().get(Account.NAME));
          }
          else {
            label = Lang.get("filter.account.several", selectedAccounts.size());
          }
          filterManager.set(ACCOUNT_FILTER, label, matcher);
        }
      }
    }, Account.TYPE);

    filterManager.addClearer(new FilterClearer() {
      public List<String> getAssociatedFilters() {
        return Arrays.asList(ACCOUNT_FILTER);
      }

      public void clear() {
        selectionService.clear(Account.TYPE);
      }
    });

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsDeletions(Account.TYPE)) {
          filterManager.clear(ACCOUNT_FILTER);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(Account.TYPE)) {
          filterManager.remove(ACCOUNT_FILTER);
          selectionService.clear(Account.TYPE);
        }
      }
    });
  }
}
