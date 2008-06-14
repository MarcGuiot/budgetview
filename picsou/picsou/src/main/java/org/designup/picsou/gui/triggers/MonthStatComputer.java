package org.designup.picsou.gui.triggers;

import org.designup.picsou.gui.model.GlobalStat;
import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.gui.utils.FloatingAverage;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.KeyBuilder.init;
import static org.globsframework.model.KeyBuilder.newKey;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.Utils;

import static java.lang.Math.*;
import java.util.*;

public class MonthStatComputer implements ChangeSetListener {
  private static final int FLOATING_AVERAGE_MONTH = 3;

  private GlobRepository repository;

  public MonthStatComputer(GlobRepository repository) {
    this.repository = repository;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsUpdates(Transaction.CATEGORY) ||
        changeSet.containsUpdates(Transaction.DISPENSABLE) ||
        changeSet.containsUpdates(Transaction.AMOUNT) ||
        changeSet.containsUpdates(Transaction.MONTH) ||
        changeSet.containsChanges(Category.TYPE) ||
        changeSet.containsCreationsOrDeletions(Transaction.TYPE)) {
      run();
    }
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
    run();
  }

  public void run() {
    repository.enterBulkDispatchingMode();
    try {
      repository.deleteAll(MonthStat.TYPE, GlobalStat.TYPE);

      SortedSet<Integer> months = new TreeSet<Integer>();

      processTransactions(months);
      if (months.isEmpty()) {
        return;
      }

      int[] monthRange = Month.range(months.first(), months.last());

      createMonths(monthRange);
      addMissingStats(monthRange);
      computeStatsForAll(monthRange);
      computeGlobalStats(monthRange);
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  private void processTransactions(SortedSet<Integer> months) {
    for (Glob transaction : repository.getAll(Transaction.TYPE)) {
      int month = transaction.get(Transaction.MONTH);
      months.add(month);
      Integer categoryId = transaction.get(Transaction.CATEGORY);
      double amount = transaction.get(Transaction.AMOUNT);
      double dispensableAmount = Boolean.TRUE.equals(transaction.get(Transaction.DISPENSABLE)) ? abs(amount) : 0.0;

      updateMonthStat(month, categoryId, Account.SUMMARY_ACCOUNT_ID, amount, dispensableAmount);

      Integer accountId = transaction.get(Transaction.ACCOUNT);
      if (accountId != null) {
        updateMonthStat(month, categoryId, accountId, amount, dispensableAmount);
      }
    }
  }

  private void updateMonthStat(int month, Integer categoryId, Integer accountId, double amount, double dispensableAmount) {
    Key key = getKey(month, categoryId, accountId);
    Glob monthStat = initMonthStat(key);
    GlobUtils.add(key, monthStat, MonthStat.INCOME, amount > 0 ? amount : 0, repository);
    GlobUtils.add(key, monthStat, MonthStat.EXPENSES, amount < 0 ? abs(amount) : 0, repository);
    GlobUtils.add(key, monthStat, MonthStat.DISPENSABLE, dispensableAmount, repository);

    Glob category = repository.get(newKey(Category.TYPE, key.get(MonthStat.CATEGORY)));
    if (!Category.isMaster(category)) {
      updateMonthStat(month, category.get(Category.MASTER), accountId, amount, dispensableAmount);
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

      Map<Integer, FloatingAverage> incomeAverage = new HashMap<Integer, FloatingAverage>();
      Map<Integer, FloatingAverage> expensesAverage = new HashMap<Integer, FloatingAverage>();
      Map<Integer, FloatingAverage> dispensableAverage = new HashMap<Integer, FloatingAverage>();
      for (int month : months) {
        double totalIncome = 0.0;
        double totalExpenses = 0.0;
        double totalDispensable = 0.0;

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

          FloatingAverage averageIncome = getOrCreate(incomeAverage, categoryId);
          FloatingAverage averageExpenses = getOrCreate(expensesAverage, categoryId);
          FloatingAverage averageDispensable = getOrCreate(dispensableAverage, categoryId);

          Key monthStatKey = getKey(month, categoryId, accountId);
          Glob monthStat = repository.get(monthStatKey);
          if (Category.isMaster(category)) {
            totalIncome += monthStat.get(MonthStat.INCOME);
            totalExpenses += monthStat.get(MonthStat.EXPENSES);
            totalDispensable += monthStat.get(MonthStat.DISPENSABLE);
          }

          averageIncome.add(monthStat.get(MonthStat.INCOME));
          averageExpenses.add(monthStat.get(MonthStat.EXPENSES));
          averageDispensable.add(monthStat.get(MonthStat.DISPENSABLE));

          repository.update(monthStatKey, MonthStat.INCOME_AVERAGE, averageIncome.getAverage());
          repository.update(monthStatKey, MonthStat.EXPENSES_AVERAGE, averageExpenses.getAverage());
          repository.update(monthStatKey, MonthStat.DISPENSABLE_AVERAGE, averageExpenses.getAverage());

          if (Category.isMaster(category)) {
            GlobUtils.add(keyForAll,
                          monthStatForAll,
                          MonthStat.DISPENSABLE,
                          monthStat.get(MonthStat.DISPENSABLE),
                          repository);
          }
        }

        FloatingAverage incomeFloatingAverageComputer = getOrCreate(incomeAverage, Category.ALL);
        FloatingAverage expensesFloatingAverageComputer = getOrCreate(expensesAverage, Category.ALL);
        FloatingAverage dispensableFloatingAverageComputer = getOrCreate(dispensableAverage, Category.ALL);
        repository.update(keyForAll, MonthStat.INCOME, totalIncome);
        repository.update(keyForAll, MonthStat.EXPENSES, totalExpenses);

        incomeFloatingAverageComputer.add(totalIncome);
        expensesFloatingAverageComputer.add(totalExpenses);
        dispensableFloatingAverageComputer.add(totalDispensable);

        repository.update(keyForAll, MonthStat.INCOME_AVERAGE, incomeFloatingAverageComputer.getAverage());
        repository.update(keyForAll, MonthStat.EXPENSES_AVERAGE, expensesFloatingAverageComputer.getAverage());
        repository.update(keyForAll, MonthStat.DISPENSABLE_AVERAGE, dispensableFloatingAverageComputer.getAverage());

        for (Glob category : repository.getAll(Category.TYPE)) {
          Key key = getKey(month, category.get(Category.ID), accountId);
          Glob monthStat = repository.get(key);

          double incomePart = computePart(totalIncome, monthStat.get(MonthStat.INCOME));
          repository.update(key, MonthStat.INCOME_PART, incomePart);
          double expensesPart = computePart(totalExpenses, monthStat.get(MonthStat.EXPENSES));
          repository.update(key, MonthStat.EXPENSES_PART, expensesPart);

        }
      }
    }
  }

  private void computeGlobalStats(int[] months) {

    for (Glob category : repository.getAll(Category.TYPE)) {
      double minTotalExpenses = Integer.MAX_VALUE;
      double maxTotalExpenses = 0.0;
      double minTotalIncome = Integer.MAX_VALUE;
      double maxTotalIncome = 0.0;

      Integer categoryId = category.get(Category.ID);

      for (int month : months) {
        Glob monthStat = repository.get(getKey(month, categoryId, Account.SUMMARY_ACCOUNT_ID));
        minTotalIncome = min(minTotalIncome, monthStat.get(MonthStat.INCOME));
        minTotalExpenses = min(minTotalExpenses, monthStat.get(MonthStat.EXPENSES));

        maxTotalIncome = max(maxTotalIncome, monthStat.get(MonthStat.INCOME));
        maxTotalExpenses = max(maxTotalExpenses, monthStat.get(MonthStat.EXPENSES));

      }
      repository.create(GlobalStat.TYPE,
                        FieldValue.value(GlobalStat.CATEGORY, categoryId),
                        FieldValue.value(GlobalStat.MIN_EXPENSES, minTotalExpenses),
                        FieldValue.value(GlobalStat.MAX_EXPENSES, maxTotalExpenses),
                        FieldValue.value(GlobalStat.MAX_INCOME, maxTotalIncome),
                        FieldValue.value(GlobalStat.MIN_INCOME, minTotalIncome));
    }
  }

  private FloatingAverage getOrCreate(Map<Integer, FloatingAverage> categoryIdToAverage, Integer categoryId) {
    FloatingAverage average = categoryIdToAverage.get(categoryId);
    if (average == null) {
      average = FloatingAverage.init(FLOATING_AVERAGE_MONTH);
      categoryIdToAverage.put(categoryId, average);
    }
    return average;
  }

  private double computePart(double totalIncome, Double income) {
    double value = totalIncome != 0 ? income / totalIncome : 0;
    return ((double)round(value * 100)) / 100;
  }

  private Glob initMonthStat(Key monthStatKey) {
    return repository.findOrCreate(monthStatKey);
  }

  private Key getKey(int month, Integer categoryId, int accountId) {
    return
      init(MonthStat.MONTH, month)
        .setValue(MonthStat.CATEGORY, categoryId != null ? categoryId : Category.NONE)
        .setValue(MonthStat.ACCOUNT, accountId)
        .get();
  }
}
