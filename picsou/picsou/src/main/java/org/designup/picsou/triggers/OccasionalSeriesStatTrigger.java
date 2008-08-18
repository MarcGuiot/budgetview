package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.OccasionalSeriesStat;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import org.globsframework.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OccasionalSeriesStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    final Map<Integer, Integer> deletedCategoriesMasters = getDeletedCategoriesMasters(changeSet);

    changeSet.safeVisit(Transaction.TYPE, new TransactionChangeVisitor(repository, deletedCategoriesMasters));
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (!changedTypes.contains(Transaction.TYPE)) {
      return;
    }

    repository.deleteAll(OccasionalSeriesStat.TYPE);

    for (Glob transaction : repository.getAll(Transaction.TYPE,
                                              and(fieldEquals(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID),
                                                  fieldEquals(Transaction.PLANNED, false)))) {
      Glob stat =
        repository.findOrCreate(getKey(transaction.get(Transaction.CATEGORY), transaction.get(Transaction.MONTH)));
      updateStat(stat, +1, transaction.get(Transaction.AMOUNT), repository);
    }
  }

  private class TransactionChangeVisitor implements ChangeSetVisitor {
    private final GlobRepository repository;
    private Map<Integer, Integer> deletedCategoriesMasters;

    public TransactionChangeVisitor(GlobRepository repository, Map<Integer, Integer> deletedCategoriesMasters) {
      this.repository = repository;
      this.deletedCategoriesMasters = deletedCategoriesMasters;
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      updateStat(values, +1, repository);
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      if (!values.contains(Transaction.SERIES)
          && !values.contains(Transaction.CATEGORY)
          && !values.contains(Transaction.MONTH)
          && !values.contains(Transaction.AMOUNT)) {
        return;
      }

      Glob transaction = repository.get(key);
      if (Utils.equal(transaction.get(Transaction.TRANSACTION_TYPE), TransactionType.PLANNED.getId())) {
        return;
      }

      Integer previousSeriesId;
      Integer currentSeriesId;
      if (values.contains(Transaction.SERIES)) {
        previousSeriesId = values.getPrevious(Transaction.SERIES);
        currentSeriesId = values.get(Transaction.SERIES);
      }
      else {
        previousSeriesId = transaction.get(Transaction.SERIES);
        currentSeriesId = transaction.get(Transaction.SERIES);
      }

      Integer previousMonthId;
      Integer currentMonthId;
      if (values.contains(Transaction.MONTH)) {
        previousMonthId = values.getPrevious(Transaction.MONTH);
        currentMonthId = values.get(Transaction.MONTH);
      }
      else {
        previousMonthId = transaction.get(Transaction.MONTH);
        currentMonthId = transaction.get(Transaction.MONTH);
      }

      Double previousAmount;
      Double currentAmount;
      if (values.contains(Transaction.AMOUNT)) {
        previousAmount = values.getPrevious(Transaction.AMOUNT);
        currentAmount = values.get(Transaction.AMOUNT);
      }
      else {
        previousAmount = transaction.get(Transaction.AMOUNT);
        currentAmount = transaction.get(Transaction.AMOUNT);
      }

      Integer previousCategoryId;
      Integer currentCategoryId;
      if (values.contains(Transaction.CATEGORY)) {
        previousCategoryId = values.getPrevious(Transaction.CATEGORY);
        currentCategoryId = values.get(Transaction.CATEGORY);
      }
      else {
        previousCategoryId = transaction.get(Transaction.CATEGORY);
        currentCategoryId = transaction.get(Transaction.CATEGORY);
      }
      if (deletedCategoriesMasters.containsKey(previousCategoryId)) {
        previousCategoryId = deletedCategoriesMasters.get(previousCategoryId);
      }
      else {
        previousCategoryId = Category.getMasterCategoryId(previousCategoryId, repository);
      }
      currentCategoryId = Category.getMasterCategoryId(currentCategoryId, repository);

      Glob previousStat = getStat(repository, previousSeriesId, previousCategoryId, previousMonthId);
      if (previousStat != null) {
        updateStat(previousStat, previousCategoryId, previousMonthId, -1, previousAmount, repository);
      }

      Glob currentStat = getOrCreateStat(repository, currentSeriesId, currentCategoryId, currentMonthId);
      if (currentStat != null) {
        repository.update(currentStat.getKey(), OccasionalSeriesStat.AMOUNT,
                          currentStat.get(OccasionalSeriesStat.AMOUNT) + currentAmount);
      }
    }

    public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      updateStat(previousValues, -1, repository);
    }
  }

  private void updateStat(FieldValues values, int multiplier, GlobRepository repository) {
    Integer seriesId = values.get(Transaction.SERIES);
    if (!Series.OCCASIONAL_SERIES_ID.equals(seriesId)) {
      return;
    }

    if (values.get(Transaction.PLANNED)) {
      return;
    }

    Integer categoryId = Category.getMasterCategoryId(values.get(Transaction.CATEGORY), repository);
    Integer monthId = values.get(Transaction.MONTH);
    Glob stat = repository.findOrCreate(getKey(categoryId, monthId));
    Double amount = values.get(Transaction.AMOUNT);

    updateStat(stat, categoryId, monthId, multiplier, amount, repository);
  }

  private void updateStat(Glob stat, Integer categoryId, Integer monthId, int multiplier, Double amount, GlobRepository repository) {
    double newAmount = updateStat(stat, multiplier, amount, repository);

    if (newAmount == 0.0) {
      GlobList transactions = repository.findByIndex(Transaction.MONTH_INDEX, monthId)
        .filterSelf(and(
          fieldEquals(Transaction.CATEGORY, categoryId),
          fieldEquals(Transaction.SERIES, Series.OCCASIONAL_SERIES_ID),
          fieldEquals(Transaction.PLANNED, false)), repository);
      if (transactions.isEmpty()) {
        repository.delete(stat.getKey());
      }
    }
  }

  private double updateStat(Glob stat, int multiplier, Double amount, GlobRepository repository) {
    double newAmount = stat.get(OccasionalSeriesStat.AMOUNT) + multiplier * amount;
    repository.update(stat.getKey(), OccasionalSeriesStat.AMOUNT, newAmount);
    return newAmount;
  }

  private Key getKey(Integer categoryId, Integer monthId) {
    return KeyBuilder.init(OccasionalSeriesStat.MONTH, monthId)
      .set(OccasionalSeriesStat.CATEGORY, categoryId)
      .get();
  }

  private Glob getStat(GlobRepository repository, Integer seriesId, Integer categoryId, Integer monthId) {
    if (!Series.OCCASIONAL_SERIES_ID.equals(seriesId)) {
      return null;
    }
    if (categoryId == null) {
      return null;
    }
    return repository.get(getKey(categoryId, monthId));
  }

  private Glob getOrCreateStat(GlobRepository repository, Integer seriesId, Integer categoryId, Integer monthId) {
    if (!Series.OCCASIONAL_SERIES_ID.equals(seriesId)) {
      return null;
    }
    return repository.findOrCreate(getKey(categoryId, monthId));
  }

  private Map<Integer, Integer> getDeletedCategoriesMasters(ChangeSet changeSet) {
    final Map<Integer, Integer> deletedCategoriesMasters = new HashMap<Integer, Integer>();

    changeSet.safeVisit(
      Category.TYPE,
      new DefaultChangeSetVisitor() {
        public void visitDeletion(Key key, FieldValues values) throws Exception {
          deletedCategoriesMasters.put(key.get(Category.ID), values.get(Category.MASTER));
        }
      });
    return deletedCategoriesMasters;
  }

}
