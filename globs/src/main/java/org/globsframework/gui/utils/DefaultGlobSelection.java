package org.globsframework.gui.utils;

import org.globsframework.gui.GlobSelection;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultGlobSelection implements GlobSelection {
  private Map<GlobType, GlobList> globs = new HashMap<GlobType, GlobList>();


  public DefaultGlobSelection() {
  }

  public DefaultGlobSelection(Glob selection) {
    if (selection == null){
      throw new RuntimeException("null not allowed here." );
    }
    globs.put(selection.getType(), new GlobList(selection));
  }

  public DefaultGlobSelection add(Collection<Glob> globs, GlobType type) {
    for (Glob glob : globs) {
      if (glob == null){
        throw new RuntimeException("null not allowed here for " + type.getName());
      }
    }
    GlobList globList = this.globs.get(type);
    if (globList == null) {
      globList = new GlobList();
      this.globs.put(type, globList);
    }
    globList.addAll(globs);
    return this;
  }

  public DefaultGlobSelection add(Glob glob, GlobType type) {
    if (glob == null){
      throw new RuntimeException("null not allowed here for " + type.getName());
    }
    GlobList globList = this.globs.get(type);
    if (globList == null) {
      globList = new GlobList();
      this.globs.put(type, globList);
    }
    globList.add(glob);
    return this;
  }

  public GlobType[] getRelevantTypes() {
    return globs.keySet().toArray(new GlobType[globs.size()]);
  }

  public boolean isRelevantForType(GlobType type) {
    return globs.containsKey(type);
  }

  public GlobList getAll(GlobType type) {
    GlobList globList = globs.get(type);
    if (globList == null) {
      return GlobList.EMPTY;
    }
    return new GlobList(globList);
  }

  public Glob findFirst(GlobType type) {
    GlobList globList = globs.get(type);
    if (globList == null || globList.isEmpty()) {
      return null;
    }
    return globList.getFirst();
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("GlobSelection:");
    for (GlobType type : globs.keySet()) {
      builder.append(' ').append(type.getName());
      builder.append(getAll(type));
    }
    return builder.toString();
  }
}
