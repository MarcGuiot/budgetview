package com.budgetview.gui.components.filtering;

import java.util.Collection;

public interface FilterListener {
  public void filterUpdated(Collection<String> changedFilters);
}
