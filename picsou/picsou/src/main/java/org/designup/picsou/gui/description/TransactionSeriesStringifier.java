package org.designup.picsou.gui.description;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SubSeries;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.utils.AbstractGlobStringifier;

public class TransactionSeriesStringifier extends AbstractGlobStringifier {

  private SeriesStringifier seriesStringifier = new SeriesStringifier();

  public String toString(Glob transaction, GlobRepository repository) {
    if (transaction == null){
      return null;
    }

    Integer seriesId = transaction.get(Transaction.SERIES);
    if (Series.UNCATEGORIZED_SERIES_ID.equals(seriesId)) {
      return "";
    }
    
    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    String seriesName = seriesStringifier.toString(series, repository);
    Glob subSeries = repository.findLinkTarget(transaction, Transaction.SUB_SERIES);
    if (subSeries != null) {
      return seriesName + " / " + subSeries.get(SubSeries.NAME);
    }
    return seriesName;
  }
}
