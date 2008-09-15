package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.utils.directory.Directory;

public class CompactSeriesStringifier extends AbstractGlobStringifier {
  private GlobStringifier categoryStringifier;
  private GlobStringifier seriesStringifier;

  public CompactSeriesStringifier(Directory directory) {
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    categoryStringifier = descriptionService.getStringifier(Transaction.CATEGORY);
    seriesStringifier = descriptionService.getStringifier(Transaction.SERIES);
  }

  public String toString(Glob transaction, GlobRepository repository) {
    Integer seriesId = transaction.get(Transaction.SERIES);
    if (Series.UNCATEGORIZED_SERIES_ID.equals(seriesId)) {
      return "";
    }

    if (Series.OCCASIONAL_SERIES_ID.equals(seriesId)) {
      return categoryStringifier.toString(transaction, repository);
    }

    return seriesStringifier.toString(transaction, repository);
  }
}
