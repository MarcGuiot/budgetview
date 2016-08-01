package com.budgetview.gui.accounts;

import com.budgetview.gui.model.BudgetStat;
import com.budgetview.gui.signpost.SignpostService;
import com.budgetview.model.Account;
import com.budgetview.model.AccountType;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class MainAccountViewPanel extends AccountViewPanel {

  public static final String HEADER_ID = "accountViewPanel.main.header";

  public MainAccountViewPanel(final GlobRepository repository, final Directory directory) {
    super(repository, directory, createMatcher(), AccountType.MAIN, Account.MAIN_SUMMARY_ACCOUNT_ID);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BudgetStat.TYPE)) {
          updateEstimatedPosition();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(BudgetStat.TYPE)) {
          updateEstimatedPosition();
        }
      }
    });
  }

  public void registerSignpost() {
    directory.get(SignpostService.class).registerComponent(HEADER_ID, header);
  }

  private static GlobMatcher createMatcher() {
    return and(fieldEquals(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()),
               not(fieldEquals(Account.ID, Account.MAIN_SUMMARY_ACCOUNT_ID)),
               not(fieldEquals(Account.ID, Account.ALL_SUMMARY_ACCOUNT_ID)));
  }

  protected AccountType getAccountType() {
    return AccountType.MAIN;
  }
}
