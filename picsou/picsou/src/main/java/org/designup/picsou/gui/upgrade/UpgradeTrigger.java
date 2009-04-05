package org.designup.picsou.gui.upgrade;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.fieldIsNull;
import org.globsframework.model.utils.GlobUtils;

import java.util.Set;

public class UpgradeTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {

    Glob version = repository.find(VersionInformation.KEY);
    if (version == null) {
      return;
    }

    final Long currentJarVersion = version.get(VersionInformation.CURRENT_JAR_VERSION);
    if (currentJarVersion.equals(PicsouApplication.JAR_VERSION)) {
      return;
    }

    repository.startChangeSet();
    try {
      if (currentJarVersion <= 4) {
        if (repository.find(Series.OCCASIONAL_SERIES) != null) {
          repository.update(Series.OCCASIONAL_SERIES,
                            value(Series.PROFILE_TYPE, ProfileType.EVERY_MONTH.getId()),
                            value(Series.DEFAULT_CATEGORY, Category.NONE),
                            value(Series.DAY, 1),
                            value(Series.NAME, Series.getOccasionalName()));
        }
      }

      if (currentJarVersion <= 9) {

        repository
          .getAll(SeriesBudget.TYPE, fieldIsNull(SeriesBudget.DAY))
          .safeApply(new GlobFunctor() {
            public void run(Glob seriesBudget, GlobRepository repository) throws Exception {
              final int lastDay = Month.getLastDayNumber(seriesBudget.get(SeriesBudget.MONTH));
              repository.update(seriesBudget.getKey(), value(SeriesBudget.DAY, lastDay));
            }
          }, repository);

        GlobUtils.updateIfExists(repository,
                                 Series.OCCASIONAL_SERIES,
                                 Series.NAME,
                                 Series.getOccasionalName());
        GlobUtils.updateIfExists(repository,
                                 Series.UNCATEGORIZED_SERIES,
                                 Series.NAME,
                                 Series.getUncategorizedName());

        repository.update(Series.UNCATEGORIZED_SERIES, Series.IS_AUTOMATIC, false);

        GlobList uncategorizedTransactions =
          repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID)
            .getGlobs();

        if (!uncategorizedTransactions.isEmpty()) {
          final Integer lastMonthId = uncategorizedTransactions.getSortedSet(Transaction.MONTH).last();
          repository
            .getAll(SeriesBudget.TYPE,
                    GlobMatchers.and(
                      GlobMatchers.fieldEquals(SeriesBudget.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                      GlobMatchers.fieldStrictlyGreaterThan(SeriesBudget.MONTH, lastMonthId)))
            .safeApply(new GlobFunctor() {
              public void run(Glob seriesBudget, GlobRepository repository) throws Exception {
                repository.update(seriesBudget.getKey(), value(SeriesBudget.AMOUNT, 0.00));
              }
            }, repository);
        }

        repository.getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.PLANNED, true))
          .safeApply(new GlobFunctor() {
            public void run(Glob transaction, GlobRepository repository) throws Exception {
              Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
              repository.update(transaction.getKey(), Transaction.LABEL, Transaction.getLabel(true, series));
            }
          }, repository);
      }
      
      repository.update(VersionInformation.KEY, VersionInformation.CURRENT_JAR_VERSION, PicsouApplication.JAR_VERSION);
    }
    finally {
      repository.completeChangeSet();
    }
  }
}
