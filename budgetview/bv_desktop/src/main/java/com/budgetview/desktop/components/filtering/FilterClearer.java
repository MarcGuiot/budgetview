package com.budgetview.desktop.components.filtering;

import java.util.List;

public interface FilterClearer {

  public List<String> getAssociatedFilters();

  public void clear();
}
