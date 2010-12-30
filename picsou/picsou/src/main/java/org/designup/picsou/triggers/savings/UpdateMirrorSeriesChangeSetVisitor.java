package org.designup.picsou.triggers.savings;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.Field;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobIdGenerator;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.GlobMatchers;

public class UpdateMirrorSeriesChangeSetVisitor implements ChangeSetVisitor {
  private LocalGlobRepository localRepository;

  public UpdateMirrorSeriesChangeSetVisitor(LocalGlobRepository localRepository) {
    this.localRepository = localRepository;
  }

  public void visitCreation(Key key, FieldValues values) throws Exception {
    createMirrorSeries(key, localRepository);
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
    {
      Glob series = localRepository.get(key);
      final Glob mirror = localRepository.findLinkTarget(series, Series.MIRROR_SERIES);
      if (mirror != null && !series.isTrue((Series.IS_MIRROR))) {
        values.safeApply(new FieldValues.Functor() {
          public void process(Field field, Object value) throws Exception {
            if (field != Series.IS_MIRROR && field != Series.MIRROR_SERIES){
              localRepository.update(mirror.getKey(), field, value);
            }
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

  public static Integer createMirrorSeries(Key key, LocalGlobRepository localRepository) {
    Glob series = localRepository.find(key);
    if (series == null || series.get(Series.MIRROR_SERIES) != null) {
      return null;
    }

    Glob fromAccount = localRepository.findLinkTarget(series, Series.FROM_ACCOUNT);
    Glob toAccount = localRepository.findLinkTarget(series, Series.TO_ACCOUNT);
    if (Account.areBothImported(fromAccount, toAccount)) {
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
      FindMirrorGlobFunctor globFunctor = new FindMirrorGlobFunctor();
      localRepository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, key.get(Series.ID))
        .saveApply(globFunctor, localRepository);
      localRepository.update(key, Series.IS_MIRROR, globFunctor.from);
      localRepository.update(mirrorSeries.getKey(), Series.IS_MIRROR, !globFunctor.from);
      createSerieBudget(key, mirrorSeries.getKey(), localRepository);
      return newSeriesId;
    }
    return null;
  }

  private static void createSerieBudget(Key existingSeries, Key newSeries, LocalGlobRepository repository) {
    GlobList seriesBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES,
                                                   existingSeries.get(Series.ID)).getGlobs();
    for (Glob glob : seriesBudget) {
      repository.create(SeriesBudget.TYPE,
                        FieldValue.value(SeriesBudget.AMOUNT, -glob.get(SeriesBudget.AMOUNT, 0)),
                        FieldValue.value(SeriesBudget.ACTIVE, glob.get(SeriesBudget.ACTIVE)),
                        FieldValue.value(SeriesBudget.MONTH, glob.get(SeriesBudget.MONTH)),
                        FieldValue.value(SeriesBudget.SERIES, newSeries.get(Series.ID)),
                        FieldValue.value(SeriesBudget.DAY, glob.get(SeriesBudget.DAY)));
    }
  }

  private static class FindMirrorGlobFunctor implements GlobFunctor {
    private boolean from = false;

    public void run(Glob glob, GlobRepository repository) throws Exception {
      if (glob.get(SeriesBudget.AMOUNT, 0) < 0) {
        from = true;
      }
    }
  }
}
