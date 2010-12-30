package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Pair;
import org.globsframework.utils.Utils;

import java.util.Set;

public class PlannedSeriesStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Key seriesStatKey = createKey(values.get(SeriesBudget.SERIES),
                                      values.get(SeriesBudget.MONTH));
        Glob seriesStat = repository.findOrCreate(seriesStatKey);

        Boolean isActive = values.isTrue(SeriesBudget.ACTIVE);
        Pair<Double, Double> remainingAndOverrun = computeRemainingAndOverrun(values, seriesStat, isActive);
        repository.update(seriesStatKey,
                          FieldValue.value(SeriesStat.PLANNED_AMOUNT,
                                           getActiveAmount(values, isActive)),
                          FieldValue.value(SeriesStat.REMAINING_AMOUNT, remainingAndOverrun.getFirst()),
                          FieldValue.value(SeriesStat.OVERRUN_AMOUNT, remainingAndOverrun.getSecond()));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesBudget.AMOUNT) || values.contains(SeriesBudget.ACTIVE)) {
          updateSeriesStat(repository, repository.get(key));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Key seriesStat = createKey(previousValues.get(SeriesBudget.SERIES),
                                   previousValues.get(SeriesBudget.MONTH));
        Glob glob = repository.find(seriesStat);
        if (glob != null) {
          repository.delete(seriesStat);
        }
      }
    });

    changeSet.safeVisit(SeriesStat.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob seriesBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, key.get(SeriesStat.SERIES))
          .findByIndex(SeriesBudget.MONTH, key.get(SeriesStat.MONTH)).getGlobs().getFirst();
        if (seriesBudget != null) {
          updateSeriesStat(repository, seriesBudget);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private void updateSeriesStat(GlobRepository repository, final Glob seriesBudget) {
    Key seriesStatKey = createKey(seriesBudget.get(SeriesBudget.SERIES),
                                  seriesBudget.get(SeriesBudget.MONTH));
    Glob seriesStat = repository.findOrCreate(seriesStatKey);

    Boolean isActive = seriesBudget.isTrue(SeriesBudget.ACTIVE);
    Pair<Double, Double> remainingAndOverrun = computeRemainingAndOverrun(seriesBudget, seriesStat, isActive);
      repository.update(seriesStatKey,
                        FieldValue.value(SeriesStat.PLANNED_AMOUNT,
                                         getActiveAmount(seriesBudget, isActive)),
                        FieldValue.value(SeriesStat.REMAINING_AMOUNT, remainingAndOverrun.getFirst()),
                        FieldValue.value(SeriesStat.OVERRUN_AMOUNT, remainingAndOverrun.getSecond()));
  }

  // Do not allw autoboxing - amount can be null
  private Double getActiveAmount(FieldValues seriesBudget, Boolean active) {
    return active ? seriesBudget.get(SeriesBudget.AMOUNT) : new Double(0.);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    for (Glob month : repository.getAll(Month.TYPE)) {
      for (Glob series : repository.getAll(Series.TYPE)) {
        repository.findOrCreate(createKey(series.get(Series.ID), month.get(Month.ID)));
      }
    }
    GlobList seriesBudgets = repository.getAll(SeriesBudget.TYPE);

    for (Glob seriesBudget : seriesBudgets) {
      Key seriesStatKey = createKey(seriesBudget.get(SeriesBudget.SERIES),
                                    seriesBudget.get(SeriesBudget.MONTH));
      Glob seriesStat = repository.findOrCreate(seriesStatKey);

      Boolean isActive = seriesBudget.isTrue(SeriesBudget.ACTIVE);
      Pair<Double, Double> remainingAndOverrun = computeRemainingAndOverrun(seriesBudget, seriesStat, isActive);

        repository.update(seriesStatKey,
                          FieldValue.value(SeriesStat.PLANNED_AMOUNT, getActiveAmount(seriesBudget, isActive)),
                          FieldValue.value(SeriesStat.REMAINING_AMOUNT, remainingAndOverrun.getFirst()),
                          FieldValue.value(SeriesStat.OVERRUN_AMOUNT, remainingAndOverrun.getSecond()));
    }
  }

  private Pair<Double, Double> computeRemainingAndOverrun(FieldValues seriesBudget, Glob seriesStat, Boolean isActive) {
    Double plannedAmount = isActive ? seriesBudget.get(SeriesBudget.AMOUNT, 0) : 0.;
    Double obervedAmount = Amounts.zeroIfNull(seriesStat.get(SeriesStat.AMOUNT));
    double remaining = 0;
    double overrun = 0;

    if (plannedAmount > 0) {
      if (obervedAmount > plannedAmount) {
        overrun = obervedAmount - plannedAmount;
      }
      else
//      if (obervedAmount > 0)
      {
        remaining = plannedAmount - obervedAmount;
      }
//      else {
//        overrun = obervedAmount;
//        remaining = plannedAmount;
//      }
    }
    else if (plannedAmount < 0) {
      if (obervedAmount < plannedAmount) {
        overrun = obervedAmount - plannedAmount;
      }
      else
//      if (obervedAmount < 0)
      {
        remaining = plannedAmount - obervedAmount;
      }
//      else {
//        overrun = obervedAmount;
//        remaining = plannedAmount;
//      }
    }
    else {
      overrun = obervedAmount;
    }
    return new Pair<Double, Double>(remaining, overrun);
  }

  private Key createKey(Integer seriesId, Integer monthId) {
    return Key.create(SeriesStat.SERIES, seriesId,
                      SeriesStat.MONTH, monthId);
  }
}
