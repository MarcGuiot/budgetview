package org.designup.picsou.triggers.savings;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.Field;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;

import static org.globsframework.model.FieldValue.value;

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

      if (Utils.equal(series.get(Series.TARGET_ACCOUNT), series.get(Series.FROM_ACCOUNT))) {
        localRepository.update(mirror.getKey(), Series.TARGET_ACCOUNT, series.get(Series.TO_ACCOUNT));
      }
      else if (Utils.equal(series.get(Series.TARGET_ACCOUNT), series.get(Series.TO_ACCOUNT))) {
        localRepository.update(mirror.getKey(), Series.TARGET_ACCOUNT, series.get(Series.FROM_ACCOUNT));
      }
      else {
        localRepository.update(series.getKey(), Series.TARGET_ACCOUNT, series.get(Series.FROM_ACCOUNT));
        localRepository.update(mirror.getKey(), Series.TARGET_ACCOUNT, series.get(Series.TO_ACCOUNT));
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
                             localRepository
      );
    for (Glob transaction : transactions) {
      localRepository.update(transaction.getKey(),
                             value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                             value(Transaction.SUB_SERIES, null));
    }
    return transactions;
  }

  public static Integer createMirrorSeries(Key key, GlobRepository localRepository) {
    Glob series = localRepository.find(key);
    if (series == null || !series.get(Series.BUDGET_AREA).equals(BudgetArea.TRANSFER.getId())) {
      return null;
    }

    Glob mirrorSeries = localRepository.findLinkTarget(series, Series.MIRROR_SERIES);
    if (mirrorSeries == null) {
      mirrorSeries = Series.createMirror(series, null, localRepository);
    }

    Integer target = series.get(Series.TARGET_ACCOUNT);
    Integer from = series.get(Series.FROM_ACCOUNT);
    Integer to = series.get(Series.TO_ACCOUNT);
    if (target == null && mirrorSeries.get(Series.TARGET_ACCOUNT) == null) {
      localRepository.update(series.getKey(), Series.TARGET_ACCOUNT, from);
      localRepository.update(mirrorSeries.getKey(), Series.TARGET_ACCOUNT, to);
    }
    else if (target != null) {
      localRepository.update(mirrorSeries.getKey(), Series.TARGET_ACCOUNT, to.equals(target) ? from : to);
    }
    else if (mirrorSeries.get(Series.TARGET_ACCOUNT) != null) {
      localRepository.update(series.getKey(), Series.TARGET_ACCOUNT,
                             mirrorSeries.get(Series.TO_ACCOUNT).equals(mirrorSeries.get(Series.TARGET_ACCOUNT)) ?
                               mirrorSeries.get(Series.FROM_ACCOUNT) : mirrorSeries.get(Series.TO_ACCOUNT)
      );
    }

    return mirrorSeries.get(Series.ID);
  }

}
