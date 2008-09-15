package org.globsframework.gui;

import org.globsframework.gui.utils.DefaultGlobSelection;
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
  private Map<GlobType, GlobList> currentSelections = new HashMap<GlobType, GlobList>();

  public void addListener(final GlobSelectionListener listener, GlobType... types) {
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

  public GlobList getSelection(GlobType globType) {
    GlobList globList = currentSelections.get(globType);
    if (globList == null) {
      return GlobList.EMPTY;
    }
    return new GlobList(globList);
  }

  public void select(Collection<Glob> globs, GlobType type, GlobType... types) {
    List<GlobType> allTypes = list(type, types);
    DefaultGlobSelection selection = new DefaultGlobSelection(globs, allTypes);
    for (GlobType globType : allTypes) {
      currentSelections.put(globType, selection.getAll(globType));
    }

    Set<GlobSelectionListener> listeners = getListeners(allTypes);
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
}
