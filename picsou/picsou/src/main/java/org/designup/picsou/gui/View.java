package org.designup.picsou.gui;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.gui.description.PicsouDescriptionService;

import java.text.DecimalFormat;

public abstract class View {
  protected GlobRepository repository;
  protected Directory directory;
  protected ColorService colorService;
  protected DescriptionService descriptionService;
  protected SelectionService selectionService;
  protected FontLocator fontLocator;
  protected DecimalFormat decimalFormat;

  protected View(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.colorService = directory.get(ColorService.class);
    this.descriptionService = directory.get(DescriptionService.class);
    this.selectionService = directory.get(SelectionService.class);
    this.fontLocator = directory.get(FontLocator.class);
    this.decimalFormat = PicsouDescriptionService.DECIMAL_FORMAT;
  }

  public void colorsChanged(ColorLocator colorLocator) {
  }

  public abstract void registerComponents(GlobsPanelBuilder builder);
}
