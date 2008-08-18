package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.impl.ThreeFieldKey;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.Utils;

import static java.lang.Math.abs;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

public class MonthStatTrigger implements ChangeSetListener {
  private GlobRepository repository;

  public MonthStatTrigger(GlobRepository repository) {
    this.repository = repository;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsUpdates(Transaction.CATEGORY) ||
        changeSet.containsUpdates(Transaction.DISPENSABLE) ||
        changeSet.containsUpdates(Transaction.AMOUNT) ||
        changeSet.containsUpdates(Transaction.MONTH) ||
        changeSet.containsUpdates(Transaction.SERIES) ||
        changeSet.containsCreationsOrDeletions(Transaction.TYPE) ||
        changeSet.containsChanges(Category.TYPE) ||
        changeSet.containsCreationsOrDeletions(Month.TYPE)) {
      Set<Key> keySet = changeSet.getDeleted(Category.TYPE);
      Set<Integer> categoryToDelete = new HashSet<Integer>();
      for (Key key : keySet) {
        categoryToDelete.add(key.get(Category.ID));
      }
      run(categoryToDelete);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    repository.enterBulkDispatchingMode();
    try {
      repository.deleteAll(MonthStat.TYPE);
      run(Collections.<Integer>emptySet());
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  public void run(Set<Integer> categoryToDelete) {
    repository.enterBulkDispatchingMode();
    try {

      updateToZero(categoryToDelete);
      SortedSet<Integer> months = repository.getAll(Month.TYPE).getSortedSet(Month.ID);
      processTransactions(months);
      if (months.isEmpty()) {
        return;
      }

      int[] monthRange = Month.range(months.first(), months.last());

      createMonths(monthRange);
      addMissingStats(monthRange);
      computeStatsForAll(monthRange);
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  private void updateToZero(Set<Integer> categoryToDelete) {
    GlobList list = repository.getAll(MonthStat.TYPE);
    Double ZERO = 0.0;
    for (Glob glob : list) {
      Key key = glob.getKey();
      if (categoryToDelete.contains(key.get(MonthStat.CATEGORY))) {
        repository.delete(key);
      }
      else {
        repository.update(key,
                          value(MonthStat.DISPENSABLE, ZERO),
                          value(MonthStat.TOTAL_RECEIVED, ZERO),
                          value(MonthStat.TOTAL_SPENT, ZERO));
      }
    }
  }

  private void processTransactions(SortedSet<Integer> months) {
    for (Glob transaction : repository.getAll(Transaction.TYPE)) {
      int month = transaction.get(Transaction.MONTH);
      months.add(month);
      Integer categoryId = transaction.get(Transaction.CATEGORY);
      double amount = transaction.get(Transaction.AMOUNT);
      double dispensableAmount = Boolean.TRUE.equals(transaction.get(Transaction.DISPENSABLE)) ? abs(amount) : 0.0;
      Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
      Integer budgetArea = -1;
      if (series != null) {
        budgetArea = series.get(Series.BUDGET_AREA);
      }
      if (transaction.get(Transaction.PLANNED, false)) {
        updatePlannedMonthStat(month, categoryId, budgetArea, Account.SUMMARY_ACCOUNT_ID, amount);
      }
      else {
        updateMonthStat(month, categoryId, budgetArea, Account.SUMMARY_ACCOUNT_ID, amount, dispensableAmount);
      }

      Integer accountId = transaction.get(Transaction.ACCOUNT);
      if (accountId != null && accountId != -1) {
        if (transaction.get(Transaction.PLANNED)) {
          updatePlannedMonthStat(month, categoryId, budgetArea, accountId, amount);
        }
        else {
          updateMonthStat(month, categoryId, budgetArea, accountId, amount, dispensableAmount);
        }
      }
    }
  }

  private void updateMonthStat(int month, Integer categoryId, Integer budgetAreaId,
                               Integer accountId, double amount, double dispensableAmount) {
    Key key = getKey(month, categoryId, accountId);
    Glob monthStat = initMonthStat(key);
    GlobUtils.add(key, monthStat, MonthStat.TOTAL_RECEIVED, amount > 0 ? amount : 0, repository);
    GlobUtils.add(key, monthStat, MonthStat.TOTAL_SPENT, amount < 0 ? abs(amount) : 0, repository);
    GlobUtils.add(key, monthStat, MonthStat.DISPENSABLE, dispensableAmount, repository);

    Glob category = repository.get(Key.create(Category.TYPE, key.get(MonthStat.CATEGORY)));
    if (!Category.isMaster(category)) {
      updateMonthStat(month, category.get(Category.MASTER), budgetAreaId, accountId, amount, dispensableAmount);
    }
  }

  private void updatePlannedMonthStat(int month, Integer categoryId, Integer budgetAreaId,
                                      Integer accountId, double amount) {
    Key key = getKey(month, categoryId, accountId);
    Glob monthStat = initMonthStat(key);
    GlobUtils.add(key, monthStat, MonthStat.PLANNED_TOTAL_RECEIVED, amount > 0 ? amount : 0, repository);
    GlobUtils.add(key, monthStat, MonthStat.PLANNED_TOTAL_SPENT, amount < 0 ? abs(amount) : 0, repository);
    Glob category = repository.get(Key.create(Category.TYPE, key.get(MonthStat.CATEGORY)));
    if (!Category.isMaster(category)) {
      updatePlannedMonthStat(month, category.get(Category.MASTER), budgetAreaId, accountId, amount);
    }
  }

  private void createMonths(int[] monthRange) {
    for (int month : monthRange) {
      repository.findOrCreate(Key.create(Month.TYPE, month));
    }
  }

  private void addMissingStats(int[] months) {
    for (int month : months) {
      for (Glob category : repository.getAll(Category.TYPE)) {
        for (Glob account : repository.getAll(Account.TYPE)) {
          Key key = getKey(month, category.get(Category.ID), account.get(Account.ID));
          if (repository.find(key) == null) {
            initMonthStat(key);
          }
        }
      }
    }
  }

  private void computeStatsForAll(int[] months) {
    for (Glob account : repository.getAll(Account.TYPE)) {
      int accountId = account.get(Account.ID);

      for (int month : months) {
        double totalReceived = 0.0;
        double totalSpent = 0.0;
        double plannedTotalReceived = 0.0;
        double plannedTotalSpent = 0.0;

        Key keyForAll = getKey(month, Category.ALL, accountId);
        Glob monthStatForAll = repository.get(keyForAll);

        for (Glob category : repository.getAll(Category.TYPE)) {
          Integer categoryId = category.get(Category.ID);
          if (Category.ALL.equals(categoryId)) {
            continue;
          }

          if (categoryId.equals(MasterCategory.INTERNAL.getId())
              && (Utils.equal(Account.SUMMARY_ACCOUNT_ID, accountId))) {
            continue;
          }

          Key monthStatKey = getKey(month, categoryId, accountId);
          Glob monthStat = repository.get(monthStatKey);
          if (Category.isMaster(category)) {
            totalReceived += monthStat.get(MonthStat.TOTAL_RECEIVED);
            totalSpent += monthStat.get(MonthStat.TOTAL_SPENT);
            plannedTotalReceived += monthStat.get(MonthStat.PLANNED_TOTAL_RECEIVED);
            plannedTotalSpent += monthStat.get(MonthStat.PLANNED_TOTAL_SPENT);
          }

          if (Category.isMaster(category)) {
            GlobUtils.add(keyForAll,
                          monthStatForAll,
                          MonthStat.DISPENSABLE,
                          monthStat.get(MonthStat.DISPENSABLE),
                          repository);
          }
        }

        repository.update(keyForAll,
                          value(MonthStat.TOTAL_RECEIVED, totalReceived),
                          value(MonthStat.TOTAL_SPENT, totalSpent),
                          value(MonthStat.PLANNED_TOTAL_RECEIVED, plannedTotalReceived),
                          value(MonthStat.PLANNED_TOTAL_SPENT, plannedTotalSpent)
        );
      }
    }
  }

  private Glob initMonthStat(Key monthStatKey) {
    return repository.findOrCreate(monthStatKey);
  }

  private Key getKey(int month, Integer categoryId, int accountId) {
    return new ThreeFieldKey(MonthStat.MONTH, month,
                             MonthStat.CATEGORY, categoryId != null ? categoryId : Category.NONE,
                             MonthStat.ACCOUNT, accountId);
  }
}
