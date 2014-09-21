package org.designup.picsou.triggers.savings;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.Field;
import org.globsframework.model.*;
import org.globsframework.model.repository.GlobIdGenerator;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;

public class UpdateMirrorSeriesChangeSetVisitor implements ChangeSetVisitor {
  private GlobRepository localRepository;

  public UpdateMirrorSeriesChangeSetVisitor(GlobRepository localRepository) {
    this.localRepository = localRepository;
  }

  public void visitCreation(Key key, FieldValues values) throws Exception {
    createMirrorSeries(key, localRepository);
  }

  public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
    if (values.contains(Series.TO_ACCOUNT) || values.contains(Series.FROM_ACCOUNT)) {
      Glob series = localRepository.get(key);

      uncategorize(series.get(Series.ID));
      if (series.get(Series.MIRROR_SERIES) != null) {
        Integer seriesToDelete = series.get(Series.MIRROR_SERIES);
        uncategorize(seriesToDelete);
      }
    }
    {
      Glob series = localRepository.get(key);
      final Glob mirror = localRepository.findLinkTarget(series, Series.MIRROR_SERIES);
      if (mirror != null) {
        values.safeApply(new FieldValues.Functor() {
          public void process(Field field, Object value) throws Exception {
            if (field != Series.MIRROR_SERIES && field != Series.TARGET_ACCOUNT) {
              localRepository.update(mirror.getKey(), field, value);
            }
          }
        });
        if (values.contains(Series.FROM_ACCOUNT) && values.getPrevious(Series.FROM_ACCOUNT) != null) {
          if (values.getPrevious(Series.FROM_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
            localRepository.update(series.getKey(), Series.TARGET_ACCOUNT, series.get(Series.FROM_ACCOUNT));
          }
          else {
            localRepository.update(mirror.getKey(), Series.TARGET_ACCOUNT, values.get(Series.FROM_ACCOUNT));
          }
          localRepository.update(mirror.getKey(), Series.FROM_ACCOUNT, values.get(Series.FROM_ACCOUNT));
        }
        if (values.contains(Series.TO_ACCOUNT) && values.getPrevious(Series.TO_ACCOUNT) != null) {
          if (values.getPrevious(Series.TO_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
            localRepository.update(series.getKey(), Series.TARGET_ACCOUNT, series.get(Series.TO_ACCOUNT));
          }
          else {
            localRepository.update(mirror.getKey(), Series.TARGET_ACCOUNT, values.get(Series.TO_ACCOUNT));
          }
          localRepository.update(mirror.getKey(), Series.TO_ACCOUNT, values.get(Series.TO_ACCOUNT));
        }
      }
    }
  }

  public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
  }

  private GlobList uncategorize(final Integer seriesId) {
    GlobList transactions = localRepository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES,
                                                        seriesId)
      .getGlobs().filterSelf(GlobMatchers.and(GlobMatchers.isFalse(Transaction.PLANNED),
                                              GlobMatchers.isFalse(Transaction.CREATED_BY_SERIES)),
                             localRepository);
    for (Glob transaction : transactions) {
      localRepository.update(transaction.getKey(),
                             FieldValue.value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                             FieldValue.value(Transaction.SUB_SERIES, null));
    }
    return transactions;
  }

  public static Integer createMirrorSeries(Key key, GlobRepository localRepository) {
    Glob series = localRepository.find(key);
    if (series == null || series.get(Series.MIRROR_SERIES) != null ||
        !series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())) {
      return null;
    }

    FieldValue seriesFieldValues[] = series.toArray();
    GlobIdGenerator generator = localRepository.getIdGenerator();
    int newSeriesId = generator.getNextId(Series.ID, 1);
    for (int i = 0; i < seriesFieldValues.length; i++) {
      if (seriesFieldValues[i].getField().equals(Series.MIRROR_SERIES)) {
        seriesFieldValues[i] = new FieldValue(Series.MIRROR_SERIES, key.get(Series.ID));
      }
    }
    Glob mirrorSeries = localRepository.create(Key.create(Series.TYPE, newSeriesId), seriesFieldValues);
    localRepository.update(key, Series.MIRROR_SERIES, mirrorSeries.get(Series.ID));
//    FindMirrorGlobFunctor globFunctor = new FindMirrorGlobFunctor();
//    localRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, key.get(Series.ID))
//      .saveApply(globFunctor, localRepository);
//    localRepository.update(key, Series.TARGET_ACCOUNT,
//                           globFunctor.from ? series.get(Series.FROM_ACCOUNT) : series.get(Series.TO_ACCOUNT));
    localRepository.update(mirrorSeries.getKey(), Series.TARGET_ACCOUNT,
                           series.get(Series.TO_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT)) ? series.get(Series.FROM_ACCOUNT)
                                                                                              : series.get(Series.TO_ACCOUNT));
    return newSeriesId;
  }

  private static class FindMirrorGlobFunctor implements GlobFunctor {
    private boolean from = false;

    public void run(Glob budget, GlobRepository repository) throws Exception {
      if (budget.get(SeriesBudget.PLANNED_AMOUNT, 0) < 0) {
        from = true;
      }
    }
  }
}
