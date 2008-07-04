package org.globsframework.gui.utils;

import org.globsframework.gui.GlobSelection;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.metamodel.GlobType;

import java.util.Collection;
import java.util.List;

public class DefaultSelection implements GlobSelection {
  private Collection<Glob> selection;
  private List<GlobType> relevantTypes;

  public DefaultSelection(Collection<Glob> selection, List<GlobType> types) {
    this.selection = selection;
    this.relevantTypes = types;
  }

  public GlobType[] getRelevantTypes() {
    return relevantTypes.toArray(new GlobType[relevantTypes.size()]);
  }

  public boolean isRelevantForType(GlobType type) {
    return relevantTypes.contains(type);
  }

  public GlobList getAll() {
    return new GlobList(selection);
  }

  public GlobList getAll(GlobType type) {
    if (!relevantTypes.contains(type)) {
      return new GlobList();
    }
    GlobList result = new GlobList();
    for (Glob glob : selection) {
      if (glob.getType().equals(type)) {
        result.add(glob);
      }
    }
    return result;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("GlobSelection:");
    for (GlobType type : relevantTypes) {
      builder.append(' ').append(type.getName());
      builder.append(getAll(type));
    }
    return builder.toString();
  }
}
