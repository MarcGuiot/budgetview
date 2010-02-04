package org.designup.picsou.triggers.savings;

import org.globsframework.model.*;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.GlobIdGenerator;
import org.globsframework.metamodel.Field;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.SeriesBudget;

public class UpdateMirrorSeriesChangeSetVisitor implements ChangeSetVisitor {
  private LocalGlobRepository localRepository;

  public UpdateMirrorSeriesChangeSetVisitor(LocalGlobRepository localRepository) {
    this.localRepository = localRepository;
  }

  public void visitCreation(Key key, FieldValues values) throws Exception {
    createMirrorSeries(key, values, localRepository);
  }

  public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
    if (values.contains(Series.TO_ACCOUNT) || values.contains(Series.FROM_ACCOUNT)) {
      Glob series = localRepository.get(key);

      uncategorize(series.get(Series.ID));
      if (series.get(Series.MIRROR_SERIES) != null && !series.isTrue(Series.IS_MIRROR)) {
        Integer seriesToDelete = series.get(Series.MIRROR_SERIES);
        uncategorize(seriesToDelete);
      }
    }
    else {
      Glob series = localRepository.get(key);
      final Glob mirror = localRepository.findLinkTarget(series, Series.MIRROR_SERIES);
      if (mirror != null && !series.isTrue((Series.IS_MIRROR))) {
        values.safeApply(new FieldValues.Functor() {
          public void process(Field field, Object value) throws Exception {
            localRepository.update(mirror.getKey(), field, value);
          }
        });
      }
    }
  }

  public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
  }

  private GlobList uncategorize(final Integer seriesId) {
    GlobList transactions = localRepository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES,
                                                        seriesId)
      .getGlobs().filterSelf(org.globsframework.model.utils.GlobMatchers.and(org.globsframework.model.utils.GlobMatchers.isFalse(Transaction.PLANNED),
                                 org.globsframework.model.utils.GlobMatchers.isFalse(Transaction.CREATED_BY_SERIES)),
                             localRepository);
    for (Glob transaction : transactions) {
      localRepository.update(transaction.getKey(),
                             org.globsframework.model.FieldValue.value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                             org.globsframework.model.FieldValue.value(Transaction.SUB_SERIES, null));
    }
    return transactions;
  }

  private Integer createMirrorSeries(Key key, FieldValues values, LocalGlobRepository repository) {
    Glob series = localRepository.find(key);
    if (series == null || series.get(Series.MIRROR_SERIES) != null) {
      return null;
    }
    Glob fromAccount = localRepository.findLinkTarget(series, Series.FROM_ACCOUNT);
    Glob toAccount = localRepository.findLinkTarget(series, Series.TO_ACCOUNT);
    if (Account.areBothImported(fromAccount, toAccount)) {
      FieldValue seriesFieldValues[] = values.toArray();
      GlobIdGenerator generator = repository.getIdGenerator();
      int mirrorId = generator.getNextId(Series.ID, 1);
      for (int i = 0; i < seriesFieldValues.length; i++) {
        if (seriesFieldValues[i].getField().equals(Series.IS_MIRROR)) {
          seriesFieldValues[i] = new FieldValue(Series.IS_MIRROR, true);
        }
        else if (seriesFieldValues[i].getField().equals(Series.MIRROR_SERIES)) {
          seriesFieldValues[i] = new FieldValue(Series.MIRROR_SERIES, key.get(Series.ID));
        }
      }
      Glob mirrorSeries = repository.create(Key.create(Series.TYPE, mirrorId), seriesFieldValues);
      repository.update(key, Series.MIRROR_SERIES, mirrorSeries.get(Series.ID));

//      GlobList targetBudgets =
//        repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, mirrorId).getGlobs();

//      ReadOnlyGlobRepository.MultiFieldIndexed sourceBudgets =
//        repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID));
//      for (Glob budget : targetBudgets) {
//        Glob sourceBudget = sourceBudgets.findByIndex(SeriesBudget.MONTH, budget.get(SeriesBudget.MONTH))
//          .getGlobs().getFirst();
//        repository.update(budget.getKey(), SeriesBudget.AMOUNT,
//                          -Math.abs(sourceBudget.get(SeriesBudget.AMOUNT)));
//        repository.update(sourceBudget.getKey(), SeriesBudget.AMOUNT, Math.abs(sourceBudget.get(SeriesBudget.AMOUNT)));
//      }

      return mirrorId;
    }
    return null;
  }

}
