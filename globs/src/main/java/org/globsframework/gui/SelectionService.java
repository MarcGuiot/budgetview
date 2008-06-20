package org.globsframework.gui;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.utils.MultiMap;
import static org.globsframework.utils.Utils.list;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.*;

public class SelectionService {
  private MultiMap<GlobType, GlobSelectionListener> listenersByType =
    new MultiMap<GlobType, GlobSelectionListener>();

  public void addListener(GlobSelectionListener listener, GlobType... types) {
    if (types.length == 0) {
      throw new InvalidParameter("Registration of " + listener + " must be done for at least one GlobType");
    }
    for (GlobType type : types) {
      listenersByType.put(type, listener);
    }
  }

  public void removeListener(GlobSelectionListener listener) {
    listenersByType.removeValue(listener);
  }

  public void select(Glob glob) {
    if (glob == null) {
      throw new InvalidParameter("Glob must be non null");
    }
    select(Collections.singletonList(glob), glob.getType());
  }

  public void select(Collection<Glob> globs, GlobType type, GlobType... types) {
    List<GlobType> allTypes = list(type, types);
    Set<GlobSelectionListener> listeners = getListeners(allTypes);
    Selection selection = new Selection(globs, list(type, types));
    for (GlobSelectionListener listener : listeners) {
      listener.selectionUpdated(selection);
    }
  }

  private Set<GlobSelectionListener> getListeners(List<GlobType> types) {
    Set<GlobSelectionListener> listeners = new HashSet<GlobSelectionListener>();
    for (GlobType type : types) {
      listeners.addAll(listenersByType.get(type));
    }
    return listeners;
  }

  public void clear(GlobType type) {
    select(Collections.<Glob>emptyList(), type);
  }

  private static class Selection implements GlobSelection {
    private Collection<Glob> selection;
    private List<GlobType> relevantTypes;

    public Selection(Collection<Glob> selection, List<GlobType> types) {
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
}
