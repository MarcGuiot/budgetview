package org.designup.picsou.gui.components.filtering;

import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import java.util.*;

public class FilterManager {
  private Filterable filterable;
  private Map<String, GlobMatcher> filters = new HashMap<String, GlobMatcher>();
  private List<String> clearableNames = new ArrayList<String>();
  private List<FilterClearer> clearers = new ArrayList<FilterClearer>();
  private List<FilterListener> listeners = new ArrayList<FilterListener>();

  public FilterManager(Filterable filterable) {
    this.filterable = filterable;
  }

  public void addListener(FilterListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(FilterListener listener) {
    this.listeners.remove(listener);
  }

  public void addClearer(FilterClearer clearer) {
    clearers.add(clearer);
    clearableNames.addAll(clearer.getAssociatedFilters());
  }

  public void set(FilterSet filterSet) {
    List<String> changedFilters = new ArrayList<String>();
    for (Map.Entry<String, GlobMatcher> entry : filterSet.getEntries()) {
      String name = entry.getKey();
      changedFilters.add(name);
      setSilently(name, entry.getValue());
    }
    notifyChanges(changedFilters);
  }

  public void set(String name, GlobMatcher matcher) {
    setSilently(name, matcher);
    notifyChanges(Collections.singletonList(name));
  }

  private void setSilently(String name, GlobMatcher matcher) {
    if ((matcher == GlobMatchers.ALL) || (matcher == null)) {
      removeSilently(name);
    }
    else {
      filters.put(name, matcher);
    }
  }

  public void replaceAllWith(String name, GlobMatcher matcher) {
    List<String> changedFilters = new ArrayList<String>();
    changedFilters.addAll(filters.keySet());
    changedFilters.add(name);
    filters.clear();
    setSilently(name, matcher);
    notifyChanges(changedFilters);
  }

  public void remove(String name) {
    if (removeSilently(name)) {
      notifyChanges(Collections.singletonList(name));
    }
  }

  private boolean removeSilently(String name) {
    if (!filters.containsKey(name)) {
      return false;
    }
    filters.remove(name);
    return true;
  }

  public boolean hasClearableFilters() {
    return !Collections.disjoint(filters.keySet(), clearableNames);
  }

  public void clear() {
    for (FilterClearer clearer : clearers) {
      clearer.clear();
    }
  }

  public void reset() {
    List<String> changedFilters = new ArrayList<String>();
    changedFilters.addAll(filters.keySet());
    filters.clear();
    notifyChanges(changedFilters);
  }

  public boolean isActive(String name) {
    return filters.containsKey(name);
  }

  private void notifyChanges(List<String> changedFilters) {
    List<GlobMatcher> filterList = new ArrayList<GlobMatcher>();
    filterList.addAll(filters.values());
    GlobMatcher filter = GlobMatchers.and(filterList);
    filterable.setFilter(filter);

    for (FilterListener listener : listeners) {
      listener.filterUpdated(changedFilters);
    }
  }

}
