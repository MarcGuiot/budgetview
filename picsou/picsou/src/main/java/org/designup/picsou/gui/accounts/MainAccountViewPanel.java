package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.budget.summary.BudgetSummaryView;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.*;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class MainAccountViewPanel extends AccountViewPanel {

  private JLabel estimatedPositionDateLabel = new JLabel();

  public MainAccountViewPanel(final GlobRepository repository, final Directory directory) {
    super(repository, directory, createMatcher(), Account.MAIN_SUMMARY_ACCOUNT_ID);



    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BudgetStat.TYPE) ||
            changeSet.containsUpdates(AccountPositionThreshold.THRESHOLD)) {
          updateEstimatedPosition();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(BudgetStat.TYPE) ||
            changedTypes.contains(AccountPositionThreshold.TYPE)) {
          updateEstimatedPosition();
        }
      }
    });
  }

  private static GlobMatcher createMatcher() {
    return and(fieldEquals(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()),
               not(fieldEquals(Account.ID, Account.MAIN_SUMMARY_ACCOUNT_ID)),
               not(fieldEquals(Account.ID, Account.ALL_SUMMARY_ACCOUNT_ID)));
  }

  protected JLabel getEstimatedAccountPositionLabel(Key accountKey) {
    return Gui.createInvisibleLabel();
  }

  protected JLabel getEstimatedAccountPositionDateLabel(Key accountKey) {
    return Gui.createInvisibleLabel();
  }

  protected boolean showPositionThreshold() {
    return true;
  }

  protected AccountType getAccountType() {
    return AccountType.MAIN;
  }

  protected void registerSummaryView(GlobsPanelBuilder builder) {
    BudgetSummaryView view = new BudgetSummaryView(repository, directory);
    view.registerComponents(builder);
  }
}
