package org.globsframework.gui.utils;

import org.globsframework.gui.GlobSelection;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import java.util.Collection;

public class GlobSelectionBuilder {
  DefaultGlobSelection selection = new DefaultGlobSelection();

  public static GlobSelection create(final Glob glob) {
    return new SingleListGlobSelection(new GlobList(glob), glob.getType());
  }

  public static GlobSelection create(Collection<Glob> globs, GlobType type) {
    return new SingleListGlobSelection(globs, type);
  }

  public static GlobSelectionBuilder init() {
    return new GlobSelectionBuilder();
  }

  public GlobSelectionBuilder add(GlobList globList, GlobType type) {
    selection.add(globList, type);
    return this;
  }

  public GlobSelectionBuilder add(Glob glob) {
    if (glob == null) {
      return this;
    }
    selection.add(glob, glob.getType());
    return this;
  }

  public GlobSelection get() {
    return selection;
  }

  private static class SingleListGlobSelection implements GlobSelection {
    private final GlobList globs;
    private GlobType type;

    public SingleListGlobSelection(Collection<Glob> globs, GlobType type) {
      this.globs = new GlobList(globs);
      this.type = type;
    }

    public GlobType[] getRelevantTypes() {
      return new GlobType[]{type};
    }

    public boolean isRelevantForType(GlobType type) {
      return this.type.equals(type);
    }

    public GlobList getAll(GlobType type) {
      return globs;
    }

    public String toString() {
      return globs.toString();
    }
  }
}
