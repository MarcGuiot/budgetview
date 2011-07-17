package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.components.filtering.FilterClearer;
import org.designup.picsou.gui.components.filtering.FilterManager;
import org.designup.picsou.gui.description.AccountComparator;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import static org.globsframework.model.utils.GlobMatchers.not;

public class AccountFilter {
  private GlobRepository repository;

  public static final String ACCOUNT_FILTER = "accounts";
  private SelectionService selectionService;

  public AccountFilter(final FilterManager filterManager, final GlobRepository repository,
                       Directory directory) {
    this.repository = repository;

    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(new GlobSelectionListener() {
                                   public void selectionUpdated(GlobSelection selection) {
                                     filterManager.set(ACCOUNT_FILTER, getCurrentAccountFilter());
                                   }
                                 }, Account.TYPE);

    filterManager.addClearer(new FilterClearer() {
      public List<String> getAssociatedFilters() {
        return Arrays.asList(ACCOUNT_FILTER);
      }

      public void clear() {
        selectionService.select(repository.get(Account.ALL_SUMMARY_KEY));
      }
    });
  }

  public GlobMatcher getCurrentAccountFilter() {
    GlobList selection = selectionService.getSelection(Account.TYPE);
    if (selection.isEmpty()) {
      return GlobMatchers.ALL;
    }
    return Matchers.transactionsForAccounts(selection.getValueSet(Account.ID), repository);
  }
}
