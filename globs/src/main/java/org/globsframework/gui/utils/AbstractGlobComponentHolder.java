package org.globsframework.gui.utils;

import org.globsframework.gui.ComponentHolder;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;

public abstract class AbstractGlobComponentHolder<T extends AbstractGlobComponentHolder> implements ComponentHolder {
  protected final GlobType type;
  protected final GlobRepository repository;
  protected final Directory directory;
  protected final SelectionService selectionService;
  protected final DescriptionService descriptionService;

  protected AbstractGlobComponentHolder(GlobType type, GlobRepository repository, Directory directory) {
    this.type = type;
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
    this.descriptionService = directory.get(DescriptionService.class);
  }

  public T setName(String name) {
    getComponent().setName(name);
    return (T)this;
  }
}
