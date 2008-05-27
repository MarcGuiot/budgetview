package org.crossbowlabs.globs.gui;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.utils.MultiMap;
import static org.crossbowlabs.globs.utils.Utils.list;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;

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
