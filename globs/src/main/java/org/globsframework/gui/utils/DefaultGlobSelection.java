package org.globsframework.gui.utils;

import org.globsframework.gui.GlobSelection;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;

public class DefaultGlobSelection implements GlobSelection {
  private Collection<Glob> globs;
  private Collection<GlobType> relevantTypes;

  public DefaultGlobSelection(GlobSelection selection) {
    globs = selection.getAll();
    relevantTypes = new ArrayList<GlobType>(Arrays.asList(selection.getRelevantTypes()));
  }

  public DefaultGlobSelection(Collection<Glob> globs, Collection<GlobType> types) {
    this.globs = globs;
    this.relevantTypes = types;
  }

  public void add(GlobList globs, GlobType type) {
    this.globs.addAll(globs);
    relevantTypes.add(type);
  }

  public GlobType[] getRelevantTypes() {
    return relevantTypes.toArray(new GlobType[relevantTypes.size()]);
  }

  public boolean isRelevantForType(GlobType type) {
    return relevantTypes.contains(type);
  }

  public GlobList getAll() {
    return new GlobList(globs);
  }

  public GlobList getAll(GlobType type) {
    if (!relevantTypes.contains(type)) {
      return new GlobList();
    }
    GlobList result = new GlobList();
    for (Glob glob : globs) {
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
