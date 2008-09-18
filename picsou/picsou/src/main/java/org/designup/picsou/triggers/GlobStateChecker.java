package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class GlobStateChecker {
  private GlobRepository repository;
  private List<Correcteur> correcteurs = new ArrayList<Correcteur>();

  boolean hasError() {
    return !correcteurs.isEmpty();
  }

  public List<Correcteur> getCorrecteurs() {
    return correcteurs;
  }

  public interface Correcteur {
    String info();

    void correct(GlobRepository repository);
  }

  public GlobStateChecker(GlobRepository repository) {
    this.repository = repository;
  }

  public boolean check() {
    SortedSet<Integer> months = repository.getAll(Month.TYPE).getSortedSet(Month.ID);
    checkMonths(months);
    checkAsError();
    checkTransactions(months);
    checkAsError();
    checkBudgetAndStat(months);
    checkAsError();
    checkPlannedTransaction(months);
    checkAsError();
    return correcteurs.isEmpty();
  }

  private void checkPlannedTransaction(SortedSet<Integer> months) {
    MonthPlannedChecker monthPlannedChecker = new MonthPlannedChecker();
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    Integer currentMonthId = currentMonth.get(CurrentMonth.MONTH_ID);
    GlobList planneds = repository.getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.PLANNED, true));
    for (Glob planned : planneds) {
      if (planned.get(Transaction.MONTH) < currentMonthId ||
          planned.get(Transaction.BANK_MONTH) < currentMonthId) {
        monthPlannedChecker.add(planned);
      }
    }
    monthPlannedChecker.addToErrorList(correcteurs);
  }

  private void checkMonths(SortedSet<Integer> months) {
    MissingMonth missingMonthonth = new MissingMonth("Missing month bettewenn "
                                                     + months.first() + " and " + months.last() + " : ");
    Integer expectedMonth = months.first();
    for (Integer month : months) {
      if (!expectedMonth.equals(month)) {
        missingMonthonth.add(month);
      }
      expectedMonth = Month.next(expectedMonth);
    }
    missingMonthonth.addToErrorList(correcteurs);

    Integer currentMonth = repository.get(CurrentMonth.KEY).get(CurrentMonth.MONTH_ID);
    if (currentMonth != 0 && !months.contains(currentMonth)) {
      correcteurs.add(new Correcteur() {
        public String info() {
          return "" + repository.get(CurrentMonth.KEY).get(CurrentMonth.MONTH_ID) + " not in existing month";
        }

        public void correct(GlobRepository repository) {
        }
      });
    }
  }

  private void checkBudgetAndStat(SortedSet<Integer> months) {
    GlobList series = repository.getAll(Series.TYPE);
    for (Glob oneSeries : series) {
      checkBudgetInSeries(oneSeries, months);
    }
  }

  private void checkBudgetInSeries(final Glob series, SortedSet<Integer> months) {
    SeriesBudgetChecker seriesBudgetChecker = new SeriesBudgetChecker(series);
    GlobList budgets = repository.getAll(SeriesBudget.TYPE,
                                         GlobMatchers.fieldEquals(SeriesBudget.SERIES, series.get(Series.ID)))
      .sort(SeriesBudget.MONTH);
    Integer firstMonth = series.get(Series.FIRST_MONTH);
    if (firstMonth == null) {
      firstMonth = months.first();
    }
    Integer lastMonth = series.get(Series.LAST_MONTH);
    if (lastMonth == null) {
      lastMonth = months.last();
    }
    if (firstMonth > lastMonth) {
      MonthSeriesChecker monthSeriesChecker = new MonthSeriesChecker(series.get(Series.ID), firstMonth, lastMonth);
      monthSeriesChecker.addToErrorList(correcteurs);
      return;
    }
    if (budgets.isEmpty()) {
      correcteurs.add(new Correcteur() {
        public String info() {
          return "no series budget for " + GlobPrinter.toString(series);
        }

        public void correct(GlobRepository repository) {
        }
      });
    }
    Integer currentBeginMonth = budgets.getFirst().get(SeriesBudget.MONTH);
    while (currentBeginMonth > firstMonth) {
      seriesBudgetChecker.addMissingMonth(currentBeginMonth);
      currentBeginMonth = Month.previous(currentBeginMonth);
    }
    Integer currentLastMonth = budgets.getLast().get(SeriesBudget.MONTH);
    while (currentLastMonth < lastMonth) {
      seriesBudgetChecker.addMissingMonth(currentLastMonth);
      currentLastMonth = Month.next(currentLastMonth);
    }
    for (Glob budget : budgets) {
      if (budget.get(SeriesBudget.MONTH) < firstMonth) {
        seriesBudgetChecker.addExtratMonth(budget.get(SeriesBudget.MONTH));
      }
      else if (budget.get(SeriesBudget.MONTH) > lastMonth) {
        seriesBudgetChecker.addExtratMonth(budget.get(SeriesBudget.MONTH));
      }
      else if (budget.get(SeriesBudget.MONTH).equals(firstMonth)) {
        Glob stat = repository.find(Key.create(SeriesStat.SERIES, series.get(Series.ID), SeriesStat.MONTH, firstMonth));
        if (stat == null) {
          final Integer firstMonth1 = firstMonth;
          correcteurs.add(new Correcteur() {
            public String info() {
              return "Missing stat month " + firstMonth1 + " for " + GlobPrinter.toString(series);
            }

            public void correct(GlobRepository repository) {
            }
          });
        }
      }
      else {
        while (!budget.get(SeriesBudget.MONTH).equals(firstMonth)) {
          seriesBudgetChecker.addMissingMonth(firstMonth);
          firstMonth = Month.next(firstMonth);
        }
      }
    }
  }

  private void checkAsError() {
    if (!correcteurs.isEmpty()) {
      throw new RuntimeException(correcteurs.get(0).info());
    }
  }

  private void checkTransactions(SortedSet<Integer> months) {
    MissingMonth missingMonth = new MissingMonth("Missing month versus transaction date. ");
    GlobList transactions = repository.getAll(Transaction.TYPE);
    for (Glob transaction : transactions) {
      if (!months.contains(transaction.get(Transaction.MONTH))) {
        missingMonth.add(transaction.get(Transaction.MONTH));
      }
    }
  }

  static class MissingMonth implements Correcteur {
    List<Integer> months = new ArrayList<Integer>();
    private String info;

    MissingMonth(String info) {
      this.info = info;
    }

    public void add(Integer monthId) {
      months.add(monthId);
    }

    public void addToErrorList(List<Correcteur> correcteurs) {
      if (!months.isEmpty()) {
        correcteurs.add(this);
      }
    }

    public String info() {
      return info + months;
    }

    public void correct(GlobRepository repository) {
      for (Integer month : months) {
        repository.create(Key.create(Month.TYPE, month));
      }
    }
  }

  static class SeriesBudgetChecker implements Correcteur {
    List<Integer> missing = new ArrayList<Integer>();
    List<Integer> extrat = new ArrayList<Integer>();
    private Glob series;

    public SeriesBudgetChecker(Glob series) {
      this.series = series;
    }

    public String info() {
      return "For series " + GlobPrinter.toString(series) +
             "\nmissing month : " + missing +
             "\nextra month : " + extrat;
    }

    public void addToErrorList(List<Correcteur> correcteurs) {
      if (!extrat.isEmpty() || !missing.isEmpty()) {
        correcteurs.add(this);
      }
    }


    public void correct(GlobRepository repository) {
    }

    public void addExtratMonth(Integer monthId) {
      extrat.add(monthId);
    }

    public void addMissingMonth(Integer monthId) {
      missing.add(monthId);
    }
  }

  static class MonthSeriesChecker implements Correcteur {
    private Integer seriesId;
    private Integer firstMonth;
    private Integer lastMonth;

    MonthSeriesChecker(Integer seriesId, Integer firstMonth, Integer lastMonth) {
      this.seriesId = seriesId;
      this.firstMonth = firstMonth;
      this.lastMonth = lastMonth;
    }

    public String info() {
      return "first month " + firstMonth + " should be before last month " + lastMonth;
    }

    public void correct(GlobRepository repository) {
      repository.update(Key.create(Series.TYPE, seriesId), Series.FIRST_MONTH, lastMonth);
      repository.update(Key.create(Series.TYPE, seriesId), Series.LAST_MONTH, firstMonth);
    }

    public void addToErrorList(List<Correcteur> correcteurs) {
      correcteurs.add(this);
    }
  }

  static class MonthPlannedChecker implements Correcteur {
    GlobList planned = new GlobList();

    public String info() {
      return "Unexpected planned transaction " + GlobPrinter.init(planned).toString();
    }

    public void correct(GlobRepository repository) {
    }

    public void addToErrorList(List<Correcteur> correcteurs) {
      if (!planned.isEmpty()) {
        correcteurs.add(this);
      }
    }

    public void add(Glob planned) {
      this.planned.add(planned);
    }
  }
}
