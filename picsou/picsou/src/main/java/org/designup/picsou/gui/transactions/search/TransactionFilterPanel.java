package org.designup.picsou.gui.transactions.search;

import org.designup.picsou.gui.components.filtering.FilterManager;
import org.designup.picsou.gui.components.filtering.components.TextFilterPanel;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import java.util.Map;
import java.util.WeakHashMap;

import static org.globsframework.model.utils.GlobMatchers.fieldContainsIgnoreCase;
import static org.globsframework.model.utils.GlobMatchers.or;

public class TransactionFilterPanel extends TextFilterPanel {
  private static DescriptionService descriptionService;

  public TransactionFilterPanel(FilterManager filterSet, GlobRepository repository, Directory directory) {
    super(filterSet, repository, directory);
    descriptionService = directory.get(DescriptionService.class);
  }

  protected GlobMatcher createMatcher(String searchFilter) {
    searchFilter = searchFilter.toLowerCase();
    return or(fieldContainsIgnoreCase(Transaction.LABEL, searchFilter),
              fieldContainsIgnoreCase(Transaction.NOTE, searchFilter),
              new AmountMatcher(searchFilter, descriptionService),
              new SeriesMatcher(searchFilter, descriptionService));
  }

  private class AmountMatcher implements GlobMatcher {
    final GlobStringifier amountStringifier;
    private final String searchFilter;

    public AmountMatcher(String searchFilter, DescriptionService descriptionService) {
      this.searchFilter = searchFilter;
      amountStringifier = descriptionService.getStringifier(Transaction.AMOUNT);
    }

    public boolean matches(Glob item, GlobRepository repository) {
      String text = amountStringifier.toString(item, repository);
      return text != null && text.contains(searchFilter);
    }
  }

  private class SeriesMatcher implements GlobMatcher {
    private Map<Integer, String> cachedNames;
    private final GlobStringifier seriesNameStringifier;
    private final String searchFilter;

    public SeriesMatcher(String searchFilter, DescriptionService descriptionService) {
      this.searchFilter = searchFilter;
      cachedNames = new WeakHashMap<Integer, String>();
      seriesNameStringifier = descriptionService.getStringifier(Transaction.SERIES);
    }

    public boolean matches(Glob item, GlobRepository repository) {
      String seriesName = cachedNames.get(item.get(Transaction.SERIES));
      if (seriesName == null) {
        seriesName = seriesNameStringifier.toString(item, repository);
        cachedNames.put(item.get(Transaction.SERIES), seriesName);
      }
      return seriesName != null && seriesName.toLowerCase().contains(searchFilter);
    }
  }
}
