package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.SubSeries;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.utils.directory.Directory;

public class CompactSeriesStringifier extends AbstractGlobStringifier {
  private GlobStringifier seriesStringifier;

  public CompactSeriesStringifier(Directory directory) {
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    seriesStringifier = descriptionService.getStringifier(Transaction.SERIES);
  }

  public String toString(Glob transaction, GlobRepository repository) {
    if (transaction == null){
      return "";
    }
    Integer seriesId = transaction.get(Transaction.SERIES);
    if (Series.UNCATEGORIZED_SERIES_ID.equals(seriesId)) {
      return "";
    }

    StringBuffer buffer = new StringBuffer();
    buffer.append(seriesStringifier.toString(transaction, repository));
    Integer subSeriesId = transaction.get(Transaction.SUB_SERIES);
    if (subSeriesId != null) {
      buffer.append(" / ");
      Glob subSeries = repository.get(Key.create(SubSeries.TYPE, subSeriesId));
      buffer.append(subSeries.get(SubSeries.NAME));
    }

    return buffer.toString();
  }
}
