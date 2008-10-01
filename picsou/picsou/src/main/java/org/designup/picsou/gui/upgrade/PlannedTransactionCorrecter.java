package org.designup.picsou.gui.upgrade;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.triggers.SeriesBudgetUpdateTransactionTrigger;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.MultiMap;

import java.util.Iterator;
import java.util.List;

public class PlannedTransactionCorrecter {
  private MultiMap<Integer, Info> infos = new MultiMap<Integer, Info>();
  private GlobRepository repository;

  public PlannedTransactionCorrecter(GlobRepository repository) {
    this.repository = repository;
  }

  public void check() {
    GlobList seriesBudget = repository.getAll(SeriesBudget.TYPE);
    for (Glob budget : seriesBudget) {
      GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, budget.get(SeriesBudget.SERIES))
        .findByIndex(Transaction.MONTH, budget.get(SeriesBudget.MONTH))
        .getGlobs();
      Double plannedAmount = 0.;
      Double amount = 0.;
      for (Glob transaction : transactions) {
        if (transaction.get(Transaction.PLANNED)) {
          plannedAmount += transaction.get(Transaction.AMOUNT);
        }
        else {
          amount += transaction.get(Transaction.AMOUNT);
        }
      }
      if (amount + plannedAmount > budget.get(SeriesBudget.AMOUNT)) {
        addError(budget.get(SeriesBudget.ID), budget.get(SeriesBudget.SERIES), budget.get(SeriesBudget.MONTH),
                 amount, plannedAmount, budget.get(SeriesBudget.AMOUNT));
      }
    }
  }

  public void correct() {
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
              .createPlannedTransaction(series, repository, info.monthId, series.get(Series.DAY),
                                        info.budgetAmount - info.observedAmount);
          }
        }
      }
    }
  }

  public void addError(Integer seriesBudgetId, Integer seriesID, Integer monthId, Double plannedAmount,
                       Double observedAMount, Double budgetAmount) {
    infos.put(seriesID, new Info(seriesBudgetId, monthId, observedAMount, budgetAmount));
  }

  static class Info {
    private Integer seriesBudgetId;
    Integer monthId;
    private Double observedAmount;
    private Double budgetAmount;

    public Info(Integer seriesBudgetId, Integer monthId, Double observedAmount, Double budgetAmount) {
      this.seriesBudgetId = seriesBudgetId;
      this.monthId = monthId;
      this.observedAmount = observedAmount;
      this.budgetAmount = budgetAmount;
    }
  }

}
