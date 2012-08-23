package org.globsframework.gui;

import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.utils.Utils;
import org.globsframework.utils.collections.CopyOnWriteMultiMap;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.*;

public class SelectionService {
  private MultiMap<GlobType, GlobSelectionListener> listenersByType =
    new CopyOnWriteMultiMap<GlobType, GlobSelectionListener>();
  private Map<GlobType, GlobList> currentSelections = new HashMap<GlobType, GlobList>();

  public void addListener(final GlobSelectionListener listener, GlobType type, GlobType... types) {
    for (GlobType globType : Utils.list(type, types)) {
      listenersByType.put(globType, listener);
    }
  }

  public void addListener(final GlobSelectionListener listener, GlobType[] types) {
    for (GlobType globType : types) {
      listenersByType.put(globType, listener);
    }
  }

  public void removeListener(GlobSelectionListener listener) {
    listenersByType.removeValue(listener);
  }

  public void select(Glob glob) {
    if (glob == null) {
      throw new InvalidParameter("Glob must be non null. Use clear(GlobType) to clear the selection.");
    }
    select(Collections.singletonList(glob), glob.getType());
  }

  public void select(Collection<Glob> globs, GlobType type) {
    GlobSelection selection = GlobSelectionBuilder.create(globs, type);
    currentSelections.put(type, selection.getAll(type));

    List<GlobSelectionListener> listeners = listenersByType.get(type);
    for (GlobSelectionListener listener : listeners) {
      listener.selectionUpdated(selection);
    }
  }

  public void select(GlobSelection selection) {
    GlobType[] globTypes = selection.getRelevantTypes();
    for (GlobType type : globTypes) {
      currentSelections.put(type, selection.getAll(type));
    }
    Set<GlobSelectionListener> called = new HashSet<GlobSelectionListener>();
    for (GlobType type : globTypes) {
      for (GlobSelectionListener listener : listenersByType.get(type)) {
        if (!called.contains(listener)) {
          listener.selectionUpdated(selection);
          called.add(listener);
        }
      }
    }
  }

  public GlobList getSelection(GlobType globType) {
    GlobList globList = currentSelections.get(globType);
    if (globList == null) {
      return GlobList.EMPTY;
    }
    GlobList list = new GlobList(globList.size());
    for (Glob glob : globList) {
      if (glob.exists()) {
        list.add(glob);
      }
    }
    return list;
  }

  public void clear(GlobType type) {
    select(Collections.<Glob>emptyList(), type);
  }

  public void clearAll() {
    for (GlobType type : currentSelections.keySet()) {
      clear(type);
    }
  }


}
