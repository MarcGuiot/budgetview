package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.impl.ThreeFieldKey;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class MonthStatTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    final Map<Integer, Integer> categoryToMaster = new HashMap<Integer, Integer>();
    changeSet.safeVisit(Category.TYPE, new DefaultChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        GlobList months = repository.getAll(Month.TYPE);
        GlobList accounts = repository.getAll(Account.TYPE);
        for (Glob month : months) {
          for (Glob account : accounts) {
            initMonthStat(getKey(month.get(Month.ID), key.get(Category.ID), account.get(Account.ID)), repository);
          }
        }
      }

      public void visitDeletion(Key key, FieldValues values) throws Exception {
        if (values.get(Category.MASTER) != null) {
          categoryToMaster.put(values.get(Category.ID), values.get(Category.MASTER));
        }
      }
    });
    updateMaster(repository, categoryToMaster);

    changeSet.safeVisit(Month.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        GlobList categories = repository.getAll(Category.TYPE);
        GlobList accounts = repository.getAll(Account.TYPE);
        for (Glob category : categories) {
          for (Glob account : accounts) {
            initMonthStat(getKey(key.get(Month.ID), category.get(Category.ID), account.get(Account.ID)), repository);
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList categories = repository.getAll(Category.TYPE);
        GlobList accounts = repository.getAll(Account.TYPE);
        for (Glob category : categories) {
          for (Glob account : accounts) {
            Key statKey = getKey(key.get(Month.ID), category.get(Category.ID), account.get(Account.ID));
            if (repository.find(statKey) != null) {
              repository.delete(statKey);
            }
          }
        }
      }
    });

    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Double amount = values.get(Transaction.AMOUNT);
        boolean isReceived = amount > 0;
        amount = Math.abs(amount);
        Integer monthId = values.get(Transaction.MONTH);
        Integer categoryId = values.get(Transaction.CATEGORY) == null ? Category.NONE : values.get(Transaction.CATEGORY);
        if (values.get(Transaction.PLANNED, false)) {
          updatePlannedMonthStat(monthId, categoryId,
                                 Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, amount, categoryToMaster, repository);
          updatePlannedMonthStat(monthId, categoryId,
                                 values.get(Transaction.ACCOUNT), isReceived, amount, categoryToMaster, repository);
          if (!MasterCategory.INTERNAL.getId().equals(categoryId)) {
            updatePlannedMonthStat(monthId, Category.ALL, Account.MAIN_SUMMARY_ACCOUNT_ID,
                                   isReceived, amount, categoryToMaster, repository);
//          updatePlannedMonthStat(monthId, Category.ALL, values.get(Transaction.ACCOUNT),
//                                 isReceived, amount, categoryToMaster, repository);

          }
        }
        else {
          updateMonthStat(monthId, categoryId, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived,
                          amount, categoryToMaster, repository);
          updateMonthStat(monthId, categoryId, values.get(Transaction.ACCOUNT),
                          isReceived, amount, categoryToMaster, repository);
          if (!MasterCategory.INTERNAL.getId().equals(categoryId)) {
            updateMonthStat(monthId, Category.ALL, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived,
                            amount, categoryToMaster, repository);

//          updateMonthStat(monthId, Category.ALL, values.get(Transaction.ACCOUNT), isReceived, amount, categoryToMaster, repository);
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob transaction = repository.get(key);
        Boolean hasTransfert = false;
        Integer oldCategoryId;
        Integer newCategoryId;
        if (values.contains(Transaction.CATEGORY)) {
          oldCategoryId = values.getPrevious(Transaction.CATEGORY) == null ? Category.NONE : values.getPrevious(Transaction.CATEGORY);
          newCategoryId = values.get(Transaction.CATEGORY) == null ? Category.NONE : values.get(Transaction.CATEGORY);
          hasTransfert = true;
        }
        else {
          oldCategoryId = newCategoryId = transaction.get(Transaction.CATEGORY) == null ?
                                          Category.NONE : transaction.get(Transaction.CATEGORY);
        }
        Integer newMonth;
        Integer oldMonth;
        if (values.contains(Transaction.MONTH)) {
          oldMonth = values.getPrevious(Transaction.MONTH);
          newMonth = values.get(Transaction.MONTH);
          hasTransfert = true;
        }
        else {
          oldMonth = newMonth = transaction.get(Transaction.MONTH);
        }
        Double newAmount;
        Double oldAmount;
        if (values.contains(Transaction.AMOUNT)) {
          oldAmount = values.getPrevious(Transaction.AMOUNT);
          newAmount = values.get(Transaction.AMOUNT);
        }
        else {
          oldAmount = newAmount = transaction.get(Transaction.AMOUNT);
        }
        boolean isReceived = oldAmount > 0;
        oldAmount = Math.abs(oldAmount);
        newAmount = Math.abs(newAmount);
        if (hasTransfert) {
          if (transaction.get(Transaction.PLANNED, false)) {
            updatePlannedMonthStat(oldMonth, oldCategoryId, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, -oldAmount, categoryToMaster, repository);
            updatePlannedMonthStat(newMonth, newCategoryId, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, newAmount, categoryToMaster, repository);
            updatePlannedMonthStat(oldMonth, oldCategoryId, transaction.get(Transaction.ACCOUNT), isReceived, -oldAmount, categoryToMaster, repository);
            updatePlannedMonthStat(newMonth, newCategoryId, transaction.get(Transaction.ACCOUNT), isReceived, newAmount, categoryToMaster, repository);
          }
          else {
            updateMonthStat(oldMonth, oldCategoryId, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, -oldAmount, categoryToMaster, repository);
            updateMonthStat(newMonth, newCategoryId, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, newAmount, categoryToMaster, repository);
            updateMonthStat(oldMonth, oldCategoryId, transaction.get(Transaction.ACCOUNT), isReceived, -oldAmount, categoryToMaster, repository);
            updateMonthStat(newMonth, newCategoryId, transaction.get(Transaction.ACCOUNT), isReceived, newAmount, categoryToMaster, repository);
          }
        }
        else if (!newAmount.equals(oldAmount)) {
          if (transaction.get(Transaction.PLANNED, false)) {
            updatePlannedMonthStat(newMonth, newCategoryId, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, newAmount - oldAmount, categoryToMaster, repository);
            updatePlannedMonthStat(newMonth, newCategoryId, transaction.get(Transaction.ACCOUNT), isReceived, newAmount - oldAmount, categoryToMaster, repository);
          }
          else {
            updateMonthStat(newMonth, newCategoryId, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, newAmount - oldAmount, categoryToMaster, repository);
            updateMonthStat(newMonth, newCategoryId, transaction.get(Transaction.ACCOUNT), isReceived, newAmount - oldAmount, categoryToMaster, repository);
          }
        }

        if (!newCategoryId.equals(oldCategoryId)) {
          if (newCategoryId.equals(MasterCategory.INTERNAL.getId())) {
            if (transaction.get(Transaction.PLANNED, false)) {
              updatePlannedMonthStat(newMonth, Category.ALL, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, -oldAmount, categoryToMaster, repository);
//              updatePlannedMonthStat(newMonth, Category.ALL, transaction.get(Transaction.ACCOUNT), isReceived, -oldAmount, categoryToMaster, repository);
            }
            else {
              updateMonthStat(newMonth, Category.ALL, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, -oldAmount, categoryToMaster, repository);
//              updateMonthStat(newMonth, Category.ALL, transaction.get(Transaction.ACCOUNT), isReceived, -oldAmount, categoryToMaster, repository);
            }
          }
          else if (oldCategoryId.equals(MasterCategory.INTERNAL.getId())) {
            if (transaction.get(Transaction.PLANNED, false)) {
              updatePlannedMonthStat(newMonth, Category.ALL, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, newAmount, categoryToMaster, repository);
//              updatePlannedMonthStat(newMonth, Category.ALL, transaction.get(Transaction.ACCOUNT), isReceived, newAmount, categoryToMaster, repository);
            }
            else {
              updateMonthStat(newMonth, Category.ALL, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, newAmount, categoryToMaster, repository);
//              updateMonthStat(newMonth, Category.ALL, transaction.get(Transaction.ACCOUNT), isReceived, newAmount, categoryToMaster, repository);
            }
          }
        }
        if (!newAmount.equals(oldAmount)
            && !oldCategoryId.equals(MasterCategory.INTERNAL.getId())
            && !newCategoryId.equals(MasterCategory.INTERNAL.getId())) {
          if (transaction.get(Transaction.PLANNED, false)) {
            updatePlannedMonthStat(newMonth, Category.ALL, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, newAmount - oldAmount, categoryToMaster, repository);
            updatePlannedMonthStat(newMonth, Category.ALL, transaction.get(Transaction.ACCOUNT), isReceived, newAmount - oldAmount, categoryToMaster, repository);
          }
          else {
            updateMonthStat(newMonth, Category.ALL, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived,
                            newAmount - oldAmount, categoryToMaster, repository);
            updateMonthStat(newMonth, Category.ALL, transaction.get(Transaction.ACCOUNT), isReceived, newAmount - oldAmount, categoryToMaster, repository);
          }
        }
      }

      public void visitDeletion(Key key, FieldValues values) throws Exception {
        int newMonth = values.get(Transaction.MONTH);
        if (repository.find(Key.create(Month.TYPE, newMonth)) == null) {
          return;
        }
        Integer categoryId = values.get(Transaction.CATEGORY) == null ? Category.NONE : values.get(Transaction.CATEGORY);
        Integer accountId = values.get(Transaction.ACCOUNT);
        Double amount = values.get(Transaction.AMOUNT);
        boolean isReceived = amount > 0;
        amount = Math.abs(amount);
        if (values.get(Transaction.PLANNED, false)) {
          updatePlannedMonthStat(newMonth, categoryId, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, -amount, categoryToMaster, repository);
          updatePlannedMonthStat(newMonth, categoryId, accountId, isReceived, -amount, categoryToMaster, repository);
          if (!MasterCategory.INTERNAL.getId().equals(categoryId)) {
            updatePlannedMonthStat(newMonth, Category.ALL, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, -amount, categoryToMaster, repository);
          }
          updatePlannedMonthStat(newMonth, Category.ALL, accountId, isReceived, -amount, categoryToMaster, repository);
        }
        else {
          updateMonthStat(newMonth, categoryId, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, -amount, categoryToMaster, repository);
          updateMonthStat(newMonth, categoryId, accountId, isReceived, -amount, categoryToMaster, repository);

          if (!MasterCategory.INTERNAL.getId().equals(categoryId)) {
            updateMonthStat(newMonth, Category.ALL, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, -amount, categoryToMaster, repository);
          }
          updateMonthStat(newMonth, Category.ALL, accountId, isReceived, -amount, categoryToMaster, repository);
        }
      }
    });

    changeSet.safeVisit(Category.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList months = repository.getAll(Month.TYPE);
        GlobList accounts = repository.getAll(Account.TYPE);
        for (Glob month : months) {
          for (Glob account : accounts) {
            Key statKey = getKey(month.get(Month.ID), key.get(Category.ID), account.get(Account.ID));
            if (repository.find(statKey) != null) {
              repository.delete(statKey);
            }
          }
        }
      }
    });
  }

  private void updateMaster(GlobRepository repository, Map<Integer, Integer> categoryToMaster) {
    GlobList categories = repository.getAll(Category.TYPE);
    for (Glob category : categories) {
      if (category.get(Category.MASTER) != null) {
        categoryToMaster.put(category.get(Category.ID), category.get(Category.MASTER));
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    repository.deleteAll(MonthStat.TYPE);
    run(repository);
  }

  public void run(GlobRepository repository) {
    SortedSet<Integer> months = repository.getAll(Month.TYPE).getSortedSet(Month.ID);
    Map<Integer, Integer> master = new HashMap<Integer, Integer>();
    updateMaster(repository, master);
    processTransactions(repository, months, master);
    if (months.isEmpty()) {
      return;
    }

    int[] monthRange = Month.range(months.first(), months.last());
    addMissingStats(monthRange, repository);
    computeStatsForAll(monthRange, repository);
  }


  private void processTransactions(GlobRepository repository, final SortedSet<Integer> months, final Map<Integer, Integer> master) {
    for (Glob transaction : repository.getAll(Transaction.TYPE)) {
      int month = transaction.get(Transaction.MONTH);
      months.add(month);
      Integer categoryId = transaction.get(Transaction.CATEGORY) == null ? Category.NONE : transaction.get(Transaction.CATEGORY);
      double amount = transaction.get(Transaction.AMOUNT);
      boolean isReceived = amount > 0;
      amount = Math.abs(amount);
      if (transaction.get(Transaction.PLANNED, false)) {
        updatePlannedMonthStat(month, categoryId, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, amount, master, repository);
      }
      else {
        updateMonthStat(month, categoryId, Account.MAIN_SUMMARY_ACCOUNT_ID, isReceived, amount, master, repository);
      }

      Integer accountId = transaction.get(Transaction.ACCOUNT);
      if (accountId != null && accountId != -1) {
        if (transaction.get(Transaction.PLANNED)) {
          updatePlannedMonthStat(month, categoryId, accountId, isReceived, amount, master, repository);
        }
        else {
          updateMonthStat(month, categoryId, accountId, isReceived, amount, master, repository);
        }
      }
    }
  }

  private void updateMonthStat(int month, Integer categoryId, Integer accountId, boolean isReceived, double amount,
                               Map<Integer, Integer> categoryToMaster, GlobRepository repository) {
    if (accountId == null) {
      return;
    }
    Key key = getKey(month, categoryId, accountId);
    Glob monthStat = initMonthStat(key, repository);
    if (isReceived) {
      GlobUtils.add(key, monthStat, MonthStat.TOTAL_RECEIVED, amount, repository);
    }
    else {
      GlobUtils.add(key, monthStat, MonthStat.TOTAL_SPENT, amount, repository);
    }

    Integer masterId = categoryToMaster.get(categoryId);
    if (masterId != null) {
      updateMonthStat(month, masterId, accountId, isReceived, amount, categoryToMaster, repository);
    }
  }

  private void updatePlannedMonthStat(int month, Integer categoryId, Integer accountId, boolean isReceived, double amount,
                                      Map<Integer, Integer> categoryToMaster, GlobRepository repository) {
    if (accountId == null) {
      return;
    }
    Key key = getKey(month, categoryId, accountId);
    Glob monthStat = initMonthStat(key, repository);
    if (isReceived) {
      GlobUtils.add(key, monthStat, MonthStat.PLANNED_TOTAL_RECEIVED, amount, repository);
    }
    else {
      GlobUtils.add(key, monthStat, MonthStat.PLANNED_TOTAL_SPENT, amount, repository);
    }

    Integer masterId = categoryToMaster.get(categoryId);
    if (masterId != null) {
      updatePlannedMonthStat(month, masterId, accountId, isReceived, amount, categoryToMaster, repository);
    }
  }

  private void addMissingStats(int[] months, GlobRepository repository) {
    for (int month : months) {
      for (Glob category : repository.getAll(Category.TYPE)) {
        for (Glob account : repository.getAll(Account.TYPE)) {
          Key key = getKey(month, category.get(Category.ID), account.get(Account.ID));
          if (repository.find(key) == null) {
            initMonthStat(key, repository);
          }
        }
      }
    }
  }

  private void computeStatsForAll(int[] months, GlobRepository repository) {
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
              && (Utils.equal(Account.MAIN_SUMMARY_ACCOUNT_ID, accountId))) {
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

  private Glob initMonthStat(Key monthStatKey, GlobRepository repository) {
    return repository.findOrCreate(monthStatKey);
  }

  private Key getKey(int month, Integer categoryId, int accountId) {
    return new ThreeFieldKey(MonthStat.MONTH, month,
                             MonthStat.CATEGORY, categoryId != null ? categoryId : Category.NONE,
                             MonthStat.ACCOUNT, accountId);
  }
}
