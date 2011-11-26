package org.designup.picsou.gui.components.filtering;

import java.util.Collection;

public interface FilterListener {
  public void filterUpdated(Collection<String> changedFilters);
}
