package com.budgetview.gui.transactions.search;

import com.budgetview.gui.components.filtering.FilterManager;
import com.budgetview.gui.components.filtering.components.TextFilterPanel;
import com.budgetview.gui.description.stringifiers.TransactionSeriesStringifier;
import com.budgetview.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import java.util.Map;
import java.util.WeakHashMap;

import static org.globsframework.model.utils.GlobMatchers.*;

public class TransactionFilterPanel extends TextFilterPanel {
  private static DescriptionService descriptionService;

  public TransactionFilterPanel(FilterManager filterSet, GlobRepository repository, Directory directory) {
    super(filterSet, repository, directory);
    descriptionService = directory.get(DescriptionService.class);
  }

  protected GlobMatcher createMatcher(String searchFilter) {
    searchFilter = searchFilter.toLowerCase();
    return or(fieldContainsIgnoreCaseAndAccents(Transaction.ORIGINAL_LABEL, searchFilter),
              fieldContainsIgnoreCaseAndAccents(Transaction.LABEL, searchFilter),
              fieldContainsIgnoreCaseAndAccents(Transaction.NOTE, searchFilter),
              new AmountMatcher(searchFilter, descriptionService),
              new SeriesMatcher(searchFilter));
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
    private Map<Integer, String> seriesNamesCache;
    private Map<Integer, String> subSeriesNamesCache;
    private final GlobStringifier seriesNameStringifier;
    private final String searchFilter;

    public SeriesMatcher(String searchFilter) {
      this.searchFilter = searchFilter.toLowerCase();
      seriesNamesCache = new WeakHashMap<Integer, String>();
      subSeriesNamesCache = new WeakHashMap<Integer, String>();
      seriesNameStringifier = new TransactionSeriesStringifier();
    }

    public boolean matches(Glob transaction, GlobRepository repository) {
      Integer subSeriesId = transaction.get(Transaction.SUB_SERIES);
      String text =
        subSeriesId != null ?
        getSubSeriesText(transaction, subSeriesId, repository) :
        getSeriesText(transaction, repository);
      return text != null && text.toLowerCase().contains(searchFilter);
    }

    private String getSeriesText(Glob transaction, GlobRepository repository) {
      String seriesName = seriesNamesCache.get(transaction.get(Transaction.SERIES));
      if (seriesName == null) {
        seriesName = seriesNameStringifier.toString(transaction, repository);
        seriesNamesCache.put(transaction.get(Transaction.SERIES), seriesName);
      }
      return seriesName;
    }

    private String getSubSeriesText(Glob transaction, Integer subSeriesId, GlobRepository repository) {
      String seriesName = subSeriesNamesCache.get(subSeriesId);
      if (seriesName == null) {
        seriesName = seriesNameStringifier.toString(transaction, repository);
        subSeriesNamesCache.put(subSeriesId, seriesName);
      }
      return seriesName;
    }
  }
}
