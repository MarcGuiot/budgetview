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
  private boolean changeInProgress = false;

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
    updateAndNotify(changedFilters);
  }

  public void set(String name, GlobMatcher matcher) {
    setSilently(name, matcher);
    updateAndNotify(Collections.singletonList(name));
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
    updateAndNotify(changedFilters);
  }

  public void remove(String name) {
    if (removeSilently(name)) {
      updateAndNotify(Collections.singletonList(name));
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
    List<String> removedFilters = new ArrayList<String>();
    try {
      changeInProgress = true;
      for (FilterClearer clearer : clearers) {
        List<String> filterNames = clearer.getAssociatedFilters();
        for (String name : filterNames) {
          GlobMatcher matcher = filters.remove(name);
          if (matcher != null) {
            removedFilters.add(name);
          }
        }
        clearer.clear();
      }
    }
    finally {
      changeInProgress = false;
    }
    updateAndNotify(removedFilters);
  }

  public void reset() {
    List<String> changedFilters = new ArrayList<String>();
    changedFilters.addAll(filters.keySet());
    filters.clear();
    updateAndNotify(changedFilters);
  }

  private void updateAndNotify(List<String> changedFilters) {
    List<GlobMatcher> filterList = new ArrayList<GlobMatcher>();
    filterList.addAll(filters.values());
    GlobMatcher filter = GlobMatchers.and(filterList);
    filterable.setFilter(filter);
    doNotify(changedFilters);
  }

  private void doNotify(Collection<String> changedFilters) {
    if (changeInProgress) {
      return;
    }
    for (FilterListener listener : listeners) {
      listener.filterUpdated(changedFilters);
    }
  }

}
