package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.gui.components.TextDisplay;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.gui.series.EditSeriesAction;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.triggers.SameAccountChecker;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class SavingsView extends View {
  private String name;
  private SeriesEditionDialog seriesEditionDialog;
  private BudgetAreaHeaderUpdater headerUpdater;
  private ChangeSetListener updateChangeSetListener;
  private Set<Integer> selectedMonthIds;

  protected SavingsView(String name, GlobRepository repository, Directory directory, SeriesEditionDialog seriesEditionDialog) {
    super(repository, directory);
    this.name = name;
    this.seriesEditionDialog = seriesEditionDialog;
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectedMonthIds = selection.getAll(Month.TYPE).getValueSet(Month.ID);
        update();
      }
    }, Month.TYPE);

    updateChangeSetListener = new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(SavingsBalanceStat.TYPE)) {
          update();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(SavingsBalanceStat.TYPE)) {
          update();
        }
      }
    };
    repository.addChangeListener(updateChangeSetListener);

  }

  private void update() {
    GlobList balanceStat = repository.getAll(BalanceStat.TYPE,
                                             GlobMatchers.fieldIn(BalanceStat.MONTH, selectedMonthIds));
    headerUpdater.update(balanceStat, BudgetArea.SAVINGS);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/savingsView.splits",
                                                      repository, directory);


    JLabel amountLabel = builder.add("totalObservedAmount", new JLabel());
    JLabel plannedLabel = builder.add("totalPlannedAmount", new JLabel());

    Gauge gauge = new Gauge(true, true);
    builder.add("totalGauge", gauge);

    this.headerUpdater =
      new BudgetAreaHeaderUpdater(TextDisplay.create(amountLabel), TextDisplay.create(plannedLabel), gauge,
                                  repository, directory) {
        protected Double getRemaining(Glob stat, BudgetArea budgetArea) {
          return -super.getRemaining(stat, budgetArea);
        }

        protected Double getPlanned(Glob stat, BudgetArea budgetArea) {
          return -super.getPlanned(stat, budgetArea);
        }

        protected Double getObserved(Glob stat, BudgetArea budgetArea) {
          return -super.getObserved(stat, budgetArea);
        }

        protected void changeGaugeSettings() {
          gauge.setInvertedSignIsAnError(initiallyPlanned > 0);
        }
      };
    this.headerUpdater.setColors("block.total",
                                 "block.total.overrun.error",
                                 "block.total.overrun.positive");


    builder.addRepeat("mainAccounts", Account.TYPE, GlobMatchers.fieldEquals(Account.ID,
                                                                             Account.MAIN_SUMMARY_ACCOUNT_ID),
                      new RepeatComponentFactory<Glob>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, Glob item) {
                          final SavingsSeriesView savingsSeriesView = new SavingsSeriesView(item, repository, directory, seriesEditionDialog);
                          cellBuilder.add("mainAccount", savingsSeriesView.getPanel());
                          cellBuilder.addDisposeListener(new Disposable() {
                            public void dispose() {
                              savingsSeriesView.dispose();
                            }
                          });
                        }
                      });

    builder.addRepeat("savingsAccounts", Account.TYPE,
                      GlobMatchers.and(new AccountGlobMatcher(),
                                       GlobMatchers.not(GlobMatchers.fieldEquals(Account.ID,
                                                                                 Account.MAIN_SUMMARY_ACCOUNT_ID))),
                      new RepeatComponentFactory<Glob>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, Glob item) {
                          final SavingsSeriesView savingsSeriesView = new SavingsSeriesView(item, repository, directory, seriesEditionDialog);
                          cellBuilder.add("savingsAccount", savingsSeriesView.getPanel());
                          cellBuilder.addDisposeListener(new Disposable() {
                            public void dispose() {
                              savingsSeriesView.dispose();
                            }
                          });
                        }
                      });

    builder.add("createSeries", new CreateSeriesAction());

    builder.add("editAllSeries",
                new EditSeriesAction(repository, directory, seriesEditionDialog, BudgetArea.SAVINGS));

    parentBuilder.add(name, builder);
  }

  private class CreateSeriesAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      seriesEditionDialog.showNewSeries(GlobList.EMPTY,
                                        selectionService.getSelection(Month.TYPE),
                                        BudgetArea.SAVINGS);
    }
  }

  private static class AccountGlobMatcher implements GlobMatcher {
    public boolean matches(Glob account, GlobRepository repository) {
      Integer accountId = account.get(Account.ID);
      if (accountId.equals(Account.ALL_SUMMARY_ACCOUNT_ID)) {
        return false;
      }
      if (accountId.equals(Account.SAVINGS_SUMMARY_ACCOUNT_ID)) {
        return false;
      }
      // On ne retourne qu'un compte pour tous les comptes courant
      if (accountId.equals(Account.MAIN_SUMMARY_ACCOUNT_ID)) {
        return true;
      }
      SameAccountChecker mainAccountChecker = SameAccountChecker.getSameAsMain(repository);
      if (mainAccountChecker.isSame(accountId)) {
        return false;
      }
      return true;
    }
  }
}
