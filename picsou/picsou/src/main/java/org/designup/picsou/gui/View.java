package org.designup.picsou.gui;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorSource;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;

public abstract class View implements ColorChangeListener {
  protected GlobRepository repository;
  protected Directory directory;
  protected ColorService colorService;
  protected DescriptionService descriptionService;
  protected SelectionService selectionService;

  protected View(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.colorService = directory.get(ColorService.class);
    this.descriptionService = directory.get(DescriptionService.class);
    this.selectionService = directory.get(SelectionService.class);
    colorService.addListener(this);
  }

  public void colorsChanged(ColorSource colorSource) {
  }

  public abstract void registerComponents(GlobsPanelBuilder builder);
}
