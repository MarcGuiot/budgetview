package org.designup.picsou.gui.components.filtering;

import java.util.List;

public interface FilterClearer {

  public List<String> getAssociatedFilters();

  public void clear();
}
