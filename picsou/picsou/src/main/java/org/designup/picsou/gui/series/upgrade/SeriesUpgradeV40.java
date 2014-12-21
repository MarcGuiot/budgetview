package org.designup.picsou.gui.series.upgrade;

import org.designup.picsou.model.*;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.globsframework.model.*;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobMatchers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import static org.globsframework.model.utils.GlobMatchers.not;

public class SeriesUpgradeV40 {

  public static void updateTargetAccountForSeries(GlobRepository repository) {

    GlobList allSeries = upgradeStandardSeries(repository);

    upgradeSavingsSeries(repository);

// TODO:
//    for (final Glob series : allSeries) {
//      final Integer targetAccount = series.get(Series.TARGET_ACCOUNT);
//      if (targetAccount != null) {
//        repository.safeApply(Transaction.TYPE,
//                             and(fieldEquals(Transaction.PLANNED, true),
//                                 fieldEquals(Transaction.SERIES, series.get(Series.ID))),
//                             new GlobFunctor() {
//                               public void run(Glob glob, GlobRepository repository) throws Exception {
//                                 repository.update(glob.getKey(), Transaction.ACCOUNT, targetAccount);
//                               }
//                             }
//        );
//      }
//    }
  }

  public static GlobList upgradeStandardSeries(GlobRepository repository) {
    GlobList allSeries = repository.getAll(Series.TYPE,
                                           and(not(fieldEquals(Series.BUDGET_AREA, BudgetArea.UNCATEGORIZED.getId())),
                                               not(fieldEquals(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId())),
                                               not(fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()))));
    for (Glob series : allSeries) {
      GlobList transactions =
        repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
        .getGlobs().filter(GlobMatchers.isFalse(Transaction.PLANNED), repository);
      Set<Integer> accounts = transactions.getValueSet(Transaction.ACCOUNT);
      if (accounts.size() == 0) {
        // leave unchanged
      }
      else if (accounts.size() == 1) {
        repository.update(series.getKey(), Series.TARGET_ACCOUNT, accounts.iterator().next());
      }
      else {
        repository.update(series.getKey(), Series.TARGET_ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID);
      }
    }
    return allSeries;
  }

  public static void upgradeSavingsSeries(GlobRepository repository) {
    GlobList allSavingsSeries = repository.getAll(Series.TYPE, fieldEquals(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId()));
    Map<Key, Key> savings = new HashMap<Key, Key>();
    for (Glob series : allSavingsSeries) {
      if (!savings.containsKey(series.getKey())) {
        Glob target = repository.findLinkTarget(series, Series.MIRROR_SERIES);
        if (target != null) {
          savings.put(target.getKey(), series.getKey());
        }
      }
    }

    for (Map.Entry<Key, Key> entry : savings.entrySet()) {
      Glob series1 = repository.get(entry.getKey());
      Glob series2 = repository.get(entry.getValue());
      Set<Integer> accountId1 = updateTargetAccount(repository, series1, repository.findLinkTarget(series1, Series.TARGET_ACCOUNT));
      Set<Integer> accountId2 = updateTargetAccount(repository, series2, repository.findLinkTarget(series2, Series.TARGET_ACCOUNT));
      updateIfNull(repository, series1, series2);
      if (accountId1.size() > 1 || accountId2.size() > 1) {
        // que faire?
      }
    }
  }

  private static void updateIfNull(GlobRepository repository, Glob series1, Glob series2) {
    if (series1.get(Series.TARGET_ACCOUNT) == null || series2.get(Series.TARGET_ACCOUNT) == null) {
      GlobList budget = repository.findLinkedTo(series1, SeriesBudget.SERIES);
      for (Glob glob : budget) {
        if (glob.get(SeriesBudget.PLANNED_AMOUNT, 0.) > 0) {
          repository.update(series1.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.TO_ACCOUNT));
          repository.update(series2.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.FROM_ACCOUNT));
          return;
        }
      }
      budget = repository.findLinkedTo(series2, SeriesBudget.SERIES);
      for (Glob glob : budget) {
        if (glob.get(SeriesBudget.PLANNED_AMOUNT, 0.) > 0) {
          repository.update(series2.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.TO_ACCOUNT));
          repository.update(series1.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.FROM_ACCOUNT));
          return;
        }
      }
      repository.update(series1.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.TO_ACCOUNT));
      repository.update(series2.getKey(), Series.TARGET_ACCOUNT, series1.get(Series.FROM_ACCOUNT));
    }
  }

  private static Set<Integer> updateTargetAccount(GlobRepository repository, Glob series1, Glob targetAccount) {
    Set<Integer> accounts1 = new HashSet<Integer>();
    if (Account.isMain(targetAccount) && !Account.isUserCreatedMainAccount(targetAccount)) {
      GlobList operations1 = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series1.get(Series.ID))
        .getGlobs().filter(GlobMatchers.isFalse(Transaction.PLANNED), repository);
      for (Glob glob : operations1) {
        accounts1.add(glob.get(Transaction.ACCOUNT));
      }
      if (accounts1.size() == 1) {
        repository.update(series1.getKey(), Series.TARGET_ACCOUNT, accounts1.iterator().next());
      }
    }
    return accounts1;
  }
}
