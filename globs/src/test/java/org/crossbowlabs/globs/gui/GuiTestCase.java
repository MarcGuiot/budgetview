package org.crossbowlabs.globs.gui;

import org.crossbowlabs.globs.model.GlobChecker;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.Formats;
import org.crossbowlabs.globs.model.format.utils.DefaultDescriptionService;
import org.crossbowlabs.globs.utils.directory.DefaultDirectory;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.IconLocator;
import org.crossbowlabs.splits.color.ColorService;
import org.uispec4j.UISpecTestCase;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class GuiTestCase extends UISpecTestCase {
  protected final Directory directory = new DefaultDirectory();
  protected final GlobChecker checker = new GlobChecker();
  protected final SelectionService selectionService = new SelectionService();

  protected void setUp() throws Exception {
    super.setUp();
    Locale.setDefault(Locale.ENGLISH);
    directory.add(DescriptionService.class,
                  new DefaultDescriptionService(
                    new Formats(new SimpleDateFormat("yyyy/MM/dd"),
                                new SimpleDateFormat("yyyy/MM/dd hh:mm:ss"),
                                new DecimalFormat("#.##"),
                                "yes", "no")));
    directory.add(SelectionService.class, selectionService);
    directory.add(new ColorService());
    directory.add(IconLocator.class, IconLocator.NULL);
  }
}
