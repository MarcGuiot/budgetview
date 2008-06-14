package org.globsframework.gui;

import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;

public interface GlobSelection {

  GlobType[] getRelevantTypes();

  boolean isRelevantForType(GlobType type);

  GlobList getAll();

  GlobList getAll(GlobType type);
}
