package org.crossbowlabs.globs.gui.utils;

import org.crossbowlabs.globs.gui.ComponentHolder;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.utils.directory.Directory;

public abstract class AbstractGlobComponentHolder implements ComponentHolder {
  protected final GlobType type;
  protected final GlobRepository repository;
  protected final SelectionService selectionService;
  protected final DescriptionService descriptionService;

  protected AbstractGlobComponentHolder(GlobType type, GlobRepository repository, Directory directory) {
    this.type = type;
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
    this.descriptionService = directory.get(DescriptionService.class);
  }
}
