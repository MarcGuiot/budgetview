package org.designup.picsou.gui;

import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.SplitsBuilder;
import org.crossbowlabs.splits.color.ColorChangeListener;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.color.ColorSource;

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

  public abstract void registerComponents(SplitsBuilder builder);
}
