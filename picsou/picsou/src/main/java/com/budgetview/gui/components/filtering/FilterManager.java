package com.budgetview.gui.components.filtering;

import com.budgetview.utils.Lang;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;

import java.util.*;

public class FilterManager {
  private Filterable filterable;
  private Map<String, GlobMatcher> filters = new HashMap<String, GlobMatcher>();
  private Map<String, String> labels = new HashMap<String, String>();
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

  public void set(String name, String label, GlobMatcher matcher) {
    setSilently(name, label, matcher);
    updateAndNotify(Collections.singletonList(name));
  }

  public void replaceAllWith(String name, String label, GlobMatcher matcher) {
    List<String> changedFilters = new ArrayList<String>();
    changedFilters.addAll(filters.keySet());
    changedFilters.add(name);
    filters.clear();
    labels.clear();
    setSilently(name, label, matcher);
    updateAndNotify(changedFilters);
  }

  private void setSilently(String name, String label, GlobMatcher matcher) {
    if ((matcher == GlobMatchers.ALL) || (matcher == null)) {
      removeSilently(name);
    }
    else {
      filters.put(name, matcher);
      labels.put(name, label);
    }
  }

  public void clear(String name) {
    List<FilterClearer> toClear = new ArrayList<FilterClearer>();
    for (FilterClearer clearer : clearers) {
      if (clearer.getAssociatedFilters().contains(name)) {
        clearer.clear();
        toClear.add(clearer);
      }
    }
    for (FilterClearer clearer : toClear) {
      clearers.remove(clearer);
      clearableNames.removeAll(clearer.getAssociatedFilters());
    }
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
    labels.remove(name);
    return true;
  }

  public boolean hasClearableFilters() {
    return !Collections.disjoint(filters.keySet(), clearableNames);
  }

  public void removeAll() {
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
          labels.remove(name);
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
    labels.clear();
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

  public String getLabel() {
    if (labels.isEmpty()) {
      return "";
    }
    String result = null;
    for (String label : labels.values()) {
      if (Strings.isNotEmpty(label)) {
        if (result == null) {
          result = label;
        }
        else {
          return Lang.get("filter.multi");
        }
      }
    }
    return result;
  }
}
