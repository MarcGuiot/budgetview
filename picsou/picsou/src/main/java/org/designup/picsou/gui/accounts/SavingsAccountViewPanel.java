package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.budget.SavingsBudgetSummaryView;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Month;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.ChangeSetMatchers;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class SavingsAccountViewPanel extends AccountViewPanel {

  public SavingsAccountViewPanel(final GlobRepository repository, final Directory directory) {
    super(repository, directory, createMatcher(), Account.SAVINGS_SUMMARY_ACCOUNT_ID);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(SavingsBudgetStat.TYPE)) {
          updateEstimatedPosition();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(SavingsBudgetStat.TYPE)) {
          updateEstimatedPosition();
        }
      }
    });
  }

  private static GlobMatcher createMatcher() {
    return and(fieldEquals(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()),
               not(fieldEquals(Account.ID, Account.SAVINGS_SUMMARY_ACCOUNT_ID)),
               not(fieldEquals(Account.ID, Account.ALL_SUMMARY_ACCOUNT_ID)));
  }

  protected void registerSummaryView(GlobsPanelBuilder builder) {
    SavingsBudgetSummaryView summaryView = new SavingsBudgetSummaryView(repository, directory);
    summaryView.registerComponents(builder);
  }

  protected JLabel getEstimatedAccountPositionLabel(final Key accountKey) {
    return getEstimatedAccountPositionLabel(accountKey, repository, directory);
  }

  public static JLabel getEstimatedAccountPositionLabel(final Key accountKey,
                                                        GlobRepository repository,
                                                        Directory directory) {
    GlobLabelView accountPosition =
      GlobLabelView.init(Month.TYPE, repository, directory,
                         new GlobListStringifier() {
                           public String toString(GlobList months, GlobRepository repository) {
                             months.sort(Month.ID);
                             Glob lastMonth = months.getLast();
                             if (lastMonth == null) {
                               return null;
                             }
                             Integer monthId = lastMonth.get(Month.ID);
                             Glob stats = repository.find(Key.create(SavingsBudgetStat.ACCOUNT, accountKey.get(Account.ID),
                                                                     SavingsBudgetStat.MONTH, monthId));
                             if (stats == null) {
                               return "";
                             }
                             return Formatting.toString(stats.get(SavingsBudgetStat.END_OF_MONTH_POSITION));
                           }
                         })
        .setUpdateMatcher(ChangeSetMatchers.changesForType(SavingsBudgetStat.TYPE))
        .setAutoHideIfEmpty(true);
    Glob account = repository.find(accountKey);
    if (account != null) {
      accountPosition.setName("estimatedAccountPosition." + account.get(Account.NAME));
    }
    return accountPosition.getComponent();
  }

  protected JLabel getEstimatedAccountPositionDateLabel(final Key accountKey) {
    return getEstimatedAccountPositionDateLabel(accountKey, repository, directory);
  }

  public static JLabel getEstimatedAccountPositionDateLabel(Key accountKey,
                                                            GlobRepository repository,
                                                            Directory directory) {
    GlobLabelView accountPosition =
      GlobLabelView.init(Month.TYPE, repository, directory,
                         new GlobListStringifier() {
                           public String toString(GlobList months, GlobRepository repository) {
                             months.sort(Month.ID);
                             Glob lastMonth = months.getLast();
                             if (lastMonth == null) {
                               return null;
                             }
                             Integer monthId = lastMonth.get(Month.ID);
                             return Formatting.toString(Month.getLastDay(monthId));
                           }
                         });
    Glob account = repository.find(accountKey);
    if (account != null) {
      accountPosition.setName("estimatedAccountPositionDate." + account.get(Account.NAME));
    }
    return accountPosition.getComponent();
  }

  protected AccountType getAccountType() {
    return AccountType.SAVINGS;
  }

  protected boolean showPositionThreshold() {
    return false;
  }
}
