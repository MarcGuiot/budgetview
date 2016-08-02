package com.budgetview.desktop.components.filtering;

import org.globsframework.model.utils.GlobMatcher;

public interface Filterable {
  void setFilter(GlobMatcher matcher);

  public static Filterable NO_OP = new Filterable() {
    public void setFilter(GlobMatcher matcher) {

    }
  };
}
