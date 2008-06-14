package org.globsframework.wicket.table.columns;

import org.globsframework.model.Glob;
import org.globsframework.wicket.table.GlobTableColumn;

import java.io.Serializable;
import java.util.Comparator;

public abstract class AbstractGlobTableColumn implements GlobTableColumn, Serializable {
  private String title;

  protected AbstractGlobTableColumn() {
    this("");
  }

  protected AbstractGlobTableColumn(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public Comparator<Glob> getComparator() {
    return null;
  }
}
