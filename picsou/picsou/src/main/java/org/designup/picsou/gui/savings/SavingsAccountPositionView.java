package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

public class SavingsAccountPositionView extends View {
  private SelectionService localSelectionService;

  public SavingsAccountPositionView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/savingsAccountPositionView.splits", repository, directory);
    final Directory localDirectory = new DefaultDirectory(directory);
    localSelectionService = new SelectionService();
    localDirectory.add(localSelectionService);
    final GlobMatcher savingsAccountMatcher =
      GlobMatchers.and(
        GlobMatchers.fieldEquals(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()),
        GlobMatchers.not(GlobMatchers.fieldEquals(Account.ID, Account.SAVINGS_SUMMARY_ACCOUNT_ID)));
    builder.addRepeat("repeatAccount", Account.TYPE,
                      savingsAccountMatcher,
                      new RepeatComponentFactory<Glob>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, Glob account) {
                          GlobLabelView accountName = GlobLabelView.init(Account.NAME, repository, localDirectory)
                            .forceSelection(account);
                          cellBuilder.add("accountName", accountName.getComponent());
                          GlobLabelView accountPosition =
                            GlobLabelView.init(SavingsBalanceStat.END_OF_MONTH_POSITION, repository, localDirectory)
                              .setFilter(GlobMatchers.fieldEquals(SavingsBalanceStat.ACCOUNT, account.get(Account.ID)));
                          accountPosition.setName("accountPosition." + account.get(Account.NAME));
                          cellBuilder.add("accountPosition", accountPosition.getComponent());
                        }
                      });
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList selectedMonths = selection.getAll(Month.TYPE);
        GlobList accounts = repository.getAll(Account.TYPE, savingsAccountMatcher);
        selectedMonths.sort(Month.ID);
        Glob lastMonth = selectedMonths.getLast();
        GlobSelectionBuilder selectionBuilder = GlobSelectionBuilder.init();
        for (Glob account : accounts) {
          selectionBuilder.add(
            repository.find(Key.create(SavingsBalanceStat.ACCOUNT, account.get(Account.ID),
                                       SavingsBalanceStat.MONTH, lastMonth.get(Month.ID))));
        }
        localSelectionService.select(selectionBuilder.get());
      }
    }, Month.TYPE);
    parentBuilder.add("savingsAccountPositionView", builder);
  }
}
