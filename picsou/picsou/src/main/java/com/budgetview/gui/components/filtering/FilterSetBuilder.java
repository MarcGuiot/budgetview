package com.budgetview.gui.components.filtering;

import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import java.util.HashMap;
import java.util.Map;

public class FilterSetBuilder {

  private Map<String, GlobMatcher> filters = new HashMap<String, GlobMatcher>();

  public static FilterSetBuilder init() {
    return new FilterSetBuilder();
  }

  public FilterSetBuilder set(String name, GlobMatcher matcher) {
    if (matcher == GlobMatchers.ALL) {
      matcher = null;
    }
    filters.put(name, matcher);
    return this;
  }

  public FilterSet get() {
    return new FilterSet(filters);
  }
}
