package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.directory.Directory;

import java.util.*;

public class GlobStateChecker {
  private GlobRepository repository;
  private List<Correction> corrections = new ArrayList<Correction>();

  boolean hasError() {
    return !corrections.isEmpty();
  }

  public List<Correction> getCorrections() {
    return corrections;
  }

  public interface Correction {
    String info(GlobRepository repository, Directory directory);

    void correct(GlobRepository repository);
  }

  public GlobStateChecker(GlobRepository repository) {
    this.repository = repository;
  }

  public boolean check() {
    SortedSet<Integer> months = repository.getAll(Month.TYPE).getSortedSet(Month.ID);
    checkMonths(months);
    checkTransactions(months);
    checkBudgetAndStat(months);
    checkPlannedTransaction();
    checkPlannedTransactionAmount();
    return corrections.isEmpty();
  }

  private void checkPlannedTransactionAmount() {
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    PlannedTransactionChecker transactionChecker = new PlannedTransactionChecker(corrections);
    GlobList seriesBudget = repository.getAll(SeriesBudget.TYPE);
    for (Glob budget : seriesBudget) {
      if (budget.get(SeriesBudget.MONTH) < currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
        continue;
      }
      GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, budget.get(SeriesBudget.SERIES))
        .findByIndex(Transaction.MONTH, budget.get(SeriesBudget.MONTH))
        .getGlobs();
      Double plannedAmount = 0.;
      Double amount = 0.;
      Double mirrorPlannedAmount = 0.;
      Double mirrorAmount = 0.;
      boolean hasMirror = false;
      Integer forAccountId = null;
      for (Glob transaction : transactions) {
        if (transaction.get(Transaction.MIRROR)) {
          hasMirror = true;
          if (transaction.get(Transaction.PLANNED)) {
            mirrorPlannedAmount += transaction.get(Transaction.AMOUNT);
          }
          else {
            mirrorAmount += transaction.get(Transaction.AMOUNT);
          }
          forAccountId = transaction.get(Transaction.ACCOUNT);
        }
        else {
          if (transaction.get(Transaction.PLANNED)) {
            plannedAmount += transaction.get(Transaction.AMOUNT);
          }
          else {
            amount += transaction.get(Transaction.AMOUNT);
          }
        }
      }
      if (hasMirror) {
        Glob series = repository.findLinkTarget(budget, SeriesBudget.SERIES);
        Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
        Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
        double multiplier = -Account.getMultiplierWithMainAsPointOfView(fromAccount, toAccount, repository);
        if (multiplier == 0) {
          return;
        }
        if (!Amounts.isNearZero(budget.get(SeriesBudget.OVERRUN_AMOUNT)) && !Amounts.isNearZero(mirrorPlannedAmount)) {
          transactionChecker.addError(budget.get(SeriesBudget.ID), budget.get(SeriesBudget.SERIES),
                                      budget.get(SeriesBudget.MONTH),
                                      mirrorAmount, mirrorPlannedAmount, budget.get(SeriesBudget.AMOUNT),
                                      budget.get(SeriesBudget.OVERRUN_AMOUNT));

        }
        else if (Math.abs(multiplier * (mirrorAmount + mirrorPlannedAmount) -
                          (budget.get(SeriesBudget.AMOUNT) + budget.get(SeriesBudget.OVERRUN_AMOUNT))) > 1) {
          transactionChecker.addError(budget.get(SeriesBudget.ID), budget.get(SeriesBudget.SERIES),
                                      budget.get(SeriesBudget.MONTH),
                                      mirrorAmount, mirrorPlannedAmount, budget.get(SeriesBudget.AMOUNT),
                                      budget.get(SeriesBudget.OVERRUN_AMOUNT));
        }
      }
      if (!Amounts.isNearZero(budget.get(SeriesBudget.OVERRUN_AMOUNT)) && !Amounts.isNearZero(plannedAmount)) {
        transactionChecker.addError(budget.get(SeriesBudget.ID), budget.get(SeriesBudget.SERIES),
                                    budget.get(SeriesBudget.MONTH),
                                    amount, plannedAmount, budget.get(SeriesBudget.AMOUNT),
                                    budget.get(SeriesBudget.OVERRUN_AMOUNT));

      }
      else if (Math.abs(amount + plannedAmount -
                        (budget.get(SeriesBudget.AMOUNT) + budget.get(SeriesBudget.OVERRUN_AMOUNT))) > 1) {
        transactionChecker.addError(budget.get(SeriesBudget.ID), budget.get(SeriesBudget.SERIES),
                                    budget.get(SeriesBudget.MONTH),
                                    amount, plannedAmount, budget.get(SeriesBudget.AMOUNT),
                                    budget.get(SeriesBudget.OVERRUN_AMOUNT));
      }
    }
  }

  private void checkPlannedTransaction() {
    MonthPlannedChecker monthPlannedChecker = new MonthPlannedChecker();
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    Integer currentMonthId = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);
    Integer lastDay = currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY);
    GlobList planneds = repository.getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.PLANNED, true));
    for (Glob planned : planneds) {
      if (planned.get(Transaction.MONTH) < currentMonthId ||
          planned.get(Transaction.BANK_MONTH) < currentMonthId) {
        monthPlannedChecker.add(planned);
      }
      else if (planned.get(Transaction.MONTH).equals(currentMonthId) && planned.get(Transaction.DAY) < lastDay) {
        monthPlannedChecker.add(planned);
      }
    }
    monthPlannedChecker.addToErrorList(corrections);
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
    missingMonthonth.addToErrorList(corrections);

    Integer currentMonth = repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH);
    if (currentMonth != 0 && !months.contains(currentMonth)) {
      corrections.add(new Correction() {
        public String info(GlobRepository repository, Directory directory) {
          return "" + GlobStateChecker.this.repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH) + " not in existing month";
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
      monthSeriesChecker.addToErrorList(corrections);
      return;
    }
    if (budgets.isEmpty()) {
      corrections.add(new Correction() {
        public String info(GlobRepository repository, Directory directory) {
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
          corrections.add(new Correction() {
            public String info(GlobRepository repository, Directory directory) {
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

  private void checkAsError(Directory directory) {
    if (!corrections.isEmpty()) {
      throw new RuntimeException(corrections.get(0).info(repository, directory));
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

  static class MissingMonth implements Correction {
    List<Integer> months = new ArrayList<Integer>();
    private String info;

    MissingMonth(String info) {
      this.info = info;
    }

    public void add(Integer monthId) {
      months.add(monthId);
    }

    public void addToErrorList(List<Correction> corrections) {
      if (!months.isEmpty()) {
        corrections.add(this);
      }
    }

    public String info(GlobRepository repository, Directory directory) {
      return info + months;
    }

    public void correct(GlobRepository repository) {
      for (Integer month : months) {
        repository.create(Key.create(Month.TYPE, month));
      }
    }
  }

  static class SeriesBudgetChecker implements Correction {
    List<Integer> missing = new ArrayList<Integer>();
    List<Integer> extrat = new ArrayList<Integer>();
    private Glob series;

    public SeriesBudgetChecker(Glob series) {
      this.series = series;
    }

    public String info(GlobRepository repository, Directory directory) {
      return "For series " + GlobPrinter.toString(series) +
             "\nmissing month : " + missing +
             "\nextra month : " + extrat;
    }

    public void addToErrorList(List<Correction> corrections) {
      if (!extrat.isEmpty() || !missing.isEmpty()) {
        corrections.add(this);
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

  static class MonthSeriesChecker implements Correction {
    private Integer seriesId;
    private Integer firstMonth;
    private Integer lastMonth;

    MonthSeriesChecker(Integer seriesId, Integer firstMonth, Integer lastMonth) {
      this.seriesId = seriesId;
      this.firstMonth = firstMonth;
      this.lastMonth = lastMonth;
    }

    public String info(GlobRepository repository, Directory directory) {
      return "first month " + firstMonth + " should be before last month " + lastMonth;
    }

    public void correct(GlobRepository repository) {
      repository.update(Key.create(Series.TYPE, seriesId), Series.FIRST_MONTH, lastMonth);
      repository.update(Key.create(Series.TYPE, seriesId), Series.LAST_MONTH, firstMonth);
    }

    public void addToErrorList(List<Correction> corrections) {
      corrections.add(this);
    }
  }

  static class MonthPlannedChecker implements Correction {
    GlobList planned = new GlobList();

    public String info(GlobRepository repository, Directory directory) {
      return "Unexpected planned transaction " + GlobPrinter.init(planned).toString();
    }

    public void correct(GlobRepository repository) {
    }

    public void addToErrorList(List<Correction> corrections) {
      if (!planned.isEmpty()) {
        corrections.add(this);
      }
    }

    public void add(Glob planned) {
      this.planned.add(planned);
    }
  }

  static private class PlannedTransactionChecker implements Correction {
    private MultiMap<Integer, Info> infos = new MultiMap<Integer, Info>();
    private List<Correction> corrections;

    private PlannedTransactionChecker(List<Correction> corrections) {
      this.corrections = corrections;
    }

    public String info(GlobRepository repository, Directory directory) {
      DescriptionService descriptionService = directory.get(DescriptionService.class);
      StringBuilder builder = new StringBuilder();
      GlobStringifier stringifier = descriptionService.getStringifier(Series.TYPE);
      for (Integer seriesId : infos.keySet()) {
        String name = stringifier.toString(repository.get(Key.create(Series.TYPE, seriesId)), repository);
        if (seriesId.equals(Series.UNCATEGORIZED_SERIES_ID)) {
          name = "'uncategorized series'";
        }
        if (name.equals("")) {
          name = Integer.toString(seriesId);
        }
        builder.append("Series ").append(name).append(" has error \n");
        List<Info> infoList = infos.get(seriesId);
        for (Info info : infoList) {
          builder.append("==>")
            .append(info.monthId)
            .append(" budget=").append(info.budgetAmount)
            .append(" real=").append(info.observedAmount)
            .append(" overrun=").append(info.overrunAmount)
            .append(" planned=").append(info.plannedAmount)
            .append("\n");
        }
      }
      return builder.toString();
    }

    public void correct(GlobRepository repository) {
      for (Integer seriesId : infos.keySet()) {
        List<Info> infoList = infos.get(seriesId);
        for (Info info : infoList) {
          GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
            .findByIndex(Transaction.MONTH, info.monthId).getGlobs()
            .filterSelf(GlobMatchers.fieldEquals(Transaction.PLANNED, true), repository);
          Iterator<Glob> iterator = transactions.iterator();
          if (iterator.hasNext()) {
            Glob transaction = iterator.next();
            if (info.observedAmount >= info.budgetAmount) {
              repository.delete(transaction.getKey());
              repository.update(Key.create(SeriesBudget.TYPE, info.seriesBudgetId), SeriesBudget.OVERRUN_AMOUNT,
                                info.budgetAmount - info.observedAmount);
            }
            else {
              repository.update(transaction.getKey(), Transaction.AMOUNT,
                                info.budgetAmount - info.observedAmount);
            }
            while (iterator.hasNext()) {
              repository.delete(iterator.next().getKey());
            }
          }
          else {
            if (info.observedAmount >= info.budgetAmount) {
              repository.update(Key.create(SeriesBudget.TYPE, info.seriesBudgetId), SeriesBudget.OVERRUN_AMOUNT,
                                info.budgetAmount - info.observedAmount);
            }
            else {
              Glob series = repository.get(Key.create(Series.TYPE, seriesId));
              SeriesBudgetUpdateTransactionTrigger
                .createPlannedTransaction(series, repository, info.monthId,
                                          Month.getDay(series.get(Series.DAY), info.monthId, Calendar.getInstance()),
                                          info.budgetAmount - info.observedAmount);
            }
          }
        }
      }
    }

    public void addError(Integer seriesBudgetId, Integer seriesID, Integer monthId, Double observedAmount,
                         Double plannedAmount, Double budgetAmount, Double overrunAmount) {
      if (infos.isEmpty()) {
        corrections.add(this);
      }
      infos.put(seriesID, new Info(seriesBudgetId, monthId, observedAmount, plannedAmount, budgetAmount, overrunAmount));
    }

    static class Info {
      private Integer seriesBudgetId;
      Integer monthId;
      private Double plannedAmount;
      private Double observedAmount;
      private Double budgetAmount;
      private Double overrunAmount;

      public Info(Integer seriesBudgetId, Integer monthId, Double observedAmount, Double plannedAmount,
                  Double budgetAmount, Double overrunAmount) {
        this.seriesBudgetId = seriesBudgetId;
        this.monthId = monthId;
        this.plannedAmount = plannedAmount;
        this.observedAmount = observedAmount;
        this.budgetAmount = budgetAmount;
        this.overrunAmount = overrunAmount;
      }
    }
  }
}
