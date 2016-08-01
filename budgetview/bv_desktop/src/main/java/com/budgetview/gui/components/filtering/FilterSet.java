package com.budgetview.gui.components.filtering;

import org.globsframework.model.utils.GlobMatcher;

import java.util.Map;
import java.util.Set;

public class FilterSet {
  private Map<String, GlobMatcher> filters;

  public FilterSet(Map<String, GlobMatcher> filters) {
    this.filters = filters;
  }

  Set<Map.Entry<String, GlobMatcher>> getEntries() {
    return filters.entrySet();
  }
}
