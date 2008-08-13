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
import java.util.*;

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

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
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
                          value(MonthStat.RECEIVED_RECURRING, ZERO),
                          value(MonthStat.RECEIVED_ENVELOP, ZERO),
                          value(MonthStat.RECEIVED_OCCASIONAL, ZERO),
                          value(MonthStat.SPENT_RECURRING, ZERO),
                          value(MonthStat.SPENT_ENVELOP, ZERO),
                          value(MonthStat.SPENT_OCCASIONAL, ZERO),
                          value(MonthStat.INCOME_RECEIVED, ZERO),
                          value(MonthStat.INCOME_SPENT, ZERO),
                          value(MonthStat.TOTAL_RECEIVED, ZERO),
                          value(MonthStat.TOTAL_SPENT, ZERO),
                          value(MonthStat.PLANNED_RECEIVED_RECURRING, ZERO),
                          value(MonthStat.PLANNED_RECEIVED_ENVELOP, ZERO),
                          value(MonthStat.PLANNED_RECEIVED_OCCASIONAL, ZERO),
                          value(MonthStat.PLANNED_SPENT_RECURRING, ZERO),
                          value(MonthStat.PLANNED_SPENT_ENVELOP, ZERO),
                          value(MonthStat.PLANNED_SPENT_OCCASIONAL, ZERO),
                          value(MonthStat.PLANNED_INCOME_RECEIVED, ZERO),
                          value(MonthStat.PLANNED_INCOME_SPENT, ZERO),
                          value(MonthStat.PLANNED_TOTAL_RECEIVED, ZERO),
                          value(MonthStat.PLANNED_TOTAL_SPENT, ZERO));
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
    if (budgetAreaId >= 0) {
      GlobUtils.add(key, monthStat, MonthStat.getReceived(BudgetArea.get(budgetAreaId)), amount > 0 ? amount : 0, repository);
      GlobUtils.add(key, monthStat, MonthStat.getSpent(BudgetArea.get(budgetAreaId)), amount < 0 ? abs(amount) : 0, repository);
    }

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
    if (budgetAreaId >= 0) {
      GlobUtils.add(key, monthStat, MonthStat.getPlannedReceived(BudgetArea.get(budgetAreaId)), amount > 0 ? amount : 0, repository);
      GlobUtils.add(key, monthStat, MonthStat.getPlannedSpent(BudgetArea.get(budgetAreaId)), amount < 0 ? abs(amount) : 0, repository);
    }

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
        double recurringReceived = 0.0;
        double recurringSpent = 0.0;
        double envelopReceived = 0.0;
        double envelopSpent = 0.0;
        double occasionalReceived = 0.0;
        double occasionalSpent = 0.0;
        double incomeReceived = 0.0;
        double incomeSpent = 0.0;

        double plannedTotalReceived = 0.0;
        double plannedTotalSpent = 0.0;
        double plannedRecurringReceived = 0.0;
        double plannedRecurringSpent = 0.0;
        double plannedEnvelopReceived = 0.0;
        double plannedEnvelopSpent = 0.0;
        double plannedOccasionalReceived = 0.0;
        double plannedOccasionalSpent = 0.0;
        double plannedIncomeReceived = 0.0;
        double plannedIncomeSpent = 0.0;

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
            recurringReceived += monthStat.get(MonthStat.RECEIVED_RECURRING);
            envelopReceived += monthStat.get(MonthStat.RECEIVED_ENVELOP);
            occasionalReceived += monthStat.get(MonthStat.RECEIVED_OCCASIONAL);

            totalSpent += monthStat.get(MonthStat.TOTAL_SPENT);
            recurringSpent += monthStat.get(MonthStat.SPENT_RECURRING);
            envelopSpent += monthStat.get(MonthStat.SPENT_ENVELOP);
            occasionalSpent += monthStat.get(MonthStat.SPENT_OCCASIONAL);

            incomeReceived += monthStat.get(MonthStat.INCOME_RECEIVED);
            incomeSpent += monthStat.get(MonthStat.INCOME_SPENT);

            plannedTotalReceived += monthStat.get(MonthStat.PLANNED_TOTAL_RECEIVED);
            plannedRecurringReceived += monthStat.get(MonthStat.PLANNED_RECEIVED_RECURRING);
            plannedEnvelopReceived += monthStat.get(MonthStat.PLANNED_RECEIVED_ENVELOP);
            plannedOccasionalReceived += monthStat.get(MonthStat.PLANNED_RECEIVED_OCCASIONAL);

            plannedTotalSpent += monthStat.get(MonthStat.PLANNED_TOTAL_SPENT);
            plannedRecurringSpent += monthStat.get(MonthStat.PLANNED_SPENT_RECURRING);
            plannedEnvelopSpent += monthStat.get(MonthStat.PLANNED_SPENT_ENVELOP);
            plannedOccasionalSpent += monthStat.get(MonthStat.PLANNED_SPENT_OCCASIONAL);

            plannedIncomeReceived += monthStat.get(MonthStat.PLANNED_INCOME_RECEIVED);
            plannedIncomeSpent += monthStat.get(MonthStat.PLANNED_INCOME_SPENT);
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
                          value(MonthStat.SPENT_RECURRING, recurringSpent),
                          value(MonthStat.SPENT_ENVELOP, envelopSpent),
                          value(MonthStat.SPENT_OCCASIONAL, occasionalSpent),
                          value(MonthStat.RECEIVED_RECURRING, recurringReceived),
                          value(MonthStat.RECEIVED_ENVELOP, envelopReceived),
                          value(MonthStat.RECEIVED_OCCASIONAL, occasionalReceived),
                          value(MonthStat.INCOME_RECEIVED, incomeReceived),
                          value(MonthStat.INCOME_SPENT, incomeSpent),
                          value(MonthStat.PLANNED_TOTAL_RECEIVED, plannedTotalReceived),
                          value(MonthStat.PLANNED_TOTAL_SPENT, plannedTotalSpent),
                          value(MonthStat.PLANNED_SPENT_RECURRING, plannedRecurringSpent),
                          value(MonthStat.PLANNED_SPENT_ENVELOP, plannedEnvelopSpent),
                          value(MonthStat.PLANNED_SPENT_OCCASIONAL, plannedOccasionalSpent),
                          value(MonthStat.PLANNED_RECEIVED_RECURRING, plannedRecurringReceived),
                          value(MonthStat.PLANNED_RECEIVED_ENVELOP, plannedEnvelopReceived),
                          value(MonthStat.PLANNED_RECEIVED_OCCASIONAL, plannedOccasionalReceived),
                          value(MonthStat.PLANNED_INCOME_RECEIVED, plannedIncomeReceived),
                          value(MonthStat.PLANNED_INCOME_SPENT, plannedIncomeSpent)
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
