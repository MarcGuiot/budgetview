package com.budgetview.desktop.utils.datacheck.check;

import com.budgetview.desktop.utils.datacheck.DataCheckReport;
import com.budgetview.model.Month;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesBudget;
import com.budgetview.triggers.MonthsToSeriesBudgetTrigger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.ReadOnlyGlobRepository;
import org.globsframework.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class SeriesCheck {

  public static void allSeriesMirrorsAreProperlySet(GlobRepository repository, DataCheckReport report) {
    for (Glob series : repository.getAll(Series.TYPE)) {
      Integer mirrorSeriesId = series.get(Series.MIRROR_SERIES);
      if (mirrorSeriesId != null && repository.findLinkTarget(series, Series.MIRROR_SERIES) == null) {
        Integer mirrorAccountId = getMirrorAccount(series);
        if (mirrorAccountId != null) {
          report.addFix("Mirror series " + mirrorSeriesId + " does not exist - creating it", Series.toString(series));
          Series.createMirror(series, mirrorAccountId, repository);
        }
        else {
          report.addFix("Could not find mirror - deleting series", Series.toString(series));
          Series.delete(series, repository);
        }
      }
      else if (!Series.isTransfer(series) && mirrorSeriesId != null) {
        report.addFix("Unexpected mirror for non transfer series - updating it", Series.toString(series));
        repository.update(series, Series.MIRROR_SERIES, null);
      }
      else if (mirrorSeriesId != null) {
        if (series.get(Series.FROM_ACCOUNT) == null || series.get(Series.TO_ACCOUNT) == null) {
          report.addFix("Savings series with both imported account has null in its account - deleting it", Series.toString(series));
          Series.delete(series, repository);
        }
      }
    }
  }

  private static Integer getMirrorAccount(Glob series) {
    if (Utils.equal(series.get(Series.TARGET_ACCOUNT), series.get(Series.TO_ACCOUNT))) {
      return series.get(Series.FROM_ACCOUNT);
    }
    if (Utils.equal(series.get(Series.TARGET_ACCOUNT), series.get(Series.FROM_ACCOUNT))) {
      return series.get(Series.TO_ACCOUNT);
    }
    return null;
  }

  public static void seriesBudgetArePresent(Glob series, Integer firstMonthForSeries, Integer lastMonthForSeries, GlobRepository repository, DataCheckReport report) {
    int currentMonth;

    currentMonth = firstMonthForSeries;

    java.util.List<Integer> budgetToCreate = new ArrayList<Integer>();

    Set<Integer> budgets =
      repository.getAll(SeriesBudget.TYPE, fieldEquals(SeriesBudget.SERIES, series.get(Series.ID)))
        .getValueSet(SeriesBudget.ID);
    ReadOnlyGlobRepository.MultiFieldIndexed index =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID));
    while (currentMonth <= lastMonthForSeries) {
      Glob budget = index.findByIndex(SeriesBudget.MONTH, currentMonth).getGlobs().getFirst();
      if (budget == null) {
        budgetToCreate.add(currentMonth);
      }
      else {
        budgets.remove(budget.get(SeriesBudget.ID));
      }
      currentMonth = Month.next(currentMonth);
    }

    repository.startChangeSet();
    try {
      for (Integer budgetId : budgets) {
        Key seriesBudgetKey = Key.create(SeriesBudget.TYPE, budgetId);
        Glob seriesBudget = repository.get(seriesBudgetKey);
        report.addFix("Deleting SeriesBudget for month " + seriesBudget.get(SeriesBudget.MONTH), Series.toString(series));
        repository.delete(seriesBudgetKey);
      }
      for (Integer month : budgetToCreate) {
        report.addFix("Adding SeriesBudget for month:" + month, Series.toString(series));
        MonthsToSeriesBudgetTrigger.addMonthForSeries(repository, month, series);
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public static void allSeriesBudgetAreProperlyAssociated(GlobRepository repository, DataCheckReport report) {
    Set<Integer> seriesId = new HashSet<Integer>();
    for (Glob budget : repository.getAll(SeriesBudget.TYPE)) {
      Glob series = repository.findLinkTarget(budget, SeriesBudget.SERIES);
      if (series == null) {
        if (seriesId.add(budget.get(SeriesBudget.SERIES))) {
          report.addError("Missing series for seriesBudget " + budget.get(SeriesBudget.SERIES) + ", deleting it");
        }
        repository.delete(budget);
      }
    }
  }
}
