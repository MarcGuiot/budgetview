package org.designup.picsou.gui.description;

import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.BudgetArea;

public class TransactionSeriesStringifier extends AbstractGlobStringifier {

  private CategoryStringifier categoryStringifier = new CategoryStringifier();
  private SeriesStringifier seriesStringifier = new SeriesStringifier();

  public String toString(Glob transaction, GlobRepository repository) {
    Integer seriesId = transaction.get(Transaction.SERIES);
    if (seriesId == null) {
      return "";
    }
    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    if (BudgetArea.OCCASIONAL_EXPENSES.getId().equals(series.get(Series.BUDGET_AREA))) {
      Glob category = repository.findLinkTarget(transaction, Transaction.CATEGORY);
      return categoryStringifier.toString(category, repository);
    }
    return seriesStringifier.toString(series, repository);
  }
}
