package com.budgetview.desktop.components.expansion;

import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

public class ExpandableTable {

  private GlobTableView globTable;
  private GlobMatcher baseMatcher = GlobMatchers.ALL;

  public ExpandableTable(GlobMatcher baseMatcher) {
    this.baseMatcher = baseMatcher;
  }

  public void setTable(GlobTableView globTable) {
    this.globTable = globTable;
  }

  public Glob getSelectedGlob() {
    return globTable.getGlobAt(globTable.getComponent().getSelectedRow());
  }

  public void select(Glob seriesWrapper) {
    globTable.select(seriesWrapper);
  }

  public void setFilter(GlobMatcher matcher) {
    globTable.setFilter(GlobMatchers.and(baseMatcher, matcher));
  }
}
