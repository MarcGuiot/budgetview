package org.crossbowlabs.globs.gui;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.GlobList;

public interface GlobSelection {

  GlobType[] getRelevantTypes();

  boolean isRelevantForType(GlobType type);

  GlobList getAll();

  GlobList getAll(GlobType type);
}
