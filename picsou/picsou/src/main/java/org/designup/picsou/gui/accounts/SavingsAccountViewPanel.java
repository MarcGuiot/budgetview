package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Month;
import org.globsframework.gui.views.GlobLabelView;
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

  private JLabel estimatedPositionLabel = new JLabel();
  private JLabel estimatedPositionDateLabel = new JLabel();

  public SavingsAccountViewPanel(final GlobRepository repository, final Directory directory) {
    super(repository, directory, createMatcher(), Account.SAVINGS_SUMMARY_ACCOUNT_ID);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(SavingsBalanceStat.TYPE)) {
          updateEstimatedPosition();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(SavingsBalanceStat.TYPE)) {
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
                             Glob stats = repository.find(Key.create(SavingsBalanceStat.ACCOUNT, accountKey.get(Account.ID),
                                                                     SavingsBalanceStat.MONTH, monthId));
                             if (stats == null) {
                               return "";
                             }
                             return Formatting.toString(stats.get(SavingsBalanceStat.END_OF_MONTH_POSITION));
                           }
                         })
        .setUpdateMatcher(ChangeSetMatchers.changesForType(SavingsBalanceStat.TYPE))
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

  protected void setEstimatedPositionLabels(Double amount, String date) {

    estimatedPositionLabel.setText(Formatting.toString(amount));
    estimatedPositionDateLabel.setText(date);

    boolean shown = amount != null;
    estimatedPositionLabel.setVisible(shown);
    estimatedPositionDateLabel.setVisible(shown);
  }

  protected JComponent getEstimatedPositionComponent() {
    return estimatedPositionLabel;
  }

  protected JComponent getEstimatedPositionDateComponent() {
    return estimatedPositionDateLabel;
  }

  protected boolean showPositionThreshold() {
    return false;
  }

  protected Glob getBalanceStat(Integer lastSelectedMonthId) {
    return repository.find(Key.create(SavingsBalanceStat.ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID,
                                      SavingsBalanceStat.MONTH, lastSelectedMonthId));
  }

  protected Double getEndOfMonthPosition(Glob balanceStat) {
    return balanceStat.get(SavingsBalanceStat.END_OF_MONTH_POSITION);
  }

  protected AccountType getAccountType() {
    return AccountType.SAVINGS;
  }
}
